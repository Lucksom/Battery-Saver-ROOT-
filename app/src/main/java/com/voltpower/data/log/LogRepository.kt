package com.voltpower.data.log

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogCategory(val label: String) {
    ROOT("ROOT"),
    TWEAK("TWEAK"),
    PROFILE("PROFILE"),
    TELEMETRY("TELEMETRY"),
    ERROR("ERROR")
}

data class LogEntry(
    val id: Long,
    val timestamp: String,
    val category: LogCategory,
    val message: String,
    val command: String? = null,
    val paramName: String = "",
    val paramValue: String = "",
    val isSuccess: Boolean = true
) {
    val displayName: String
        get() = message.ifBlank { paramName.ifBlank { category.label } }
}

object LogRepository {
    private const val MAX_LOGS = 250
    private var nextId = 1L

    // Console starts completely empty — only real applied tweaks are logged
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun addLog(
        category: LogCategory,
        message: String,
        command: String? = null,
        paramName: String = "",
        paramValue: String = "",
        isSuccess: Boolean = true
    ) {
        val cleanMessage = message.trim()
        if (cleanMessage.isEmpty()) return

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
        val entry = LogEntry(
            id = nextId++,
            timestamp = timeFormat.format(Date()),
            category = category,
            message = cleanMessage,
            command = null, // Never store or expose raw shell commands in UI console
            paramName = paramName,
            paramValue = paramValue,
            isSuccess = isSuccess
        )

        val current = _logs.value.toMutableList()
        current.add(0, entry) // Newest first
        if (current.size > MAX_LOGS) {
            current.removeAt(current.lastIndex)
        }
        _logs.value = current
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
}
