#define _GNU_SOURCE
#include "proxy.h"
#include "conntrack.h"
#include "desync.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <pthread.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <poll.h>
#include <android/log.h>

#define LOG_TAG "AntiDPI-Proxy"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define BUFFER_SIZE 16384

static volatile bool g_running = false;
static int g_server_fd = -1;
static pthread_t g_server_thread;
static proxy_config_t g_proxy_cfg;

typedef struct {
    int client_fd;
    proxy_config_t cfg;
} client_worker_arg_t;

static int connect_outbound(const char *host, int port, socket_protect_fn protect_fn) {
    struct addrinfo hints, *res = NULL, *rp = NULL;
    char port_str[16];
    snprintf(port_str, sizeof(port_str), "%d", port);

    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;

    if (getaddrinfo(host, port_str, &hints, &res) != 0 || !res) {
        return -1;
    }

    int out_fd = -1;
    for (rp = res; rp != NULL; rp = rp->ai_next) {
        out_fd = socket(rp->ai_family, rp->ai_socktype, rp->ai_protocol);
        if (out_fd < 0) continue;

        /* Protect socket from Android VPN routing loop */
        if (protect_fn) {
            protect_fn(out_fd);
        }

        /* Enable TCP_NODELAY */
        int one = 1;
        setsockopt(out_fd, IPPROTO_TCP, TCP_NODELAY, &one, sizeof(one));

        if (connect(out_fd, rp->ai_addr, rp->ai_addrlen) == 0) {
            break;
        }

        close(out_fd);
        out_fd = -1;
    }

    freeaddrinfo(res);
    return out_fd;
}

static void *client_handler_thread(void *arg) {
    client_worker_arg_t *carg = (client_worker_arg_t *)arg;
    int client_fd = carg->client_fd;
    proxy_config_t cfg = carg->cfg;
    free(carg);

    conntrack_add_connection();

    uint8_t buffer[BUFFER_SIZE];
    ssize_t n = recv(client_fd, buffer, sizeof(buffer), 0);
    if (n < 2 || buffer[0] != 0x05) { /* SOCKS5 version check */
        close(client_fd);
        conntrack_remove_connection();
        return NULL;
    }

    /* SOCKS5 Auth Handshake: Reply NO_AUTH (0x05, 0x00) */
    uint8_t auth_reply[2] = {0x05, 0x00};
    send(client_fd, auth_reply, 2, MSG_NOSIGNAL);

    /* SOCKS5 Connection Request */
    n = recv(client_fd, buffer, sizeof(buffer), 0);
    if (n < 4 || buffer[0] != 0x05 || buffer[1] != 0x01) { /* CONNECT command = 0x01 */
        close(client_fd);
        conntrack_remove_connection();
        return NULL;
    }

    char target_host[256];
    int target_port = 0;
    uint8_t atyp = buffer[3];

    if (atyp == 0x01) { /* IPv4 */
        if (n < 10) { close(client_fd); conntrack_remove_connection(); return NULL; }
        struct in_addr in;
        memcpy(&in, buffer + 4, 4);
        inet_ntop(AF_INET, &in, target_host, sizeof(target_host));
        target_port = ((int)buffer[8] << 8) | buffer[9];
    } else if (atyp == 0x03) { /* Domain Name */
        uint8_t domain_len = buffer[4];
        if (n < 5 + domain_len + 2) { close(client_fd); conntrack_remove_connection(); return NULL; }
        memcpy(target_host, buffer + 5, domain_len);
        target_host[domain_len] = '\0';
        target_port = ((int)buffer[5 + domain_len] << 8) | buffer[5 + domain_len + 1];
    } else if (atyp == 0x04) { /* IPv6 */
        if (n < 22) { close(client_fd); conntrack_remove_connection(); return NULL; }
        struct in6_addr in6;
        memcpy(&in6, buffer + 4, 16);
        inet_ntop(AF_INET6, &in6, target_host, sizeof(target_host));
        target_port = ((int)buffer[20] << 8) | buffer[21];
    } else {
        close(client_fd);
        conntrack_remove_connection();
        return NULL;
    }

    int remote_fd = connect_outbound(target_host, target_port, cfg.protect_fn);
    if (remote_fd < 0) {
        /* Reply SOCKS5 General Failure (0x01) */
        uint8_t fail_reply[10] = {0x05, 0x01, 0x00, 0x01, 0,0,0,0, 0,0};
        send(client_fd, fail_reply, 10, MSG_NOSIGNAL);
        close(client_fd);
        conntrack_remove_connection();
        return NULL;
    }

    /* Reply SOCKS5 SUCCESS (0x00) */
    uint8_t success_reply[10] = {0x05, 0x00, 0x00, 0x01, 0,0,0,0, 0,0};
    send(client_fd, success_reply, 10, MSG_NOSIGNAL);

    /* Data relay loop with DPI desynchronization on first outgoing payload */
    bool initial_payload_sent = false;
    struct pollfd fds[2];
    fds[0].fd = client_fd;
    fds[0].events = POLLIN;
    fds[1].fd = remote_fd;
    fds[1].events = POLLIN;

    while (g_running) {
        int ret = poll(fds, 2, 30000); /* 30 sec idle timeout */
        if (ret <= 0) break;

        /* Client -> Remote */
        if (fds[0].revents & POLLIN) {
            ssize_t bytes = recv(client_fd, buffer, sizeof(buffer), 0);
            if (bytes <= 0) break;

            if (!initial_payload_sent) {
                /* Apply DPI Desynchronization (SNI split, fake TTL, disorder) */
                desync_send_payload(remote_fd, buffer, bytes, &cfg.desync_cfg);
                conntrack_inc_desynced();
                initial_payload_sent = true;
            } else {
                send(remote_fd, buffer, bytes, MSG_NOSIGNAL);
            }
            conntrack_add_bytes_out(bytes);
        }

        /* Remote -> Client */
        if (fds[1].revents & POLLIN) {
            ssize_t bytes = recv(remote_fd, buffer, sizeof(buffer), 0);
            if (bytes <= 0) break;
            send(client_fd, buffer, bytes, MSG_NOSIGNAL);
            conntrack_add_bytes_in(bytes);
        }

        if ((fds[0].revents & (POLLERR | POLLHUP)) || (fds[1].revents & (POLLERR | POLLHUP))) {
            break;
        }
    }

    close(remote_fd);
    close(client_fd);
    conntrack_remove_connection();
    return NULL;
}

static void *server_listener_thread(void *arg) {
    (void)arg;
    while (g_running) {
        struct sockaddr_in client_addr;
        socklen_t client_len = sizeof(client_addr);
        int client_fd = accept(g_server_fd, (struct sockaddr *)&client_addr, &client_len);
        if (client_fd < 0) {
            if (!g_running) break;
            usleep(5000);
            continue;
        }

        client_worker_arg_t *carg = (client_worker_arg_t *)malloc(sizeof(client_worker_arg_t));
        if (!carg) {
            close(client_fd);
            continue;
        }
        carg->client_fd = client_fd;
        carg->cfg = g_proxy_cfg;

        pthread_t worker;
        pthread_attr_t attr;
        pthread_attr_init(&attr);
        pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
        if (pthread_create(&worker, &attr, client_handler_thread, carg) != 0) {
            free(carg);
            close(client_fd);
        }
        pthread_attr_destroy(&attr);
    }
    return NULL;
}

int proxy_start(const proxy_config_t *cfg) {
    if (g_running) return 0;

    g_proxy_cfg = *cfg;
    conntrack_init();

    g_server_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (g_server_fd < 0) {
        LOGE("Failed to create server socket: %s", strerror(errno));
        return -1;
    }

    int opt = 1;
    setsockopt(g_server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

    struct sockaddr_in saddr;
    memset(&saddr, 0, sizeof(saddr));
    saddr.sin_family = AF_INET;
    saddr.sin_addr.s_addr = inet_addr("127.0.0.1");
    saddr.sin_port = htons(cfg->listen_port);

    if (bind(g_server_fd, (struct sockaddr *)&saddr, sizeof(saddr)) < 0) {
        LOGE("Failed to bind proxy on port %d: %s", cfg->listen_port, strerror(errno));
        close(g_server_fd);
        g_server_fd = -1;
        return -1;
    }

    if (listen(g_server_fd, 128) < 0) {
        LOGE("Failed to listen on proxy socket: %s", strerror(errno));
        close(g_server_fd);
        g_server_fd = -1;
        return -1;
    }

    g_running = true;
    if (pthread_create(&g_server_thread, NULL, server_listener_thread, NULL) != 0) {
        LOGE("Failed to create server thread");
        g_running = false;
        close(g_server_fd);
        g_server_fd = -1;
        return -1;
    }

    LOGI("AntiDPI local SOCKS5 proxy started successfully on 127.0.0.1:%d", cfg->listen_port);
    return 0;
}

void proxy_stop(void) {
    if (!g_running) return;
    g_running = false;
    if (g_server_fd >= 0) {
        shutdown(g_server_fd, SHUT_RDWR);
        close(g_server_fd);
        g_server_fd = -1;
    }
    pthread_join(g_server_thread, NULL);
    conntrack_cleanup();
    LOGI("AntiDPI local proxy stopped.");
}

bool proxy_is_running(void) {
    return g_running;
}
