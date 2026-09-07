#define _GNU_SOURCE
#include "desync.h"

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <sys/socket.h>
#include <android/log.h>

#define LOG_TAG "AntiDPI-Desync"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

bool is_tls_client_hello(const uint8_t *buf, size_t len) {
    if (len < 9) return false;
    /* ContentType: Handshake (0x16) */
    if (buf[0] != 0x16) return false;
    /* Legacy Version: 0x0301 (TLS 1.0) .. 0x0303 (TLS 1.2) */
    if (buf[1] != 0x03 || buf[2] > 0x04) return false;
    /* HandshakeType: ClientHello (0x01) */
    if (buf[5] != 0x01) return false;
    return true;
}

bool find_sni_offset(const uint8_t *buf, size_t len, size_t *offset, size_t *sni_len) {
    if (!is_tls_client_hello(buf, len)) return false;

    size_t pos = 5; /* Handshake Header */
    if (pos + 4 > len) return false;

    uint32_t hs_len = ((uint32_t)buf[pos + 1] << 16) | ((uint32_t)buf[pos + 2] << 8) | buf[pos + 3];
    pos += 4;
    if (pos + hs_len > len) hs_len = (uint32_t)(len - pos);

    /* ClientHello: version (2) + random (32) = 34 */
    if (pos + 34 > len) return false;
    pos += 34;

    /* Session ID */
    if (pos >= len) return false;
    uint8_t session_id_len = buf[pos];
    pos += 1 + session_id_len;

    /* Cipher Suites */
    if (pos + 2 > len) return false;
    uint16_t cipher_len = ((uint16_t)buf[pos] << 8) | buf[pos + 1];
    pos += 2 + cipher_len;

    /* Compression Methods */
    if (pos >= len) return false;
    uint8_t comp_len = buf[pos];
    pos += 1 + comp_len;

    /* Extensions length */
    if (pos + 2 > len) return false;
    uint16_t ext_total_len = ((uint16_t)buf[pos] << 8) | buf[pos + 1];
    pos += 2;

    size_t ext_end = pos + ext_total_len;
    if (ext_end > len) ext_end = len;

    while (pos + 4 <= ext_end) {
        uint16_t ext_type = ((uint16_t)buf[pos] << 8) | buf[pos + 1];
        uint16_t ext_data_len = ((uint16_t)buf[pos + 2] << 8) | buf[pos + 3];
        pos += 4;

        if (ext_type == 0x0000) { /* Server Name Indication (SNI) */
            if (pos + 2 <= ext_end) {
                /* Server Name List Length (2) */
                /* Server Name Type (1) -> host_name = 0 */
                /* Server Name Length (2) */
                if (pos + 5 <= ext_end && buf[pos + 2] == 0x00) {
                    uint16_t name_len = ((uint16_t)buf[pos + 3] << 8) | buf[pos + 4];
                    if (offset) *offset = pos + 5;
                    if (sni_len) *sni_len = name_len;
                    return true;
                }
            }
        }
        pos += ext_data_len;
    }

    return false;
}

bool is_http_request(const uint8_t *buf, size_t len) {
    if (len < 10) return false;
    if (memcmp(buf, "GET ", 4) == 0 ||
        memcmp(buf, "POST ", 5) == 0 ||
        memcmp(buf, "HEAD ", 5) == 0 ||
        memcmp(buf, "PUT ", 4) == 0 ||
        memcmp(buf, "CONNECT ", 8) == 0) {
        return true;
    }
    return false;
}

static void send_fake_packet(int sockfd, int fake_ttl, const char *fake_host) {
    int orig_ttl = 64;
    socklen_t optlen = sizeof(orig_ttl);

    /* Get current TTL */
    getsockopt(sockfd, IPPROTO_IP, IP_TTL, &orig_ttl, &optlen);

    /* Set low TTL so packet expires at ISP middlebox */
    int low_ttl = (fake_ttl > 0 && fake_ttl < 30) ? fake_ttl : 4;
    setsockopt(sockfd, IPPROTO_IP, IP_TTL, &low_ttl, sizeof(low_ttl));

    /* Dummy TLS ClientHello targeting innocent domain */
    char dummy_payload[128];
    snprintf(dummy_payload, sizeof(dummy_payload),
             "GET / HTTP/1.1\r\nHost: %s\r\nUser-Agent: Mozilla/5.0\r\n\r\n",
             (fake_host && fake_host[0]) ? fake_host : "www.google.com");
    
    send(sockfd, dummy_payload, strlen(dummy_payload), MSG_NOSIGNAL);

    /* Small delay (1ms) to ensure middlebox processes fake packet first */
    usleep(1500);

    /* Restore original normal TTL */
    setsockopt(sockfd, IPPROTO_IP, IP_TTL, &orig_ttl, sizeof(orig_ttl));
}

ssize_t desync_send_payload(int sockfd, const uint8_t *buf, size_t len, const desync_config_t *cfg) {
    if (!cfg || cfg->mode == DESYNC_NONE || len == 0) {
        return send(sockfd, buf, len, MSG_NOSIGNAL);
    }

    /* Disable Nagle's algorithm so segments are sent immediately without coalescing */
    int flag = 1;
    setsockopt(sockfd, IPPROTO_TCP, TCP_NODELAY, &flag, sizeof(flag));

    bool is_tls = is_tls_client_hello(buf, len);
    bool is_http = is_http_request(buf, len);

    if (!is_tls && !is_http) {
        /* Not a handshake/HTTP request, send normally */
        return send(sockfd, buf, len, MSG_NOSIGNAL);
    }

    LOGD("Applying DPI evasion for %s (len: %zu, mode: %d)",
         is_tls ? "TLS ClientHello" : "HTTP Request", len, cfg->mode);

    /* 1. Fake Packet Injection (if enabled) */
    if (cfg->fake_data || cfg->mode == DESYNC_FAKE || cfg->mode == DESYNC_FULL_COMBO) {
        send_fake_packet(sockfd, cfg->fake_ttl, cfg->fake_host);
    }

    /* 2. Calculate split position */
    size_t split_pos = 2; // Default: 2 bytes split
    if (cfg->split_offset > 0 && (size_t)cfg->split_offset < len) {
        split_pos = (size_t)cfg->split_offset;
    } else if (is_tls) {
        size_t sni_offset = 0, sni_len = 0;
        if (find_sni_offset(buf, len, &sni_offset, &sni_len)) {
            /* Split right in the middle of SNI domain */
            if (sni_offset + 1 < len) {
                split_pos = sni_offset + 1;
            }
        }
    }

    if (split_pos >= len) split_pos = 1;

    /* 3. Send in fragmented chunks */
    ssize_t total_sent = 0;

    if (cfg->disorder || cfg->mode == DESYNC_DISORDER || cfg->mode == DESYNC_FULL_COMBO) {
        /* Send 1st segment */
        ssize_t s1 = send(sockfd, buf, split_pos, MSG_NOSIGNAL);
        if (s1 <= 0) return s1;
        total_sent += s1;

        /* Force small gap between packets (1-2 ms) so DPI middlebox treats them as distinct TCP segments */
        usleep(2000);

        /* Send remaining segments in smaller chunks */
        size_t remain = len - split_pos;
        const uint8_t *rem_buf = buf + split_pos;
        while (remain > 0) {
            size_t chunk = (remain > 64) ? 64 : remain;
            ssize_t s2 = send(sockfd, rem_buf, chunk, MSG_NOSIGNAL);
            if (s2 <= 0) break;
            rem_buf += s2;
            remain -= s2;
            total_sent += s2;
            if (remain > 0) usleep(1000);
        }
    } else {
        /* Standard split mode */
        ssize_t s1 = send(sockfd, buf, split_pos, MSG_NOSIGNAL);
        if (s1 <= 0) return s1;
        total_sent += s1;

        usleep(1500);

        ssize_t s2 = send(sockfd, buf + split_pos, len - split_pos, MSG_NOSIGNAL);
        if (s2 > 0) total_sent += s2;
    }

    return total_sent;
}
