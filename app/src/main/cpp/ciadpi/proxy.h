#ifndef ANTIDPI_PROXY_H
#define ANTIDPI_PROXY_H

#include "desync.h"
#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef int (*socket_protect_fn)(int sockfd);

typedef struct {
    int listen_port;
    desync_config_t desync_cfg;
    socket_protect_fn protect_fn;
} proxy_config_t;

int proxy_start(const proxy_config_t *cfg);
void proxy_stop(void);
bool proxy_is_running(void);

#ifdef __cplusplus
}
#endif

#endif /* ANTIDPI_PROXY_H */
