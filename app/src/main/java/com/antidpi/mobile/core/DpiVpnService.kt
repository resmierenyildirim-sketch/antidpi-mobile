package com.antidpi.mobile.core

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.antidpi.mobile.AntiDpiApp
import com.antidpi.mobile.R
import com.antidpi.mobile.data.NetworkStats
import com.antidpi.mobile.ui.MainActivity
import kotlinx.coroutines.*

class DpiVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var statsJob: Job? = null

    private var lastBytesIn = 0L
    private var lastBytesOut = 0L
    private var connectionStartTime = 0L

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "DpiVpnService created.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_CONNECT
        when (action) {
            ACTION_CONNECT -> {
                startVpn()
            }
            ACTION_DISCONNECT -> {
                stopVpn()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startVpn() {
        if (DpiEngineManager.isConnected.value) {
            Log.i(TAG, "VPN is already running.")
            return
        }

        try {
            val prefs = (application as AntiDpiApp).preferencesManager
            val profile = prefs.getActiveProfile()

            val builder = Builder()
                .setSession(getString(R.string.app_name))
                .setMtu(1500)
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("1.1.1.1")
                .addDnsServer("8.8.8.8")

            // Exclude our own app from the VPN tunnel to prevent recursion
            try {
                builder.addDisallowedApplication(packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                Log.w(TAG, "Failed to disallow self package", e)
            }

            // Exclude user-selected apps (e.g. banking apps)
            for (appPkg in prefs.excludedApps) {
                try {
                    builder.addDisallowedApplication(appPkg)
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.w(TAG, "Package not found for exclusion: $appPkg")
                }
            }

            builder.setBlocking(false)
            vpnInterface = builder.establish()

            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface (null fd)")
                DpiEngineManager.setConnected(false)
                return
            }

            val tunFd = vpnInterface!!.fd

            // Register this VpnService with native bridge so it can call protect(socketFd)
            NativeBridge.nativeRegisterVpnService(this)

            // Start native DPI bypass engine
            val res = NativeBridge.nativeStartEngine(
                tunFd = tunFd,
                socksPort = 1080,
                mode = profile.mode,
                splitOffset = profile.splitOffset,
                fakeTtl = profile.fakeTtl,
                disorder = profile.disorder,
                fakeData = profile.fakeData,
                fakeHost = profile.fakeHost
            )

            if (res != 0) {
                Log.e(TAG, "Native engine failed to start with code: $res")
                stopVpn()
                return
            }

            connectionStartTime = System.currentTimeMillis()
            DpiEngineManager.setConnected(true)
            startForeground(NOTIFICATION_ID, buildNotification("DPI Koruması Aktif - Hız Kısıtlaması Yok"))

            startStatsPolling()
            Log.i(TAG, "DpiVpnService successfully started with profile: ${profile.name}")

        } catch (e: Exception) {
            Log.e(TAG, "Exception starting DpiVpnService", e)
            stopVpn()
        }
    }

    private fun startStatsPolling() {
        statsJob?.cancel()
        statsJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                val rawStats = NativeBridge.nativeGetStats()
                if (rawStats != null && rawStats.size >= 4) {
                    val bytesIn = rawStats[0]
                    val bytesOut = rawStats[1]
                    val packetsDesynced = rawStats[2]
                    val activeConn = rawStats[3].toInt()

                    val speedIn = if (lastBytesIn > 0 && bytesIn >= lastBytesIn) bytesIn - lastBytesIn else 0L
                    val speedOut = if (lastBytesOut > 0 && bytesOut >= lastBytesOut) bytesOut - lastBytesOut else 0L

                    lastBytesIn = bytesIn
                    lastBytesOut = bytesOut

                    val duration = System.currentTimeMillis() - connectionStartTime

                    val currentStats = NetworkStats(
                        bytesIn = bytesIn,
                        bytesOut = bytesOut,
                        packetsDesynced = packetsDesynced,
                        activeConnections = activeConn,
                        downloadSpeedBps = speedIn,
                        uploadSpeedBps = speedOut,
                        connectionDurationMs = duration
                    )
                    DpiEngineManager.updateStats(currentStats)
                }
            }
        }
    }

    private fun stopVpn() {
        statsJob?.cancel()
        statsJob = null

        NativeBridge.nativeStopEngine()

        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing vpnInterface", e)
        }
        vpnInterface = null

        DpiEngineManager.setConnected(false)
        DpiEngineManager.updateStats(NetworkStats())

        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.i(TAG, "DpiVpnService stopped.")
    }

    private fun buildNotification(contentText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = Intent(this, DpiVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val pendingDisconnect = PendingIntent.getService(
            this, 1, disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, AntiDpiApp.VPN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.status_protected))
            .setContentText(contentText)
            .setOngoing(true)
            .setContentIntent(pendingOpenApp)
            .addAction(R.drawable.ic_launcher_foreground, getString(R.string.btn_disconnect), pendingDisconnect)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    override fun onDestroy() {
        stopVpn()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val TAG = "AntiDPI-VpnService"
        const val ACTION_CONNECT = "com.antidpi.mobile.ACTION_CONNECT"
        const val ACTION_DISCONNECT = "com.antidpi.mobile.ACTION_DISCONNECT"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, DpiVpnService::class.java).apply {
                action = ACTION_CONNECT
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, DpiVpnService::class.java).apply {
                action = ACTION_DISCONNECT
            }
            context.startService(intent)
        }
    }
}
