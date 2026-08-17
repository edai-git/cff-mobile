package com.example.myapp.server

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val timestamp: String,
    val method: String,
    val path: String,
    val statusCode: Int,
    val sizeBytes: Long,
    val durationMs: Long,
    val clientIp: String
)

data class ServerStatus(
    val isRunning: Boolean = false,
    val port: Int = 8080,
    val urls: List<String> = emptyList(),
    val logs: List<LogEntry> = emptyList(),
    val totalRequests: Int = 0,
    val totalBytes: Long = 0,
    val startedAt: Long? = null,
    val errorMessage: String? = null
)

object ServerState {
    private val _status = MutableStateFlow(ServerStatus())
    val status: StateFlow<ServerStatus> = _status.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun setRunning(isRunning: Boolean, port: Int, urls: List<String> = emptyList(), error: String? = null) {
        _status.value = _status.value.copy(
            isRunning = isRunning,
            port = port,
            urls = urls,
            startedAt = if (isRunning) System.currentTimeMillis() else null,
            errorMessage = error
        )
    }

    fun addLog(method: String, path: String, statusCode: Int, sizeBytes: Long, durationMs: Long, clientIp: String) {
        val entry = LogEntry(
            timestamp = timeFormat.format(Date()),
            method = method,
            path = path,
            statusCode = statusCode,
            sizeBytes = sizeBytes,
            durationMs = durationMs,
            clientIp = clientIp
        )
        val currentLogs = _status.value.logs.take(99).toMutableList()
        currentLogs.add(0, entry)

        _status.value = _status.value.copy(
            logs = currentLogs,
            totalRequests = _status.value.totalRequests + 1,
            totalBytes = _status.value.totalBytes + sizeBytes
        )
    }

    fun clearLogs() {
        _status.value = _status.value.copy(
            logs = emptyList(),
            totalRequests = 0,
            totalBytes = 0
        )
    }

    fun updateUrls(urls: List<String>) {
        _status.value = _status.value.copy(urls = urls)
    }
}
