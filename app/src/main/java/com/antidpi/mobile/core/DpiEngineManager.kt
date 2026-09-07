package com.antidpi.mobile.core

import android.content.Context
import com.antidpi.mobile.data.NetworkStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DpiEngineManager {

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _stats = MutableStateFlow(NetworkStats())
    val stats: StateFlow<NetworkStats> = _stats.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(listOf("AntiDPI Mobile başlatıldı ve hazır."))
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
        if (connected) {
            addLog("Yerel DPI VPN tüneli kuruldu. Paketler manipüle ediliyor.")
        } else {
            addLog("Bağlantı kapatıldı.")
        }
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
        if (_isConnected.value) {
            DpiVpnService.stop(context)
        } else {
            DpiVpnService.start(context)
        }
    }
}
