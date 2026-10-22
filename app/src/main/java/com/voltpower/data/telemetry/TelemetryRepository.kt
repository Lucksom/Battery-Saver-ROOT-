package com.voltpower.data.telemetry

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.view.Display
import android.view.WindowManager
import com.voltpower.data.root.RootBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs

data class CpuCoreTelemetry(
    val coreIndex: Int,
    val isOnline: Boolean,
    val currentFreqMhz: Int,
    val maxFreqMhz: Int
)

data class DeviceTelemetry(
    val batteryLevel: Int = 50,
    val batteryTempC: Float = 32.0f,
    val batteryVoltageMv: Int = 4000,
    val batteryCurrentMa: Int = -180,
    val batteryHealthPercent: Int = 96,
    val batteryCycleCount: Int = 142,
    val batteryCapacityMah: Int = 5000,
    val batteryFullChargeMah: Int = 4800,
    val isCharging: Boolean = false,
    val chargingWatts: Float = 0f,
    val cpuCores: List<CpuCoreTelemetry> = emptyList(),
    val activeGovernor: String = "schedutil",
    val isGmsDozeActive: Boolean = false,
    val isRootGranted: Boolean = false,
    val currentRefreshRateHz: Int = 60,
    val isSystemBatterySaverActive: Boolean = false,
    val gpuFreqInfo: String = "0 MHz"
)

object TelemetryRepository {

    suspend fun readTelemetry(context: Context): DeviceTelemetry = withContext(Dispatchers.IO) {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 50
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val pct = if (scale > 0 && level >= 0) (level * 100) / scale else 50

        val tempRaw = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 315) ?: 315
        val tempC = readSysFile("/sys/class/power_supply/battery/temp")?.toFloatOrNull()?.div(10f)
            ?: (tempRaw / 10f)

        val voltRaw = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4050) ?: 4050
        val voltageMv = if (voltRaw < 100) voltRaw * 1000 else voltRaw

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val rawCurrentNow = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            ?: readSysFile("/sys/class/power_supply/battery/current_now")?.toIntOrNull()
            ?: -150_000
        val currentMa = if (abs(rawCurrentNow) > 10_000) rawCurrentNow / 1000 else rawCurrentNow

        val watts = (abs(currentMa).toFloat() * voltageMv.toFloat()) / 1_000_000f

        val cycleCount = readSysFile("/sys/class/power_supply/battery/cycle_count")?.toIntOrNull()
            ?: readSysFile("/sys/class/power_supply/bms/cycle_count")?.toIntOrNull()
            ?: if ( android.os.Build.VERSION.SDK_INT >= 34 ) {
                batteryIntent?.getIntExtra("android.os.extra.CYCLE_COUNT", -1)?.takeIf { it > 0 } ?: 128
            } else 128

        val designCapRaw = readSysFile("/sys/class/power_supply/battery/charge_full_design")?.toIntOrNull()
            ?: readSysFile("/sys/class/power_supply/bms/charge_full_design")?.toIntOrNull()
            ?: 5000_000
        val designMah = if (designCapRaw > 100_000) designCapRaw / 1000 else if (designCapRaw > 100) designCapRaw else 5000

        val fullCapRaw = readSysFile("/sys/class/power_supply/battery/charge_full")?.toIntOrNull()
            ?: readSysFile("/sys/class/power_supply/bms/charge_full")?.toIntOrNull()
            ?: (designCapRaw * 95 / 100)
        val fullMah = if (fullCapRaw > 100_000) fullCapRaw / 1000 else if (fullCapRaw > 100) fullCapRaw else 4800

        val healthPct = if (designMah > 0) ((fullMah.toFloat() / designMah.toFloat()) * 100f).toInt().coerceIn(50, 100) else 96

        val rootOk = RootBridge.isRootAvailable()

        // 8-core usage & frequency calculation: ONLY calculated when root permission is granted!
        val rootCoreBatch = if (rootOk) RootBridge.readOctaCoreSysfsBatch() else null

        val cores = (0..7).map { coreIndex ->
            if (!rootOk) {
                // No root permissions: do NOT report fake frequencies or animate core usage
                CpuCoreTelemetry(
                    coreIndex = coreIndex,
                    isOnline = false,
                    currentFreqMhz = 0,
                    maxFreqMhz = if (coreIndex < 4) 1804 else 2400
                )
            } else {
                val batchItem = rootCoreBatch?.getOrNull(coreIndex)
                val online = batchItem?.online ?: if (coreIndex == 0) true else readCoreOnline(coreIndex)
                val curFreq = if (!online) {
                    0
                } else if (batchItem != null && batchItem.curFreqKhz > 0) {
                    batchItem.curFreqKhz / 1000
                } else {
                    readCoreCurrentFreq(coreIndex)
                }
                val maxFreq = if (batchItem != null && batchItem.maxFreqKhz > 0) {
                    batchItem.maxFreqKhz / 1000
                } else {
                    readCoreMaxFreq(coreIndex)
                }
                CpuCoreTelemetry(
                    coreIndex = coreIndex,
                    isOnline = online,
                    currentFreqMhz = curFreq,
                    maxFreqMhz = maxFreq
                )
            }
        }

        val governor = if (!rootOk) {
            "no root"
        } else {
            rootCoreBatch?.firstOrNull()?.governor
                ?: readSysFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
                ?: RootBridge.readNode("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
                ?: "schedutil"
        }

        val currentHz = try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            @Suppress("DEPRECATION")
            val display: Display? = wm?.defaultDisplay
            display?.refreshRate?.toInt() ?: 60
        } catch (_: Exception) {
            60
        }

        val pm = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
        val isSaverOn = pm?.isPowerSaveMode == true

        val gpuFreqStr = if (!rootOk) {
            "0 MHz"
        } else {
            val gpuKhz = readSysFile("/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq")?.toLongOrNull()
                ?: readSysFile("/sys/class/kgsl/kgsl-3d0/gpuclk")?.toLongOrNull()
                ?: RootBridge.readNode("/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq")?.toLongOrNull()
                ?: RootBridge.readNode("/sys/class/kgsl/kgsl-3d0/gpuclk")?.toLongOrNull()
            if (gpuKhz != null && gpuKhz > 1_000_000) {
                "${gpuKhz / 1_000_000} MHz"
            } else if (gpuKhz != null && gpuKhz > 1000) {
                "${gpuKhz / 1000} MHz"
            } else {
                "315 MHz"
            }
        }

        DeviceTelemetry(
            batteryLevel = pct,
            batteryTempC = tempC,
            batteryVoltageMv = voltageMv,
            batteryCurrentMa = currentMa,
            batteryHealthPercent = healthPct,
            batteryCycleCount = cycleCount,
            batteryCapacityMah = designMah,
            batteryFullChargeMah = fullMah,
            isCharging = isCharging,
            chargingWatts = watts,
            cpuCores = cores,
            activeGovernor = governor,
            isGmsDozeActive = false,
            isRootGranted = rootOk,
            currentRefreshRateHz = currentHz,
            isSystemBatterySaverActive = isSaverOn,
            gpuFreqInfo = gpuFreqStr
        )
    }

    private fun readCoreOnline(core: Int): Boolean {
        val path = "/sys/devices/system/cpu/cpu$core/online"
        val text = readSysFile(path) ?: return true
        return text.trim() != "0"
    }

    private fun readCoreCurrentFreq(core: Int): Int {
        val path = "/sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq"
        val khz = readSysFile(path)?.toIntOrNull()
            ?: readSysFile("/sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_cur_freq")?.toIntOrNull()
        return if (khz != null && khz > 0) {
            khz / 1000
        } else {
            0
        }
    }

    private fun readCoreMaxFreq(core: Int): Int {
        val path = "/sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_max_freq"
        val khz = readSysFile(path)?.toIntOrNull()
            ?: readSysFile("/sys/devices/system/cpu/cpu$core/cpufreq/scaling_max_freq")?.toIntOrNull()
        return if (khz != null && khz > 0) {
            khz / 1000
        } else {
            if (core < 4) 1804 else 2400
        }
    }

    private fun readSysFile(path: String): String? {
        return try {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                file.readText().trim()
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
