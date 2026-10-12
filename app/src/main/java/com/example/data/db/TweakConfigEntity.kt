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

    // Granular Toggles (Safe, RAM-only) - Turned OFF by default so user enables one by one
    val cpuFreqCapEnabled: Boolean = false,
    val schedutilGovernorEnabled: Boolean = false,
    val schedutilRateLimitsEnabled: Boolean = false,
    val twoCoresOfflineBelow20Enabled: Boolean = false,
    val offlineCoreCount: Int = 2, // 2, 3, or 4 cores
    val offlineBatteryThreshold: Int = 20, // 5% to 100%
    val inputTouchBoostDisabled: Boolean = false,
    val gpuPowerLimitEnabled: Boolean = false,
    val adrenoIdlerEnabled: Boolean = false,
    val storageIoQueueEnabled: Boolean = false,
    val dynamicFsyncEnabled: Boolean = false,
    val vmDirtyWritebackEnabled: Boolean = false,
    val tcpBbrCongestionEnabled: Boolean = false,
    val gmsDozeEnabled: Boolean = false,
    val lpmSleepEnabled: Boolean = false,
    val doubleTapWakePreserved: Boolean = false,
    val schedtuneTopAppEnabled: Boolean = false,
    val powerEfficientWorkqueueEnabled: Boolean = false,
    val systemBatterySaverEnabled: Boolean = false,
    val systemBatterySaverWithoutDarkEnabled: Boolean = false,
    val systemDarkModeEnabled: Boolean = false,
    val refreshRateHz: Int = 0, // 0 for Dynamic/Default, 60, 90, 120
    val lock60HzRefreshRateEnabled: Boolean = false,
    val backgroundScanningDisabled: Boolean = false,
    val touchDozeModeEnabled: Boolean = false,
    val controlCenterBlurEnabled: Boolean = false,

    // Balanced Mode Specific Toggles & Enhancements
    val balanceCpuTweaksEnabled: Boolean = false,
    val balanceRamScalingEnabled: Boolean = false,
    val balanceCfsSchedulerEnabled: Boolean = false,
    val balanceGpuOptimizationEnabled: Boolean = false,
    val balanceStorageIoEnabled: Boolean = false,
    val balanceNetworkBbrEnabled: Boolean = false,
    val deepSleepScreenOffEnabled: Boolean = false,
    val deepSleepDelayMinutes: Int = 5,
    val deepSleepWhitelist: String = "com.whatsapp,org.telegram.messenger,com.spotify.music",
    val developerProcessLimitEnabled: Boolean = false,
    val developerProcessLimit: Int = 4,
    val balanceCpuCapPercent: Int = 70,

    // Performance Mode Specific Toggles & Sub-Modes
    val performanceSubMode: String = "LITE", // LITE, HEAVY, ULTRA
    val perfCpuGovernorLockEnabled: Boolean = false,
    val perfAllCoresOnlineEnabled: Boolean = false,
    val perfSchedtuneBoostEnabled: Boolean = false,
    val perfGpuAdrenoBoostEnabled: Boolean = false,
    val perfGpuNoNapEnabled: Boolean = false,
    val perfDdrBusBoostEnabled: Boolean = false,
    val perfStorageQueue512Enabled: Boolean = false,
    val perfTouchBoostEnabled: Boolean = false,
    val perfLmkTuningEnabled: Boolean = false,

    // HyperOS / MIUI Super Power Saving Engine
    val hyperOsPowerSaverEnabled: Boolean = false,
    val hyperOsAodDisabled: Boolean = false,
    val hyperOsAurogonFreezerEnabled: Boolean = false,
    val hyperOsSuperPowerCleanEnabled: Boolean = false,
    val hyperOsFiveGPowerOptEnabled: Boolean = false,
    val hyperOsTouchBoostDisabled: Boolean = false,
    val hyperOsLock60HzEnabled: Boolean = false,
    val backgroundAllowedApps: String = "org.barebrowser,com.f0x1d.logfox,com.whatsapp"
)
