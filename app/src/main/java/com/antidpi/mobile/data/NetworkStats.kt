package com.antidpi.mobile.data

import java.util.Locale

data class NetworkStats(
    val bytesIn: Long = 0L,
    val bytesOut: Long = 0L,
    val packetsDesynced: Long = 0L,
    val activeConnections: Int = 0,
    val downloadSpeedBps: Long = 0L,
    val uploadSpeedBps: Long = 0L,
    val connectionDurationMs: Long = 0L
) {
    fun formatBytesIn(): String = formatBytes(bytesIn)
    fun formatBytesOut(): String = formatBytes(bytesOut)
    fun formatDownloadSpeed(): String = "${formatBytes(downloadSpeedBps)}/s"
    fun formatUploadSpeed(): String = "${formatBytes(uploadSpeedBps)}/s"

    fun formatDuration(): String {
        val totalSecs = connectionDurationMs / 1000
        val hours = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)
        } else {
            String.format(Locale.US, "%02d:%02d", mins, secs)
        }
    }

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes < 1024) return "$bytes B"
            val kb = bytes / 1024.0
            if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
            val mb = kb / 1024.0
            if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb)
            val gb = mb / 1024.0
            return String.format(Locale.US, "%.2f GB", gb)
        }
    }
}
