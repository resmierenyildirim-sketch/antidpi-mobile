#ifndef ANTIDPI_TUN2SOCKS_H
#define ANTIDPI_TUN2SOCKS_H

#include <stdbool.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    int tun_fd;
    int socks_port;
    const char *socks_ip;
} tun2socks_config_t;

int tun2socks_start(const tun2socks_config_t *cfg);
void tun2socks_stop(void);
bool tun2socks_is_running(void);

#ifdef __cplusplus
}
#endif

#endif /* ANTIDPI_TUN2SOCKS_H */
