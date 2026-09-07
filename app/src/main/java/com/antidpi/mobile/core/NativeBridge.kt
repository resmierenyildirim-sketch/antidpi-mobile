package com.antidpi.mobile.core

import android.util.Log

object NativeBridge {
    private const val TAG = "AntiDPI-NativeBridge"

    init {
        try {
            System.loadLibrary("antidpi")
            Log.i(TAG, "libantidpi.so loaded successfully.")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load libantidpi.so", e)
        }
    }

    external fun nativeRegisterVpnService(vpnService: Any)
    external fun nativeStartEngine(
        tunFd: Int,
        socksPort: Int,
        mode: Int,
        splitOffset: Int,
        fakeTtl: Int,
        disorder: Boolean,
        fakeData: Boolean,
        fakeHost: String
    ): Int
    external fun nativeStopEngine()
    external fun nativeIsRunning(): Boolean
    external fun nativeGetStats(): LongArray?
}
