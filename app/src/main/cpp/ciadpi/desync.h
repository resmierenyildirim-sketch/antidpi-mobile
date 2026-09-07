#ifndef ANTIDPI_DESYNC_H
#define ANTIDPI_DESYNC_H

#include <stddef.h>
#include <stdint.h>
#include <sys/types.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef enum {
    DESYNC_NONE = 0,
    DESYNC_SPLIT = 1,
    DESYNC_DISORDER = 2,
    DESYNC_FAKE = 3,
    DESYNC_FULL_COMBO = 4
} desync_mode_t;

typedef struct {
    desync_mode_t mode;
    int split_offset;       /* e.g. 1, 2, or 0 for SNI offset */
    int fake_ttl;           /* Hop limit for fake packet (e.g. 3 - 8) */
    bool disorder;          /* Send 2nd segment before 1st segment */
    bool fake_data;         /* Inject fake packet with fake TTL before payload */
    bool mod_http;          /* Modify HTTP host header */
    char fake_host[128];    /* Fake domain to confuse DPI */
} desync_config_t;

/* Protocol detection */
bool is_tls_client_hello(const uint8_t *buf, size_t len);
bool find_sni_offset(const uint8_t *buf, size_t len, size_t *offset, size_t *sni_len);
bool is_http_request(const uint8_t *buf, size_t len);

/* Desynchronization transmission */
ssize_t desync_send_payload(int sockfd, const uint8_t *buf, size_t len, const desync_config_t *cfg);

#ifdef __cplusplus
}
#endif

#endif /* ANTIDPI_DESYNC_H */
