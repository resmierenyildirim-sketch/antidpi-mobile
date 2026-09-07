#define _GNU_SOURCE
#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <android/log.h>

#include "ciadpi/desync.h"
#include "ciadpi/conntrack.h"
#include "ciadpi/proxy.h"
#include "tun2socks/tun2socks.h"

#define LOG_TAG "AntiDPI-JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static JavaVM *g_jvm = NULL;
static jobject g_vpn_service_obj = NULL;
static jmethodID g_protect_method = NULL;

static int jni_protect_socket(int sockfd) {
    if (!g_jvm || !g_vpn_service_obj || !g_protect_method) {
        return 0;
    }

    JNIEnv *env = NULL;
    int need_detach = 0;
    jint res = (*g_jvm)->GetEnv(g_jvm, (void **)&env, JNI_VERSION_1_6);
    if (res == JNI_EDETACHED) {
        if ((*g_jvm)->AttachCurrentThread(g_jvm, &env, NULL) != 0) {
            return -1;
        }
        need_detach = 1;
    }

    jboolean ret = (*env)->CallBooleanMethod(env, g_vpn_service_obj, g_protect_method, (jint)sockfd);

    if (need_detach) {
        (*g_jvm)->DetachCurrentThread(g_jvm);
    }

    return (ret == JNI_TRUE) ? 0 : -1;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    (void)reserved;
    g_jvm = vm;
    LOGI("AntiDPI JNI library loaded successfully.");
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL
Java_com_antidpi_mobile_core_NativeBridge_nativeRegisterVpnService(
        JNIEnv *env, jobject thiz, jobject vpnService) {
    (void)thiz;
    if (g_vpn_service_obj) {
        (*env)->DeleteGlobalRef(env, g_vpn_service_obj);
        g_vpn_service_obj = NULL;
    }
    if (vpnService) {
        g_vpn_service_obj = (*env)->NewGlobalRef(env, vpnService);
        jclass clazz = (*env)->GetObjectClass(env, vpnService);
        g_protect_method = (*env)->GetMethodID(env, clazz, "protect", "(I)Z");
        LOGI("VpnService registered with protect method id: %p", g_protect_method);
    }
}

JNIEXPORT jint JNICALL
Java_com_antidpi_mobile_core_NativeBridge_nativeStartEngine(
        JNIEnv *env, jobject thiz,
        jint tun_fd,
        jint socks_port,
        jint mode,
        jint split_offset,
        jint fake_ttl,
        jboolean disorder,
        jboolean fake_data,
        jstring fake_host) {
    (void)thiz;

    proxy_config_t pcfg;
    memset(&pcfg, 0, sizeof(pcfg));
    pcfg.listen_port = (socks_port > 0) ? socks_port : 1080;
    pcfg.protect_fn = jni_protect_socket;

    pcfg.desync_cfg.mode = (desync_mode_t)mode;
    pcfg.desync_cfg.split_offset = (int)split_offset;
    pcfg.desync_cfg.fake_ttl = (fake_ttl > 0) ? (int)fake_ttl : 4;
    pcfg.desync_cfg.disorder = (disorder == JNI_TRUE);
    pcfg.desync_cfg.fake_data = (fake_data == JNI_TRUE);
    pcfg.desync_cfg.mod_http = true;

    if (fake_host) {
        const char *host_str = (*env)->GetStringUTFChars(env, fake_host, NULL);
        if (host_str) {
            strncpy(pcfg.desync_cfg.fake_host, host_str, sizeof(pcfg.desync_cfg.fake_host) - 1);
            (*env)->ReleaseStringUTFChars(env, fake_host, host_str);
        }
    } else {
        strcpy(pcfg.desync_cfg.fake_host, "www.google.com");
    }

    if (proxy_start(&pcfg) != 0) {
        LOGE("Failed to start DPI proxy");
        return -1;
    }

    if (tun_fd >= 0) {
        tun2socks_config_t tcfg;
        tcfg.tun_fd = tun_fd;
        tcfg.socks_port = pcfg.listen_port;
        tcfg.socks_ip = "127.0.0.1";
        tun2socks_start(&tcfg);
    }

    LOGI("AntiDPI native engine started (port=%d, mode=%d, split=%d, ttl=%d)",
         pcfg.listen_port, mode, split_offset, pcfg.desync_cfg.fake_ttl);
    return 0;
}

JNIEXPORT void JNICALL
Java_com_antidpi_mobile_core_NativeBridge_nativeStopEngine(JNIEnv *env, jobject thiz) {
    (void)env;
    (void)thiz;
    tun2socks_stop();
    proxy_stop();
    LOGI("AntiDPI native engine stopped.");
}

JNIEXPORT jboolean JNICALL
Java_com_antidpi_mobile_core_NativeBridge_nativeIsRunning(JNIEnv *env, jobject thiz) {
    (void)env;
    (void)thiz;
    return (proxy_is_running() || tun2socks_is_running()) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jlongArray JNICALL
Java_com_antidpi_mobile_core_NativeBridge_nativeGetStats(JNIEnv *env, jobject thiz) {
    (void)thiz;
    conntrack_stats_t stats;
    conntrack_get_stats(&stats);

    jlongArray result = (*env)->NewLongArray(env, 4);
    if (!result) return NULL;

    jlong arr[4];
    arr[0] = (jlong)stats.bytes_in;
    arr[1] = (jlong)stats.bytes_out;
    arr[2] = (jlong)stats.packets_desynced;
    arr[3] = (jlong)stats.active_connections;

    (*env)->SetLongArrayRegion(env, result, 0, 4, arr);
    return result;
}
