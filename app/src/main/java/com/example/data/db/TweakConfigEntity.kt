package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tweak_config")
data class TweakConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val activeProfile: String = "POWERSAVE", // POWERSAVE, BALANCE, PERFORMANCE, CUSTOM
    val isMasterApplied: Boolean = false,
    val backupCreated: Boolean = false,
    val backupFilePath: String = "/sdcard/Download/Stock_Kernel_Backup.sh",
    val themeMode: String = "SYSTEM", // SYSTEM, DARK, LIGHT
    val lastAppliedTimestamp: Long = 0L,

    // Granular Toggles (Safe, RAM-only)
    val cpuFreqCapEnabled: Boolean = true,
    val schedutilGovernorEnabled: Boolean = true,
    val schedutilRateLimitsEnabled: Boolean = true,
    val twoCoresOfflineBelow20Enabled: Boolean = true,
    val offlineCoreCount: Int = 2, // 2, 3, or 4 cores
    val offlineBatteryThreshold: Int = 20, // 5% to 100%
    val inputTouchBoostDisabled: Boolean = true,
    val gpuPowerLimitEnabled: Boolean = true,
    val adrenoIdlerEnabled: Boolean = true,
    val storageIoQueueEnabled: Boolean = true,
    val dynamicFsyncEnabled: Boolean = true,
    val vmDirtyWritebackEnabled: Boolean = true,
    val tcpBbrCongestionEnabled: Boolean = true,
    val gmsDozeEnabled: Boolean = true,
    val lpmSleepEnabled: Boolean = true,
    val doubleTapWakePreserved: Boolean = true,
    val schedtuneTopAppEnabled: Boolean = true,
    val powerEfficientWorkqueueEnabled: Boolean = true,
    val systemBatterySaverWithoutDarkEnabled: Boolean = true,
    val lock60HzRefreshRateEnabled: Boolean = true,

    // Balanced Mode Specific Toggles & Enhancements
    val balanceCpuTweaksEnabled: Boolean = true,
    val balanceRamScalingEnabled: Boolean = true,
    val balanceCfsSchedulerEnabled: Boolean = true,
    val balanceGpuOptimizationEnabled: Boolean = true,
    val balanceStorageIoEnabled: Boolean = true,
    val balanceNetworkBbrEnabled: Boolean = true,
    val deepSleepScreenOffEnabled: Boolean = true,
    val deepSleepDelayMinutes: Int = 5,
    val deepSleepWhitelist: String = "com.whatsapp,org.telegram.messenger,com.spotify.music",
    val developerProcessLimitEnabled: Boolean = true,
    val developerProcessLimit: Int = 4,
    val balanceCpuCapPercent: Int = 70
)
