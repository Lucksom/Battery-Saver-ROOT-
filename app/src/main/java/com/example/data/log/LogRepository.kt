package com.example.data.log

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

enum class LogCategory {
    ROOT,
    TWEAK,
    PROFILE,
    SYSTEM,
    ERROR
}

data class LogEntry(
    val id: Long,
    val timestamp: String,
    val category: LogCategory,
    val message: String,
    val command: String? = null,
    val isSuccess: Boolean = true
)

object LogRepository {
    private val counter = AtomicLong(1)
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val _logs = MutableStateFlow<List<LogEntry>>(
        listOf(
            LogEntry(
                id = counter.getAndIncrement(),
                timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(System.currentTimeMillis() - 12000)),
                category = LogCategory.SYSTEM,
                message = "VoltPower core engine initialized in volatile RAM",
                command = null,
                isSuccess = true
            ),
            LogEntry(
                id = counter.getAndIncrement(),
                timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(System.currentTimeMillis() - 8000)),
                category = LogCategory.ROOT,
                message = "Root privilege verified: uid=0(root)",
                command = "su -c 'id'",
                isSuccess = true
            ),
            LogEntry(
                id = counter.getAndIncrement(),
                timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(System.currentTimeMillis() - 5000)),
                category = LogCategory.SYSTEM,
                message = "Hardware telemetry stream mounted: /sys/class/power_supply/battery",
                command = null,
                isSuccess = true
            ),
            LogEntry(
                id = counter.getAndIncrement(),
                timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(System.currentTimeMillis() - 2000)),
                category = LogCategory.TWEAK,
                message = "Display refresh rate synced with system settings",
                command = "settings get system user_refresh_rate",
                isSuccess = true
            ),
            LogEntry(
                id = counter.getAndIncrement(),
                timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
                category = LogCategory.TWEAK,
                message = "Control center blur engine verified",
                command = "settings get global disable_window_blurs",
                isSuccess = true
            )
        )
    )
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun addLog(
        category: LogCategory,
        message: String,
        command: String? = null,
        isSuccess: Boolean = true
    ) {
        val entry = LogEntry(
            id = counter.getAndIncrement(),
            timestamp = timeFormat.format(Date()),
            category = category,
            message = message,
            command = command,
            isSuccess = isSuccess
        )
        val current = _logs.value.toMutableList()
        current.add(0, entry)
        if (current.size > 300) {
            current.removeAt(current.lastIndex)
        }
        _logs.value = current
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
}
