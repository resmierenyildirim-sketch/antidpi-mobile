#ifndef ANTIDPI_CONNTRACK_H
#define ANTIDPI_CONNTRACK_H

#include <stdint.h>
#include <stdbool.h>
#include <pthread.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    uint64_t bytes_in;
    uint64_t bytes_out;
    uint64_t packets_desynced;
    uint32_t active_connections;
} conntrack_stats_t;

void conntrack_init(void);
void conntrack_cleanup(void);

void conntrack_add_connection(void);
void conntrack_remove_connection(void);

void conntrack_add_bytes_in(uint64_t bytes);
void conntrack_add_bytes_out(uint64_t bytes);
void conntrack_inc_desynced(void);

void conntrack_get_stats(conntrack_stats_t *out_stats);
void conntrack_reset_stats(void);

#ifdef __cplusplus
}
#endif

#endif /* ANTIDPI_CONNTRACK_H */
