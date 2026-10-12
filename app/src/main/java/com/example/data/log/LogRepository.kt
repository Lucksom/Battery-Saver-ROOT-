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
    val paramName: String? = null,
    val paramValue: String? = null,
    val isSuccess: Boolean = true
) {
    val displayName: String
        get() = paramName ?: when {
            message.contains(":") -> message.substringBefore(":").trim()
            message.contains("=") -> message.substringBefore("=").trim()
            else -> message
        }

    val displayValue: String
        get() = paramValue ?: when {
            command != null && command.contains("=") -> command.substringAfter("=").trim()
            message.contains(":") -> message.substringAfter(":").trim()
            message.contains("=") -> message.substringAfter("=").trim()
            command != null -> command
            else -> if (isSuccess) "Active (OK)" else "Disabled"
        }
}

object LogRepository {
    private val counter = AtomicLong(1)
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val _logs = MutableStateFlow<List<LogEntry>>(
        listOf(
            LogEntry(
                id = counter.getAndIncrement(),
                timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(System.currentTimeMillis() - 14000)),
                category = LogCategory.SYSTEM,
                message = "Kernel Engine Initialization",
                paramName = "VoltPower Core Engine",
                paramValue = "Mounted in volatile RAM",
                command = "mount -t tmpfs tmpfs /dev/voltpower",
                isSuccess = true
            ),
            LogEntry(
                id = counter.getAndIncrement(),
                timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(System.currentTimeMillis() - 10000)),
                category = LogCategory.TWEAK,
                message = "Display Refresh Rate",
                paramName = "peak_refresh_rate",
                paramValue = "120 Hz Dynamic",
                command = "settings put system peak_refresh_rate 120.0",
                isSuccess = true
            ),
            LogEntry(
                id = counter.getAndIncrement(),
                timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(System.currentTimeMillis() - 7000)),
                category = LogCategory.TWEAK,
                message = "CPU Frequency Governor",
                paramName = "scaling_governor",
                paramValue = "schedutil",
                command = "echo schedutil > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
                isSuccess = true
            ),
            LogEntry(
                id = counter.getAndIncrement(),
                timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(System.currentTimeMillis() - 4000)),
                category = LogCategory.TWEAK,
                message = "Control Center Blur",
                paramName = "disable_window_blurs",
                paramValue = "1 (Removed Blur)",
                command = "settings put global disable_window_blurs 1",
                isSuccess = true
            ),
            LogEntry(
                id = counter.getAndIncrement(),
                timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(System.currentTimeMillis() - 1500)),
                category = LogCategory.SYSTEM,
                message = "Battery Sysfs Stream",
                paramName = "power_supply/battery",
                paramValue = "Hardware telemetry active",
                command = "cat /sys/class/power_supply/battery/uevent",
                isSuccess = true
            )
        )
    )
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun addLog(
        category: LogCategory,
        message: String,
        command: String? = null,
        paramName: String? = null,
        paramValue: String? = null,
        isSuccess: Boolean = true
    ) {
        val entry = LogEntry(
            id = counter.getAndIncrement(),
            timestamp = timeFormat.format(Date()),
            category = category,
            message = message,
            command = command,
            paramName = paramName,
            paramValue = paramValue,
            isSuccess = isSuccess
        )
        val current = _logs.value.toMutableList()
        current.add(0, entry)
        if (current.size > 300) {
            current.removeAt(current.lastIndex)
        }
        _logs.value = current
    }

    fun addLog(
        category: LogCategory,
        message: String,
        command: String? = null,
        isSuccess: Boolean = true
    ) {
        addLog(
            category = category,
            message = message,
            command = command,
            paramName = null,
            paramValue = null,
            isSuccess = isSuccess
        )
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
}
