#include "conntrack.h"
#include <string.h>

static conntrack_stats_t g_stats;
static pthread_mutex_t g_stats_mutex = PTHREAD_MUTEX_INITIALIZER;

void conntrack_init(void) {
    pthread_mutex_lock(&g_stats_mutex);
    memset(&g_stats, 0, sizeof(g_stats));
    pthread_mutex_unlock(&g_stats_mutex);
}

void conntrack_cleanup(void) {
    pthread_mutex_lock(&g_stats_mutex);
    memset(&g_stats, 0, sizeof(g_stats));
    pthread_mutex_unlock(&g_stats_mutex);
}

void conntrack_add_connection(void) {
    pthread_mutex_lock(&g_stats_mutex);
    g_stats.active_connections++;
    pthread_mutex_unlock(&g_stats_mutex);
}

void conntrack_remove_connection(void) {
    pthread_mutex_lock(&g_stats_mutex);
    if (g_stats.active_connections > 0) {
        g_stats.active_connections--;
    }
    pthread_mutex_unlock(&g_stats_mutex);
}

void conntrack_add_bytes_in(uint64_t bytes) {
    pthread_mutex_lock(&g_stats_mutex);
    g_stats.bytes_in += bytes;
    pthread_mutex_unlock(&g_stats_mutex);
}

void conntrack_add_bytes_out(uint64_t bytes) {
    pthread_mutex_lock(&g_stats_mutex);
    g_stats.bytes_out += bytes;
    pthread_mutex_unlock(&g_stats_mutex);
}

void conntrack_inc_desynced(void) {
    pthread_mutex_lock(&g_stats_mutex);
    g_stats.packets_desynced++;
    pthread_mutex_unlock(&g_stats_mutex);
}

void conntrack_get_stats(conntrack_stats_t *out_stats) {
    if (!out_stats) return;
    pthread_mutex_lock(&g_stats_mutex);
    *out_stats = g_stats;
    pthread_mutex_unlock(&g_stats_mutex);
}

void conntrack_reset_stats(void) {
    pthread_mutex_lock(&g_stats_mutex);
    memset(&g_stats, 0, sizeof(g_stats));
    pthread_mutex_unlock(&g_stats_mutex);
}
