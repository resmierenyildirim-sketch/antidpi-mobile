package com.antidpi.mobile.core

import android.content.Context
import com.antidpi.mobile.data.NetworkStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING
}

object DpiEngineManager {

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _stats = MutableStateFlow(NetworkStats())
    val stats: StateFlow<NetworkStats> = _stats.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(listOf("AntiDPI Mobile hazır."))
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    fun setConnectionState(state: ConnectionState) {
        _connectionState.value = state
        _isConnected.value = (state == ConnectionState.CONNECTED)
        when (state) {
            ConnectionState.CONNECTED -> addLog("DPI koruması aktif.")
            ConnectionState.DISCONNECTED -> addLog("Bağlantı kapatıldı.")
            ConnectionState.CONNECTING -> addLog("Bağlanıyor...")
            ConnectionState.DISCONNECTING -> addLog("Bağlantı kesiliyor...")
        }
    }

    fun setConnected(connected: Boolean) {
        setConnectionState(if (connected) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED)
    }

    fun updateStats(newStats: NetworkStats) {
        _stats.value = newStats
    }

    fun addLog(message: String) {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timestamp = timeFormat.format(Date())
        val logEntry = "[$timestamp] $message"
        val current = _logs.value.toMutableList()
        current.add(0, logEntry) // Prepend newest
        if (current.size > 200) {
            current.removeAt(current.size - 1)
        }
        _logs.value = current
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun toggle(context: Context) {
        if (_connectionState.value == ConnectionState.CONNECTING || _connectionState.value == ConnectionState.DISCONNECTING) {
            return
        }
        if (_isConnected.value) {
            DpiVpnService.stop(context)
        } else {
            DpiVpnService.start(context)
        }
    }
}
