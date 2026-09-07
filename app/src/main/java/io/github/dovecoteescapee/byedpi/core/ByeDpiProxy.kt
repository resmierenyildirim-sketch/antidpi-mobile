package io.github.dovecoteescapee.byedpi.core

import android.util.Log

class ByeDpiProxy {
    companion object {
        private const val TAG = "ByeDpiProxy"
        init {
            try {
                System.loadLibrary("byedpi")
                Log.i(TAG, "libbyedpi.so loaded successfully.")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load libbyedpi.so", e)
            }
        }
    }

    @Volatile
    private var fd = -1

    fun createSocket(args: Array<String>): Int {
        Log.i(TAG, "Creating proxy socket with args: ${args.joinToString(" ")}")
        val socketFd = jniCreateSocketWithCommandLine(args)
        this.fd = socketFd
        return socketFd
    }

    fun startProxy(socketFd: Int): Int {
        Log.i(TAG, "Starting proxy on socket fd: $socketFd")
        return jniStartProxy(socketFd)
    }

    fun stopProxy(): Int {
        val currentFd = fd
        if (currentFd >= 0) {
            Log.i(TAG, "Stopping proxy on socket fd: $currentFd")
            val res = jniStopProxy(currentFd)
            fd = -1
            return res
        }
        return 0
    }

    private external fun jniCreateSocketWithCommandLine(args: Array<String>): Int
    private external fun jniStartProxy(fd: Int): Int
    private external fun jniStopProxy(fd: Int): Int
}
