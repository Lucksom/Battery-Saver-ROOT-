package com.voltpower.data.telemetry

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.view.Display
import android.view.WindowManager
import com.voltpower.data.root.RootBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt

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
    val batteryCurrentMah: Int = 0,
    val isCharging: Boolean = false,
    val chargingWatts: Float = 0f,
    val cpuCores: List<CpuCoreTelemetry> = emptyList(),
    val activeGovernor: String = "schedutil",
    val isGmsDozeActive: Boolean = false,
    val isRootGranted: Boolean = false,
    val currentRefreshRateHz: Int = 60,
    val isSystemBatterySaverActive: Boolean = false,
    val gpuFreqMhz: Int = 0,
    val gpuFreqInfo: String = "0 MHz",
    val gpuLoadPercent: Int = -1,
    val gpuSubtitle: String = "Root required",
    val storageUsedGb: Float = 0f,
    val storageTotalGb: Float = 0f,
    val storageUsedPercent: Int = 0
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
            ?.takeIf { it != Int.MIN_VALUE && it != 0 }
            ?: readSysFile("/sys/class/power_supply/battery/current_now")?.toIntOrNull()
            ?: -150_000
        val currentMa = if (abs(rawCurrentNow) > 10_000) rawCurrentNow / 1000 else rawCurrentNow

        val watts = (abs(currentMa).toFloat() * voltageMv.toFloat()) / 1_000_000f

        val cycleCount = readSysFile("/sys/class/power_supply/battery/cycle_count")?.toIntOrNull()
            ?: readSysFile("/sys/class/power_supply/bms/cycle_count")?.toIntOrNull()
            ?: if (android.os.Build.VERSION.SDK_INT >= 34) {
                batteryIntent?.getIntExtra("android.os.extra.CYCLE_COUNT", -1)?.takeIf { it > 0 } ?: 128
            } else 128

        val rootOk = RootBridge.isRootAvailable()
        val hwBatch = if (rootOk) RootBridge.readHardwareSysfsBatch() else null
        val rootCoreBatch = hwBatch?.cores

        // --- Accurate Battery Capacity Calculation ---
        // 1. Charge counter (remaining charge in uAh/mAh) from BatteryManager or sysfs
        val bmChargeCounterRaw = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            ?.takeIf { it > 0 && it != Int.MIN_VALUE }?.toLong()
        val sysChargeCounterRaw = hwBatch?.batCounterRaw?.let { extractFirstLong(it) }
            ?: readSysFile("/sys/class/power_supply/battery/charge_counter")?.let { extractFirstLong(it) }
            ?: readSysFile("/sys/class/power_supply/bms/charge_counter")?.let { extractFirstLong(it) }
        val chargeCounterMah = normalizeChargeToMah(bmChargeCounterRaw ?: sysChargeCounterRaw ?: 0L) ?: 0

        // 2. Design capacity from root sysfs, direct sysfs, or Android PowerProfile
        val sysDesignMah = listOfNotNull(
            hwBatch?.batDesignRaw?.let { extractFirstLong(it) },
            readSysFile("/sys/class/power_supply/battery/charge_full_design")?.let { extractFirstLong(it) },
            readSysFile("/sys/class/power_supply/bms/charge_full_design")?.let { extractFirstLong(it) },
            readSysFile("/sys/class/power_supply/max170xx_battery/charge_full_design")?.let { extractFirstLong(it) },
            readSysFile("/sys/class/power_supply/battery/batt_full_capacity")?.let { extractFirstLong(it) },
            readSysFile("/sys/class/power_supply/battery/fg_fullcapnom")?.let { extractFirstLong(it) }
        ).firstNotNullOfOrNull { normalizeCapacityToMah(it) }

        val sysFullMah = listOfNotNull(
            hwBatch?.batFullRaw?.let { extractFirstLong(it) },
            readSysFile("/sys/class/power_supply/battery/charge_full")?.let { extractFirstLong(it) },
            readSysFile("/sys/class/power_supply/bms/charge_full")?.let { extractFirstLong(it) },
            readSysFile("/sys/class/power_supply/max170xx_battery/charge_full")?.let { extractFirstLong(it) }
        ).firstNotNullOfOrNull { normalizeCapacityToMah(it) }

        val powerProfileMah = readPowerProfileCapacityMah(context)

        val estimatedFullFromCounterMah = if (chargeCounterMah > 100 && pct in 5..100) {
            ((chargeCounterMah * 100.0) / pct.toDouble()).roundToInt().takeIf { it in 1200..25000 }
        } else null

        val designMah = sysDesignMah
            ?: powerProfileMah?.takeIf { it > 1000 }
            ?: sysFullMah
            ?: estimatedFullFromCounterMah
            ?: powerProfileMah
            ?: 5000

        val fullMah = sysFullMah
            ?: estimatedFullFromCounterMah
            ?: designMah

        val healthPct = if (designMah > 0) {
            ((fullMah.toFloat() / designMah.toFloat()) * 100f).roundToInt().coerceIn(50, 100)
        } else 96

        val currentRemainingMah = if (chargeCounterMah in 50..designMah + 1000) {
            chargeCounterMah
        } else {
            ((fullMah * pct) / 100.0).roundToInt()
        }

        // --- 8-core usage & frequency calculation (ONLY when root permission is granted) ---
        val cores = (0..7).map { coreIndex ->
            if (!rootOk) {
                CpuCoreTelemetry(
                    coreIndex = coreIndex,
                    isOnline = false,
                    currentFreqMhz = 0,
                    maxFreqMhz = if (coreIndex < 4) 1804 else 2400
                )
            } else {
                val batchItem = rootCoreBatch?.firstOrNull { it.coreIndex == coreIndex }
                    ?: rootCoreBatch?.getOrNull(coreIndex)
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

        // --- Accurate GPU Telemetry ---
        val rawGpuFreqStr = hwBatch?.gpuFreqRaw?.takeIf { it.isNotBlank() }
            ?: readSysFile("/sys/class/kgsl/kgsl-3d0/gpuclk")
            ?: readSysFile("/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq")
            ?: readSysFile("/sys/class/kgsl/kgsl-3d0/clock_mhz")
            ?: readSysFile("/sys/kernel/gpu/gpu_clock")
            ?: if (rootOk) {
                RootBridge.readNode("/sys/class/kgsl/kgsl-3d0/gpuclk")
                    ?: RootBridge.readNode("/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq")
                    ?: RootBridge.readNode("/sys/class/kgsl/kgsl-3d0/clock_mhz")
                    ?: RootBridge.readNode("/sys/kernel/gpu/gpu_clock")
            } else null

        val parsedGpuMhz = parseGpuFreqToMhz(rawGpuFreqStr)

        val rawGpuBusyStr = hwBatch?.gpuBusyRaw?.takeIf { it.isNotBlank() }
            ?: readSysFile("/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage")
            ?: readSysFile("/sys/kernel/gpu/gpu_busy")
            ?: readSysFile("/sys/class/kgsl/kgsl-3d0/gpubusy")

        val parsedGpuLoadPct = parseGpuBusyPercent(rawGpuBusyStr)

        val rawGpuPwrLevel = (hwBatch?.gpuPwrLevelRaw?.takeIf { it.isNotBlank() }
            ?: readSysFile("/sys/class/kgsl/kgsl-3d0/min_pwrlevel")
            ?: readSysFile("/sys/class/kgsl/kgsl-3d0/default_pwrlevel"))
            ?.let { extractFirstLong(it)?.toInt() }

        val rawGpuModelOrGov = (hwBatch?.gpuModelOrGovRaw?.takeIf { it.isNotBlank() }
            ?: readSysFile("/sys/kernel/gpu/gpu_model")
            ?: readSysFile("/sys/class/kgsl/kgsl-3d0/gpu_model")
            ?: readSysFile("/sys/class/kgsl/kgsl-3d0/devfreq/governor"))
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        val cleanGpuLabel = when {
            !rawGpuModelOrGov.isNullOrBlank() -> {
                rawGpuModelOrGov
                    .replace(Regex("(?i)qualcomm|technologies, inc\\.|\\(tm\\)"), "")
                    .trim()
                    .take(16)
                    .ifBlank { "GPU" }
            }
            rawGpuPwrLevel != null -> "Adreno"
            else -> "GPU"
        }

        val gpuSub = when {
            parsedGpuMhz > 0 && parsedGpuLoadPct >= 0 -> "$cleanGpuLabel · $parsedGpuLoadPct% load"
            parsedGpuMhz > 0 && rawGpuPwrLevel != null -> "$cleanGpuLabel · pwrlevel $rawGpuPwrLevel"
            parsedGpuMhz > 0 -> "$cleanGpuLabel · active"
            rootOk -> "$cleanGpuLabel · idle"
            else -> "Root required"
        }

        val gpuFreqStr = if (parsedGpuMhz > 0) "$parsedGpuMhz MHz" else "0 MHz"

        // --- Accurate Storage Telemetry (Used / Total GB) ---
        val (storageUsedGb, storageTotalGb, storageUsedPct) = readInternalStorageStats(context)

        DeviceTelemetry(
            batteryLevel = pct,
            batteryTempC = tempC,
            batteryVoltageMv = voltageMv,
            batteryCurrentMa = currentMa,
            batteryHealthPercent = healthPct,
            batteryCycleCount = cycleCount,
            batteryCapacityMah = designMah,
            batteryFullChargeMah = fullMah,
            batteryCurrentMah = currentRemainingMah,
            isCharging = isCharging,
            chargingWatts = watts,
            cpuCores = cores,
            activeGovernor = governor,
            isGmsDozeActive = false,
            isRootGranted = rootOk,
            currentRefreshRateHz = currentHz,
            isSystemBatterySaverActive = isSaverOn,
            gpuFreqMhz = parsedGpuMhz,
            gpuFreqInfo = gpuFreqStr,
            gpuLoadPercent = parsedGpuLoadPct,
            gpuSubtitle = gpuSub,
            storageUsedGb = storageUsedGb,
            storageTotalGb = storageTotalGb,
            storageUsedPercent = storageUsedPct
        )
    }

    private fun readInternalStorageStats(context: Context): Triple<Float, Float, Int> {
        try {
            val ssm = context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
            if (ssm != null) {
                val totalBytes = ssm.getTotalBytes(StorageManager.UUID_DEFAULT)
                val freeBytes = ssm.getFreeBytes(StorageManager.UUID_DEFAULT)
                if (totalBytes > 0L) {
                    val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
                    // StorageStatsManager rounds totalBytes to decimal GB (1000^3) on most Android 8+ builds
                    val divisor = if (totalBytes % 1_000_000_000L == 0L) {
                        1_000_000_000.0
                    } else {
                        1024.0 * 1024.0 * 1024.0
                    }
                    val totalGb = (totalBytes / divisor).toFloat()
                    val usedGb = (usedBytes / divisor).toFloat()
                    val pct = ((usedBytes.toDouble() / totalBytes.toDouble()) * 100.0)
                        .roundToInt()
                        .coerceIn(0, 100)
                    return Triple(usedGb, totalGb, pct)
                }
            }
        } catch (_: Throwable) {
            // Fall back to StatFs below
        }

        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.totalBytes
            val availBytes = stat.availableBytes
            val usedBytes = (totalBytes - availBytes).coerceAtLeast(0L)
            val giB = 1024.0 * 1024.0 * 1024.0
            val totalGb = (totalBytes / giB).toFloat()
            val usedGb = (usedBytes / giB).toFloat()
            val pct = if (totalBytes > 0L) {
                ((usedBytes.toDouble() / totalBytes.toDouble()) * 100.0).roundToInt().coerceIn(0, 100)
            } else 0
            Triple(usedGb, totalGb, pct)
        } catch (_: Throwable) {
            Triple(0f, 0f, 0)
        }
    }

    private fun extractFirstLong(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        return Regex("(\\d+)").find(raw)?.groupValues?.getOrNull(1)?.toLongOrNull()
    }

    private fun normalizeCapacityToMah(raw: Long): Int? {
        if (raw <= 0L) return null
        val mah = when {
            raw >= 100_000_000L -> (raw / 1_000_000L).toInt()
            raw >= 100_000L -> (raw / 1_000L).toInt() // uAh -> mAh (e.g., 5000000 -> 5000)
            raw in 25_000L..99_999L -> (raw / 10L).toInt() // 0.1mAh -> mAh (e.g., 50000 -> 5000)
            raw in 1_200L..24_999L -> raw.toInt() // Direct mAh (e.g., 5000 -> 5000)
            else -> return null
        }
        return mah.takeIf { it in 1200..25000 }
    }

    private fun normalizeChargeToMah(raw: Long): Int? {
        if (raw <= 0L) return null
        return when {
            raw >= 10_000L -> (raw / 1_000L).toInt() // uAh -> mAh
            raw in 1L..9_999L -> raw.toInt()
            else -> null
        }
    }

    private fun readPowerProfileCapacityMah(context: Context): Int? {
        return try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val instance = powerProfileClass.getConstructor(Context::class.java).newInstance(context)
            val cap = (powerProfileClass.getMethod("getBatteryCapacity").invoke(instance) as? Double)
                ?.roundToInt()
            cap?.takeIf { it in 1000..25000 }
        } catch (_: Throwable) {
            null
        }
    }

    private fun parseGpuFreqToMhz(raw: String?): Int {
        if (raw.isNullOrBlank()) return 0
        val nums = Regex("(\\d+)").findAll(raw).mapNotNull { it.groupValues.getOrNull(1)?.toLongOrNull() }.toList()
        val value = nums.firstOrNull { it > 0L } ?: return 0
        val mhz = when {
            value >= 10_000_000L -> (value / 1_000_000L).toInt() // Hz -> MHz (e.g. 315000000 -> 315)
            value >= 10_000L -> (value / 1_000L).toInt() // kHz -> MHz (e.g. 315000 -> 315)
            value in 1L..9_999L -> value.toInt() // Already MHz (e.g. 315 -> 315)
            else -> 0
        }
        return mhz.takeIf { it in 1..4000 } ?: 0
    }

    private fun parseGpuBusyPercent(raw: String?): Int {
        if (raw.isNullOrBlank()) return -1
        val nums = Regex("(\\d+)").findAll(raw).mapNotNull { it.groupValues.getOrNull(1)?.toLongOrNull() }.toList()
        if (nums.isEmpty()) return -1
        // If gpubusy format "busy total" (two numbers where second > first and second > 100)
        if (nums.size >= 2 && nums[1] > 100L && nums[0] <= nums[1]) {
            return ((nums[0].toDouble() / nums[1].toDouble()) * 100.0).roundToInt().coerceIn(0, 100)
        }
        return nums[0].toInt().coerceIn(0, 100)
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

