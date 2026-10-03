package com.example.data.telemetry

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.data.root.RootBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class CpuCoreInfo(
    val coreIndex: Int,
    val isOnline: Boolean,
    val currentFreqMhz: Int,
    val maxFreqMhz: Int
)

data class DeviceTelemetry(
    val isRootGranted: Boolean,
    val batteryLevel: Int,
    val batteryTempC: Float,
    val batteryVoltageMv: Int,
    val isCharging: Boolean,
    val activeGovernor: String,
    val cpuCores: List<CpuCoreInfo>,
    val backupFileExists: Boolean
)

class TelemetryRepository(private val context: Context) {

    suspend fun getTelemetry(): DeviceTelemetry = withContext(Dispatchers.IO) {
        val rootOk = RootBridge.isRootAvailable()

        // Battery Telemetry
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryIntent = context.registerReceiver(null, batteryFilter)

        val level = batteryIntent?.let { intent ->
            val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (rawLevel >= 0 && scale > 0) (rawLevel * 100) / scale else 50
        } ?: 50

        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempC = tempTenths / 10.0f
        val voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // CPU Governor
        val gov = readGovernor()

        // CPU Cores Info
        val cores = mutableListOf<CpuCoreInfo>()
        val totalCores = Runtime.getRuntime().availableProcessors().coerceAtLeast(8)
        for (i in 0 until totalCores) {
            val isOnline = if (i == 0) true else {
                readCoreOnline(i)
            }
            val curFreq = readCoreCurrentFreq(i)
            val maxFreq = readCoreMaxFreq(i)
            cores.add(
                CpuCoreInfo(
                    coreIndex = i,
                    isOnline = isOnline,
                    currentFreqMhz = curFreq,
                    maxFreqMhz = maxFreq
                )
            )
        }

        val backupExists = File(RootBridge.BACKUP_FILE_PATH).exists() ||
                File(context.filesDir, "Stock_Kernel_Backup.sh").exists()

        DeviceTelemetry(
            isRootGranted = rootOk,
            batteryLevel = level,
            batteryTempC = tempC,
            batteryVoltageMv = voltage,
            isCharging = isCharging,
            activeGovernor = gov,
            cpuCores = cores,
            backupFileExists = backupExists
        )
    }

    private suspend fun readGovernor(): String {
        val file = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
        if (file.exists() && file.canRead()) {
            try {
                val txt = file.readText().trim()
                if (txt.isNotEmpty()) return txt
            } catch (_: Exception) {}
        }
        val rootVal = RootBridge.readNode("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
        if (!rootVal.isNullOrBlank()) {
            return rootVal
        }
        return "powersave"
    }

    private fun readCoreOnline(core: Int): Boolean {
        val file = File("/sys/devices/system/cpu/cpu$core/online")
        if (file.exists() && file.canRead()) {
            return try {
                file.readText().trim() == "1"
            } catch (_: Exception) {
                true
            }
        }
        return true
    }

    private fun readCoreCurrentFreq(core: Int): Int {
        val file = File("/sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq")
        if (file.exists() && file.canRead()) {
            return try {
                (file.readText().trim().toIntOrNull() ?: 0) / 1000
            } catch (_: Exception) {
                0
            }
        }
        return 0
    }

    private fun readCoreMaxFreq(core: Int): Int {
        val file = File("/sys/devices/system/cpu/cpu$core/cpufreq/scaling_max_freq")
        if (file.exists() && file.canRead()) {
            return try {
                (file.readText().trim().toIntOrNull() ?: 0) / 1000
            } catch (_: Exception) {
                0
            }
        }
        return 0
    }
}
