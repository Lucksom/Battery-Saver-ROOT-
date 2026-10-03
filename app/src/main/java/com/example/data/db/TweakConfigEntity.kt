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
    val powerEfficientWorkqueueEnabled: Boolean = true
)
