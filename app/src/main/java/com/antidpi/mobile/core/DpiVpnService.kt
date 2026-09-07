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
import io.github.dovecoteescapee.byedpi.core.ByeDpiProxy
import io.github.dovecoteescapee.byedpi.core.TProxyService
import kotlinx.coroutines.*
import java.io.File

class DpiVpnService : VpnService() {

    private val byeDpiProxy = ByeDpiProxy()
    private var proxyJob: Job? = null
    private var vpnInterface: ParcelFileDescriptor? = null
    private var configFile: File? = null

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
            val port = 1080

            // 1. Start ByeDPI SOCKS5 Proxy
            val args = profile.toArgs(ip = "127.0.0.1", port = port)
            val socketFd = byeDpiProxy.createSocket(args)
            if (socketFd < 0) {
                Log.e(TAG, "Failed to create ByeDPI socket")
                DpiEngineManager.setConnected(false)
                return
            }

            proxyJob = serviceScope.launch(Dispatchers.IO) {
                val code = byeDpiProxy.startProxy(socketFd)
                Log.i(TAG, "ByeDpi proxy exited with code: $code")
            }

            // Small delay to ensure proxy is listening
            Thread.sleep(100)

            // 2. Build Android TUN Interface
            val builder = Builder()
                .setSession(getString(R.string.app_name))
                .setMtu(8500)
                .addAddress("10.10.10.10", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("1.1.1.1")
                .addDnsServer("8.8.8.8")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setMetered(false)
            }

            // Exclude self package
            try {
                builder.addDisallowedApplication(packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                Log.w(TAG, "Failed to disallow self package", e)
            }

            // Exclude user-selected apps
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
                stopVpn()
                return
            }

            val tunFd = vpnInterface!!.fd

            // 3. Create YAML config for hev-socks5-tunnel
            val tun2socksConfig = """
            misc:
              task-stack-size: 81920
            socks5:
              mtu: 8500
              address: 127.0.0.1
              port: $port
              udp: udp
            """.trimIndent()

            val tempFile = File.createTempFile("tun_config", ".yaml", cacheDir)
            tempFile.writeText(tun2socksConfig)
            this.configFile = tempFile

            // 4. Start hev-socks5-tunnel with lwIP IP stack
            TProxyService.TProxyStartService(tempFile.absolutePath, tunFd)

            connectionStartTime = System.currentTimeMillis()
            DpiEngineManager.setConnected(true)
            startForeground(NOTIFICATION_ID, buildNotification("AntiDPI Aktif - Sıfır Hız Kaybı"))

            startStatsPolling()
            Log.i(TAG, "DpiVpnService successfully started with ByeDPI & hev-socks5-tunnel! Profile: ${profile.name}")

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
                try {
                    val rawStats = TProxyService.TProxyGetStats()
                    if (rawStats.size >= 4) {
                        val txPackets = rawStats[0]
                        val txBytes = rawStats[1]
                        val rxPackets = rawStats[2]
                        val rxBytes = rawStats[3]

                        val speedIn = if (lastBytesIn > 0 && rxBytes >= lastBytesIn) rxBytes - lastBytesIn else 0L
                        val speedOut = if (lastBytesOut > 0 && txBytes >= lastBytesOut) txBytes - lastBytesOut else 0L

                        lastBytesIn = rxBytes
                        lastBytesOut = txBytes

                        val duration = System.currentTimeMillis() - connectionStartTime

                        val currentStats = NetworkStats(
                            bytesIn = rxBytes,
                            bytesOut = txBytes,
                            packetsDesynced = txPackets,
                            activeConnections = 1,
                            downloadSpeedBps = speedIn,
                            uploadSpeedBps = speedOut,
                            connectionDurationMs = duration
                        )
                        DpiEngineManager.updateStats(currentStats)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to get stats", e)
                }
            }
        }
    }

    private fun stopVpn() {
        statsJob?.cancel()
        statsJob = null

        try {
            TProxyService.TProxyStopService()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping TProxyService", e)
        }

        try {
            byeDpiProxy.stopProxy()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping ByeDPI proxy", e)
        }

        proxyJob?.cancel()
        proxyJob = null

        try {
            configFile?.delete()
        } catch (e: Exception) {
            Log.w(TAG, "Error deleting config file", e)
        }
        configFile = null

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
