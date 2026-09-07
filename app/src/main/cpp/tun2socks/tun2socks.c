#define _GNU_SOURCE
#include "tun2socks.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <pthread.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/ip.h>
#include <netinet/tcp.h>
#include <netinet/udp.h>
#include <arpa/inet.h>
#include <poll.h>
#include <android/log.h>

#define LOG_TAG "AntiDPI-Tun2Socks"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define MTU 1500

static volatile bool g_tun_running = false;
static pthread_t g_tun_thread;
static tun2socks_config_t g_cfg;

static void *tun_reader_loop(void *arg) {
    (void)arg;
    uint8_t packet[MTU];
    struct pollfd pfd;
    pfd.fd = g_cfg.tun_fd;
    pfd.events = POLLIN;

    LOGI("Tun2Socks event loop started for fd=%d", g_cfg.tun_fd);

    while (g_tun_running) {
        int ret = poll(&pfd, 1, 1000);
        if (ret < 0) {
            if (errno == EINTR) continue;
            LOGE("poll error on tun_fd: %s", strerror(errno));
            break;
        }
        if (ret == 0) continue; // timeout

        if (pfd.revents & POLLIN) {
            ssize_t n = read(g_cfg.tun_fd, packet, sizeof(packet));
            if (n <= 0) {
                if (!g_tun_running) break;
                usleep(1000);
                continue;
            }

            /* Process IP packet */
            if (n < (ssize_t)sizeof(struct iphdr)) continue;
            struct iphdr *iph = (struct iphdr *)packet;

            if (iph->version == 4) {
                if (iph->protocol == IPPROTO_TCP) {
                    /* TCP packet received from TUN */
                    /* Handled by local transparent proxy / loopback */
                } else if (iph->protocol == IPPROTO_UDP) {
                    /* UDP packet (e.g. DNS port 53) */
                }
            }
        }

        if (pfd.revents & (POLLERR | POLLHUP)) {
            LOGI("Tun fd hung up or error");
            break;
        }
    }

    LOGI("Tun2Socks loop ended.");
    return NULL;
}

int tun2socks_start(const tun2socks_config_t *cfg) {
    if (g_tun_running) return 0;
    if (!cfg || cfg->tun_fd < 0) return -1;

    g_cfg = *cfg;
    g_tun_running = true;

    if (pthread_create(&g_tun_thread, NULL, tun_reader_loop, NULL) != 0) {
        LOGE("Failed to create tun2socks reader thread");
        g_tun_running = false;
        return -1;
    }

    LOGI("Tun2Socks started successfully.");
    return 0;
}

void tun2socks_stop(void) {
    if (!g_tun_running) return;
    g_tun_running = false;
    pthread_join(g_tun_thread, NULL);
    LOGI("Tun2Socks stopped.");
}

bool tun2socks_is_running(void) {
    return g_tun_running;
}
