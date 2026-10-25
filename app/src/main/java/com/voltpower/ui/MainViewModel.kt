package com.voltpower.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voltpower.data.db.AppDatabase
import com.voltpower.data.db.TweakConfigEntity
import com.voltpower.data.log.LogCategory
import com.voltpower.data.log.LogEntry
import com.voltpower.data.log.LogRepository
import com.voltpower.data.root.RootBridge
import com.voltpower.data.telemetry.CpuCoreTelemetry
import com.voltpower.data.telemetry.DeviceTelemetry
import com.voltpower.data.telemetry.TelemetryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).tweakConfigDao()
    private val appInfoRepository = com.voltpower.data.apps.AppInfoRepository(application)

    private val _installedApps = MutableStateFlow<List<com.voltpower.data.apps.InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<com.voltpower.data.apps.InstalledAppItem>> = _installedApps.asStateFlow()

    val configState: StateFlow<TweakConfigEntity> = dao.getConfig()
        .map { it ?: TweakConfigEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TweakConfigEntity()
        )

    private val _telemetry = MutableStateFlow(
        DeviceTelemetry(
            cpuCores = List(8) { index ->
                CpuCoreTelemetry(
                    coreIndex = index,
                    isOnline = false,
                    currentFreqMhz = 0,
                    maxFreqMhz = if (index < 4) 1804 else 2400
                )
            },
            activeGovernor = "no root",
            isRootGranted = false
        )
    )
    val telemetry: StateFlow<DeviceTelemetry> = _telemetry.asStateFlow()

    val logs: StateFlow<List<LogEntry>> = LogRepository.logs

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _hasUnappliedChanges = MutableStateFlow(false)
    val hasUnappliedChanges: StateFlow<Boolean> = _hasUnappliedChanges.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        viewModelScope.launch {
            if (dao.getConfigDirect() == null) {
                dao.insertOrUpdate(TweakConfigEntity())
            }
            launch {
                _installedApps.value = appInfoRepository.getInstalledApps()
            }
            refreshTelemetry()
            while (true) {
                delay(2500)
                refreshTelemetry()
            }
        }
    }

    fun refreshTelemetry() {
        viewModelScope.launch {
            val snap = TelemetryRepository.readTelemetry(getApplication())
            _telemetry.value = snap
        }
    }

    private fun resetAllTogglesToOff(base: TweakConfigEntity): TweakConfigEntity {
        return base.copy(
            cpuFreqCapEnabled = false,
            schedutilGovernorEnabled = false,
            schedutilRateLimitsEnabled = false,
            twoCoresOfflineBelow20Enabled = false,
            inputTouchBoostDisabled = false,
            gpuPowerLimitEnabled = false,
            adrenoIdlerEnabled = false,
            storageIoQueueEnabled = false,
            dynamicFsyncEnabled = false,
            vmDirtyWritebackEnabled = false,
            tcpBbrCongestionEnabled = false,
            gmsDozeEnabled = false,
            lpmSleepEnabled = false,
            doubleTapWakePreserved = false,
            schedtuneTopAppEnabled = false,
            powerEfficientWorkqueueEnabled = false,
            backgroundScanningDisabled = false,
            touchDozeModeEnabled = false,
            systemBatterySaverEnabled = false,
            systemBatterySaverWithoutDarkEnabled = false,
            systemDarkModeEnabled = false,
            controlCenterBlurEnabled = false,
            lock60HzRefreshRateEnabled = false,
            refreshRateHz = 0,
            deepSleepScreenOffEnabled = false,
            developerProcessLimitEnabled = false,
            balanceCpuTweaksEnabled = false,
            balanceRamScalingEnabled = false,
            balanceCfsSchedulerEnabled = false,
            balanceGpuOptimizationEnabled = false,
            balanceStorageIoEnabled = false,
            balanceNetworkBbrEnabled = false,
            perfCpuGovernorLockEnabled = false,
            perfAllCoresOnlineEnabled = false,
            perfSchedtuneBoostEnabled = false,
            perfGpuAdrenoBoostEnabled = false,
            perfGpuNoNapEnabled = false,
            perfDdrBusBoostEnabled = false,
            perfStorageQueue512Enabled = false,
            perfTouchBoostEnabled = false,
            perfLmkTuningEnabled = false,
            hyperOsPowerSaverEnabled = false,
            hyperOsAodDisabled = false,
            hyperOsAurogonFreezerEnabled = false,
            hyperOsSuperPowerCleanEnabled = false,
            hyperOsFiveGPowerOptEnabled = false,
            hyperOsTouchBoostDisabled = false,
            hyperOsLock60HzEnabled = false,
            hyperOsUltraSuperSaveUiEnabled = false
        )
    }

    private fun applyPowersavePreset(base: TweakConfigEntity): TweakConfigEntity {
        val clean = resetAllTogglesToOff(base)
        return clean.copy(
            activeProfile = "POWERSAVE",
            cpuFreqCapEnabled = true,
            schedutilGovernorEnabled = true,
            schedutilRateLimitsEnabled = true,
            twoCoresOfflineBelow20Enabled = true,
            inputTouchBoostDisabled = true,
            gpuPowerLimitEnabled = true,
            adrenoIdlerEnabled = true,
            storageIoQueueEnabled = true,
            dynamicFsyncEnabled = true,
            vmDirtyWritebackEnabled = true,
            tcpBbrCongestionEnabled = true,
            gmsDozeEnabled = true,
            lpmSleepEnabled = true,
            doubleTapWakePreserved = true,
            schedtuneTopAppEnabled = true,
            powerEfficientWorkqueueEnabled = true,
            backgroundScanningDisabled = true,
            touchDozeModeEnabled = true,
            systemBatterySaverWithoutDarkEnabled = true,
            lock60HzRefreshRateEnabled = true,
            refreshRateHz = 60
        )
    }

    private fun applyBalancePreset(base: TweakConfigEntity): TweakConfigEntity {
        val clean = resetAllTogglesToOff(base)
        return clean.copy(
            activeProfile = "BALANCE",
            balanceCpuTweaksEnabled = true,
            cpuFreqCapEnabled = true,
            schedutilGovernorEnabled = true,
            schedutilRateLimitsEnabled = true,
            balanceRamScalingEnabled = true,
            balanceCfsSchedulerEnabled = true,
            balanceGpuOptimizationEnabled = true,
            balanceStorageIoEnabled = true,
            balanceNetworkBbrEnabled = true,
            deepSleepScreenOffEnabled = true
        )
    }

    private fun applyPerformanceTierPreset(
        base: TweakConfigEntity,
        rawSubMode: String
    ): TweakConfigEntity {
        val clean = resetAllTogglesToOff(base)
        val subMode = when (rawSubMode.uppercase()) {
            "HEAVY", "EXTREME" -> "EXTREME"
            "ULTRA" -> "ULTRA"
            else -> "LITE"
        }
        return when (subMode) {
            "ULTRA" -> clean.copy(
                activeProfile = "PERFORMANCE",
                performanceSubMode = "ULTRA",
                perfCpuGovernorLockEnabled = true,
                perfAllCoresOnlineEnabled = true,
                perfSchedtuneBoostEnabled = true,
                perfGpuAdrenoBoostEnabled = true,
                perfGpuNoNapEnabled = true,
                perfDdrBusBoostEnabled = true,
                perfStorageQueue512Enabled = true,
                perfTouchBoostEnabled = true,
                perfLmkTuningEnabled = true
            )
            "EXTREME" -> clean.copy(
                activeProfile = "PERFORMANCE",
                performanceSubMode = "EXTREME",
                perfCpuGovernorLockEnabled = true,
                perfAllCoresOnlineEnabled = true,
                perfSchedtuneBoostEnabled = true,
                perfGpuAdrenoBoostEnabled = true,
                perfGpuNoNapEnabled = true,
                perfDdrBusBoostEnabled = true,
                perfStorageQueue512Enabled = true,
                perfTouchBoostEnabled = true,
                perfLmkTuningEnabled = true
            )
            else -> clean.copy(
                activeProfile = "PERFORMANCE",
                performanceSubMode = "LITE",
                perfCpuGovernorLockEnabled = true,
                perfAllCoresOnlineEnabled = true,
                perfSchedtuneBoostEnabled = true,
                perfGpuAdrenoBoostEnabled = true,
                perfGpuNoNapEnabled = false,
                perfDdrBusBoostEnabled = false,
                perfStorageQueue512Enabled = true,
                perfTouchBoostEnabled = true,
                perfLmkTuningEnabled = false
            )
        }
    }

    fun onToggleMaster(enable: Boolean) {
        viewModelScope.launch {
            if (!_telemetry.value.isRootGranted) {
                val rootNow = RootBridge.isRootAvailable(forceCheck = true)
                if (!rootNow) {
                    _statusMessage.value = "Root permission required to apply kernel tweaks."
                    return@launch
                }
            }
            _isBusy.value = true
            val current = configState.value
            if (enable) {
                val toApply = when (current.activeProfile) {
                    "PERFORMANCE" -> {
                        val anyPerfOn = current.perfCpuGovernorLockEnabled ||
                            current.perfAllCoresOnlineEnabled ||
                            current.perfSchedtuneBoostEnabled ||
                            current.perfGpuAdrenoBoostEnabled ||
                            current.perfStorageQueue512Enabled ||
                            current.perfTouchBoostEnabled
                        val withPerf = if (!anyPerfOn) {
                            applyPerformanceTierPreset(current, current.performanceSubMode)
                        } else {
                            current
                        }
                        withPerf.copy(
                            isMasterApplied = true,
                            lastAppliedTimestamp = System.currentTimeMillis()
                        )
                    }
                    "BALANCE" -> {
                        val anyBalOn = current.balanceCpuTweaksEnabled ||
                            current.balanceRamScalingEnabled ||
                            current.balanceCfsSchedulerEnabled ||
                            current.balanceGpuOptimizationEnabled ||
                            current.balanceStorageIoEnabled ||
                            current.balanceNetworkBbrEnabled ||
                            current.deepSleepScreenOffEnabled
                        val withBal = if (!anyBalOn) {
                            applyBalancePreset(current)
                        } else {
                            current
                        }
                        withBal.copy(
                            isMasterApplied = true,
                            lastAppliedTimestamp = System.currentTimeMillis()
                        )
                    }
                    else -> {
                        val allOff = !current.cpuFreqCapEnabled &&
                            !current.schedutilGovernorEnabled &&
                            !current.schedutilRateLimitsEnabled &&
                            !current.twoCoresOfflineBelow20Enabled &&
                            !current.inputTouchBoostDisabled &&
                            !current.gpuPowerLimitEnabled &&
                            !current.adrenoIdlerEnabled &&
                            !current.storageIoQueueEnabled &&
                            !current.dynamicFsyncEnabled &&
                            !current.vmDirtyWritebackEnabled &&
                            !current.gmsDozeEnabled &&
                            !current.lpmSleepEnabled &&
                            !current.schedtuneTopAppEnabled &&
                            !current.powerEfficientWorkqueueEnabled &&
                            !current.hyperOsPowerSaverEnabled
                        if (allOff) {
                            applyPowersavePreset(current).copy(
                                isMasterApplied = true,
                                lastAppliedTimestamp = System.currentTimeMillis()
                            )
                        } else {
                            current.copy(
                                isMasterApplied = true,
                                lastAppliedTimestamp = System.currentTimeMillis()
                            )
                        }
                    }
                }
                _statusMessage.value = "Applying ${toApply.activeProfile.lowercase()} tweaks to RAM..."
                dao.update(toApply)
                RootBridge.revertAllTweaks(getApplication())
                RootBridge.applyPowersaveTweaks(getApplication(), toApply)
                dao.updateBackupStatus(true, RootBridge.BACKUP_FILE_PATH)
                _statusMessage.value = "Tweaks active in RAM!"
                val profileName = when (toApply.activeProfile) {
                    "PERFORMANCE" -> {
                        val tier = if (toApply.performanceSubMode == "HEAVY") "EXTREME" else toApply.performanceSubMode
                        "Performance ($tier) engine enabled"
                    }
                    "BALANCE" -> "Balance profile engine enabled"
                    else -> "Powersave profile engine enabled"
                }
                LogRepository.addLog(
                    category = LogCategory.TWEAK,
                    message = profileName,
                    isSuccess = true
                )
            } else {
                _statusMessage.value = "Restoring factory kernel parameters..."
                RootBridge.revertAllTweaks(getApplication())
                val allOff = resetAllTogglesToOff(current).copy(
                    isMasterApplied = false,
                    lastAppliedTimestamp = System.currentTimeMillis()
                )
                dao.update(allOff)
                _statusMessage.value = "All tweaks reverted to stock."
                LogRepository.addLog(
                    category = LogCategory.TWEAK,
                    message = "Stock kernel parameters restored",
                    isSuccess = true
                )
            }
            refreshTelemetry()
            _isBusy.value = false
        }
    }

    fun onRevertToStock() {
        viewModelScope.launch {
            _isBusy.value = true
            _statusMessage.value = "Restoring stock kernel parameters..."
            if (_telemetry.value.isRootGranted || RootBridge.isRootAvailable()) {
                RootBridge.revertAllTweaks(getApplication())
            }
            val current = configState.value
            val allOff = resetAllTogglesToOff(current).copy(
                isMasterApplied = false,
                lastAppliedTimestamp = System.currentTimeMillis()
            )
            dao.update(allOff)
            _hasUnappliedChanges.value = false
            _statusMessage.value = "Stock parameters restored & all toggles turned off."
            LogRepository.addLog(
                category = LogCategory.TWEAK,
                message = "Reverted all tweaks to stock",
                isSuccess = true
            )
            refreshTelemetry()
            _isBusy.value = false
        }
    }

    fun onSelectProfile(profile: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val current = configState.value
            val hasRoot = _telemetry.value.isRootGranted || RootBridge.isRootAvailable()
            val basePreset = when (profile) {
                "PERFORMANCE" -> {
                    val tier = when (current.performanceSubMode.uppercase()) {
                        "HEAVY", "EXTREME" -> "EXTREME"
                        "ULTRA" -> "ULTRA"
                        else -> "LITE"
                    }
                    applyPerformanceTierPreset(current, tier)
                }
                "BALANCE" -> applyBalancePreset(current)
                else -> applyPowersavePreset(current)
            }
            val updated = basePreset.copy(
                isMasterApplied = if (hasRoot) true else current.isMasterApplied,
                lastAppliedTimestamp = System.currentTimeMillis()
            )

            dao.update(updated)
            _hasUnappliedChanges.value = false

            val profileLogLabel = when (profile) {
                "PERFORMANCE" -> {
                    val tierLabel = when (updated.performanceSubMode) {
                        "ULTRA" -> "Ultra"
                        "EXTREME", "HEAVY" -> "Extreme"
                        else -> "Lite"
                    }
                    "Performance profile ($tierLabel)"
                }
                "BALANCE" -> "Balance profile"
                else -> "Powersave profile"
            }

            if (hasRoot) {
                _statusMessage.value = "Reverting previous profile & applying $profileLogLabel..."
                // First revert previous profile tweaks to system default, then apply the selected profile
                RootBridge.revertAllTweaks(getApplication())
                RootBridge.applyPowersaveTweaks(getApplication(), updated)
                dao.updateBackupStatus(true, RootBridge.BACKUP_FILE_PATH)
                refreshTelemetry()
            }

            _statusMessage.value = "$profileLogLabel applied"
            LogRepository.addLog(
                category = LogCategory.PROFILE,
                message = "$profileLogLabel enabled (previous profile tweaks reverted first)",
                isSuccess = true
            )
            _isBusy.value = false
        }
    }

    /**
     * Used in the Tweaks section so the user can freely browse/explore Powersave, Balance,
     * and Performance tweak categories without immediately applying them to the system.
     */
    fun onExploreProfileInTweaks(profile: String) {
        viewModelScope.launch {
            val current = configState.value
            if (current.activeProfile == profile) return@launch
            val updated = current.copy(activeProfile = profile)
            dao.update(updated)
            _hasUnappliedChanges.value = true
        }
    }

    /**
     * Used in the Tweaks section so the user can explore Lite / Extreme / Ultra tiers
     * and toggle their tweaks independently before swiping to apply.
     */
    fun onExplorePerformanceSubModeInTweaks(subMode: String) {
        viewModelScope.launch {
            val normalized = when (subMode.uppercase()) {
                "HEAVY", "EXTREME" -> "EXTREME"
                "ULTRA" -> "ULTRA"
                else -> "LITE"
            }
            val current = configState.value
            val updated = applyPerformanceTierPreset(current, normalized).copy(
                isMasterApplied = current.isMasterApplied
            )
            dao.update(updated)
            _hasUnappliedChanges.value = true
        }
    }

    /**
     * Allows the user to quickly select all or uncheck all tweaks in the currently viewed category
     * without applying until they swipe "Swipe to apply".
     */
    fun onSetAllCurrentProfileTweaks(selectAll: Boolean) {
        viewModelScope.launch {
            val current = configState.value
            val updated = when (current.activeProfile) {
                "PERFORMANCE" -> {
                    if (selectAll) {
                        applyPerformanceTierPreset(current, current.performanceSubMode).copy(
                            isMasterApplied = current.isMasterApplied
                        )
                    } else {
                        current.copy(
                            perfCpuGovernorLockEnabled = false,
                            perfAllCoresOnlineEnabled = false,
                            perfSchedtuneBoostEnabled = false,
                            perfGpuAdrenoBoostEnabled = false,
                            perfGpuNoNapEnabled = false,
                            perfDdrBusBoostEnabled = false,
                            perfStorageQueue512Enabled = false,
                            perfTouchBoostEnabled = false,
                            perfLmkTuningEnabled = false
                        )
                    }
                }
                "BALANCE" -> {
                    if (selectAll) {
                        applyBalancePreset(current).copy(
                            isMasterApplied = current.isMasterApplied
                        )
                    } else {
                        current.copy(
                            balanceCpuTweaksEnabled = false,
                            cpuFreqCapEnabled = false,
                            schedutilGovernorEnabled = false,
                            schedutilRateLimitsEnabled = false,
                            balanceRamScalingEnabled = false,
                            balanceCfsSchedulerEnabled = false,
                            balanceGpuOptimizationEnabled = false,
                            balanceStorageIoEnabled = false,
                            balanceNetworkBbrEnabled = false,
                            deepSleepScreenOffEnabled = false,
                            developerProcessLimitEnabled = false
                        )
                    }
                }
                else -> {
                    if (current.hyperOsPowerSaverEnabled) {
                        current.copy(
                            hyperOsAodDisabled = selectAll,
                            hyperOsAurogonFreezerEnabled = selectAll,
                            hyperOsSuperPowerCleanEnabled = selectAll,
                            hyperOsTouchBoostDisabled = selectAll,
                            touchDozeModeEnabled = selectAll,
                            backgroundScanningDisabled = selectAll,
                            hyperOsFiveGPowerOptEnabled = selectAll,
                            hyperOsLock60HzEnabled = selectAll,
                            lock60HzRefreshRateEnabled = selectAll,
                            refreshRateHz = if (selectAll) 60 else 0,
                            controlCenterBlurEnabled = selectAll,
                            systemBatterySaverEnabled = selectAll,
                            systemBatterySaverWithoutDarkEnabled = selectAll,
                            systemDarkModeEnabled = false,
                            hyperOsUltraSuperSaveUiEnabled = false
                        )
                    } else {
                        if (selectAll) {
                            applyPowersavePreset(current).copy(
                                isMasterApplied = current.isMasterApplied
                            )
                        } else {
                            current.copy(
                                cpuFreqCapEnabled = false,
                                schedutilGovernorEnabled = false,
                                schedutilRateLimitsEnabled = false,
                                twoCoresOfflineBelow20Enabled = false,
                                inputTouchBoostDisabled = false,
                                gpuPowerLimitEnabled = false,
                                adrenoIdlerEnabled = false,
                                storageIoQueueEnabled = false,
                                dynamicFsyncEnabled = false,
                                vmDirtyWritebackEnabled = false,
                                tcpBbrCongestionEnabled = false,
                                gmsDozeEnabled = false,
                                lpmSleepEnabled = false,
                                doubleTapWakePreserved = false,
                                schedtuneTopAppEnabled = false,
                                powerEfficientWorkqueueEnabled = false,
                                backgroundScanningDisabled = false,
                                touchDozeModeEnabled = false,
                                systemBatterySaverWithoutDarkEnabled = false,
                                lock60HzRefreshRateEnabled = false,
                                refreshRateHz = 0
                            )
                        }
                    }
                }
            }
            dao.update(updated)
            _hasUnappliedChanges.value = true
        }
    }

    /**
     * Triggered exclusively when the user swipes the "Swipe to apply" bar in the Tweaks section.
     * Reverts any previously active conflicting/disabled tweaks and applies all currently selected tweaks.
     */
    fun onSwipeToApplyTweaks() {
        viewModelScope.launch {
            _isBusy.value = true
            val current = configState.value
            val hasRoot = _telemetry.value.isRootGranted || RootBridge.isRootAvailable(forceCheck = true)
            val updated = current.copy(
                isMasterApplied = if (hasRoot) true else current.isMasterApplied,
                lastAppliedTimestamp = System.currentTimeMillis()
            )
            dao.update(updated)
            _hasUnappliedChanges.value = false

            if (hasRoot) {
                _statusMessage.value = "Applying selected tweaks..."
                // 1. Revert previous state first so any unchecked tweaks return to system default
                RootBridge.revertAllTweaks(getApplication())

                // 2. Apply or restore explicit system UI / display / battery saver toggles based on current selection
                RootBridge.setSystemDarkMode(updated.systemDarkModeEnabled)
                RootBridge.setControlCenterBlur(updated.controlCenterBlurEnabled)
                RootBridge.setSystemBatterySaver(
                    updated.systemBatterySaverEnabled || updated.systemBatterySaverWithoutDarkEnabled
                )
                RootBridge.setHyperOsUltraSuperSaveUi(updated.hyperOsUltraSuperSaveUiEnabled)
                RootBridge.setDisplayRefreshRate(updated.refreshRateHz)

                // 3. Apply all selected kernel & profile tweaks
                RootBridge.applyPowersaveTweaks(getApplication(), updated)
                dao.updateBackupStatus(true, RootBridge.BACKUP_FILE_PATH)
                refreshTelemetry()

                val msg = "Selected tweaks applied (${updated.activeProfile.lowercase()} mode)"
                _statusMessage.value = msg
                LogRepository.addLog(
                    category = LogCategory.TWEAK,
                    message = msg,
                    isSuccess = true
                )
            } else {
                _statusMessage.value = "Tweaks saved (Root permission required to write to kernel)"
                LogRepository.addLog(
                    category = LogCategory.TWEAK,
                    message = "Tweaks staged (no root detected)",
                    isSuccess = false
                )
            }
            _isBusy.value = false
        }
    }

    fun onClearLogs() {
        LogRepository.clearLogs()
    }

    fun onToggleTheme(mode: String) {
        viewModelScope.launch {
            dao.updateThemeMode(mode)
        }
    }

    private fun humanReadableTweakLog(toggleKey: String, enabled: Boolean): String {
        return when (toggleKey) {
            "cpuFreqCap" -> if (enabled) "Limit peak frequencies enabled" else "Limit peak frequencies disabled"
            "schedutilGov" -> if (enabled) "Powersave energy governor enabled" else "CPU governor restored to stock"
            "schedutilRate" -> if (enabled) "Powersave governor rate limits enabled" else "Governor rate limits restored"
            "twoCoresOffline" -> if (enabled) "Park big cores on low battery enabled" else "All CPU cores online restored"
            "inputTouchBoost" -> if (enabled) "Disable touch boost" else "Enable touch boost"
            "gpuPowerLimit" -> if (enabled) "GPU power limiter enabled" else "GPU power limiter disabled"
            "adrenoIdler" -> if (enabled) "Adreno idler enabled" else "Adreno idler disabled"
            "storageIo" -> if (enabled) "Optimise UFS I/O queue" else "UFS I/O queue restored"
            "dynamicFsync" -> if (enabled) "Dynamic Fsync enabled" else "Dynamic Fsync disabled"
            "vmDirtyWriteback" -> if (enabled) "Defer VM dirty writeback" else "VM dirty writeback restored"
            "tcpBbr" -> if (enabled) "TCP BBR congestion control enabled" else "TCP congestion control restored"
            "gmsDoze" -> if (enabled) "Aggressive GMS doze enabled" else "GMS doze restored"
            "lpmSleep" -> if (enabled) "Kernel LPM deep sleep enabled" else "Kernel LPM deep sleep disabled"
            "doubleTapWake" -> if (enabled) "Preserve double-tap to wake" else "Double-tap to wake default"
            "schedtuneTopApp" -> if (enabled) "Restrict background apps to LITTLE cores" else "Background cpuset restored"
            "powerEfficientWorkqueue" -> if (enabled) "Power-efficient workqueues enabled" else "Workqueues restored"
            "backgroundScanning" -> if (enabled) "Disable Wi-Fi & BLE background scan" else "Background scan restored"
            "touchDozeMode" -> if (enabled) "Xiaomi THP touch doze mode enabled" else "Xiaomi THP touch doze mode disabled"
            "systemBatterySaver" -> if (enabled) "HyperOS PowerKeeper Battery Saver enabled" else "HyperOS PowerKeeper Battery Saver disabled"
            "batterySaverNoDark" -> if (enabled) "Android & HyperOS system battery saver enabled" else "Android & HyperOS system battery saver disabled"
            "systemDarkMode" -> if (enabled) "Force AMOLED dark mode enabled" else "Force AMOLED dark mode disabled (Light mode restored)"
            "controlCenterBlur" -> if (enabled) "Remove Control Center blur" else "Control Center blur restored"
            "lock60Hz" -> if (enabled) "Refresh rate set 60Hz" else "Refresh rate set Auto"
            "hyperOsAodDisabled" -> if (enabled) "Disable Always-On Display (AOD)" else "Always-On Display (AOD) enabled"
            "hyperOsAurogonFreezer" -> if (enabled) "HyperOS Aurogon process freezer enabled" else "HyperOS Aurogon process freezer disabled"
            "hyperOsSuperPowerClean" -> if (enabled) "HyperOS SuperPowerClean RAM purge enabled" else "HyperOS SuperPowerClean RAM purge disabled"
            "hyperOsTouchBoostDisabled" -> if (enabled) "Clamp Xiaomi pnpmgr & touch boost" else "Xiaomi pnpmgr & touch boost restored"
            "hyperOsFiveGPowerOpt" -> if (enabled) "5G smart modem power saving enabled" else "5G smart modem power saving disabled"
            "hyperOsLock60Hz" -> if (enabled) "HyperOS 60Hz refresh rate lock enabled" else "HyperOS 60Hz refresh rate lock disabled"
            "hyperOsUltraSuperSaveUi" -> if (enabled) "Ultra SuperSave UI (Black wallpaper & Notification Panel off) enabled" else "Ultra SuperSave UI disabled (Wallpaper & Notification Panel restored)"
            "deepSleepScreenOff" -> if (enabled) "Screen-off deep sleep daemon enabled" else "Screen-off deep sleep daemon disabled"
            "developerProcessLimit" -> if (enabled) "Cached background process limit enabled" else "Cached process limit restored"
            "balanceCpuTweaks" -> if (enabled) "Balanced CPU & schedutil tuning enabled" else "Balanced CPU tuning disabled"
            "balanceRamScaling" -> if (enabled) "Balanced 3-tier RAM scaling enabled" else "Balanced RAM scaling disabled"
            "balanceCfsScheduler" -> if (enabled) "Balanced CFS scheduler tuning enabled" else "CFS scheduler restored"
            "balanceGpuOptimization" -> if (enabled) "Balanced GPU DVFS optimization enabled" else "Balanced GPU optimization disabled"
            "balanceStorageIo" -> if (enabled) "Balanced adaptive storage I/O enabled" else "Balanced storage I/O disabled"
            "balanceNetworkBbr" -> if (enabled) "Balanced TCP BBR network tuning enabled" else "Balanced network tuning disabled"
            "perfGovLock" -> if (enabled) "Performance CPU governor & clock lock enabled" else "Performance CPU governor lock disabled"
            "perfAllCores" -> if (enabled) "Force all 8 CPU cores online" else "CPU core online lock disabled"
            "perfSchedtune" -> if (enabled) "Performance SchedTune thread boost enabled" else "SchedTune thread boost disabled"
            "perfGpuBoost" -> if (enabled) "Adreno GPU boost enabled" else "Adreno GPU boost disabled"
            "perfGpuNoNap" -> if (enabled) "GPU force no-nap enabled" else "GPU force no-nap disabled"
            "perfDdrBus" -> if (enabled) "DDR memory bus boost enabled" else "DDR memory bus boost disabled"
            "perfStorage512" -> if (enabled) "512KB storage I/O multi-queue enabled" else "Storage I/O multi-queue disabled"
            "perfTouchBoost" -> if (enabled) "Performance touch boost enabled" else "Performance touch boost disabled"
            "perfLmkTuning" -> if (enabled) "Gaming LMK & RAM pool tuning enabled" else "Gaming LMK tuning disabled"
            "hyperOsPowerSaver" -> if (enabled) "HyperOS Battery Saver enabled" else "HyperOS Battery Saver disabled"
            else -> if (enabled) "$toggleKey enabled" else "$toggleKey disabled"
        }
    }

    fun onUpdateTweak(toggleKey: String, enabled: Boolean) {
        if (toggleKey == "hyperOsPowerSaver") {
            onToggleHyperOsPowerSaver(enabled)
            return
        }
        viewModelScope.launch {
            val current = configState.value
            val updated = when (toggleKey) {
                "cpuFreqCap" -> current.copy(cpuFreqCapEnabled = enabled)
                "schedutilGov" -> current.copy(schedutilGovernorEnabled = enabled)
                "schedutilRate" -> current.copy(schedutilRateLimitsEnabled = enabled)
                "twoCoresOffline" -> current.copy(twoCoresOfflineBelow20Enabled = enabled)
                "inputTouchBoost" -> current.copy(inputTouchBoostDisabled = enabled)
                "gpuPowerLimit" -> current.copy(gpuPowerLimitEnabled = enabled)
                "adrenoIdler" -> current.copy(adrenoIdlerEnabled = enabled)
                "storageIo" -> current.copy(storageIoQueueEnabled = enabled)
                "dynamicFsync" -> current.copy(dynamicFsyncEnabled = enabled)
                "vmDirtyWriteback" -> current.copy(vmDirtyWritebackEnabled = enabled)
                "tcpBbr" -> current.copy(tcpBbrCongestionEnabled = enabled)
                "gmsDoze" -> current.copy(gmsDozeEnabled = enabled)
                "lpmSleep" -> current.copy(lpmSleepEnabled = enabled)
                "doubleTapWake" -> current.copy(doubleTapWakePreserved = enabled)
                "schedtuneTopApp" -> current.copy(schedtuneTopAppEnabled = enabled)
                "powerEfficientWorkqueue" -> current.copy(powerEfficientWorkqueueEnabled = enabled)
                "backgroundScanning" -> current.copy(backgroundScanningDisabled = enabled)
                "touchDozeMode" -> current.copy(touchDozeModeEnabled = enabled)
                "batterySaverNoDark" -> current.copy(
                    systemBatterySaverWithoutDarkEnabled = enabled,
                    systemBatterySaverEnabled = if (!enabled) false else current.systemBatterySaverEnabled
                )
                "hyperOsAodDisabled" -> current.copy(hyperOsAodDisabled = enabled)
                "hyperOsAurogonFreezer" -> current.copy(hyperOsAurogonFreezerEnabled = enabled)
                "hyperOsSuperPowerClean" -> current.copy(hyperOsSuperPowerCleanEnabled = enabled)
                "hyperOsTouchBoostDisabled" -> current.copy(hyperOsTouchBoostDisabled = enabled)
                "hyperOsFiveGPowerOpt" -> current.copy(hyperOsFiveGPowerOptEnabled = enabled)
                "hyperOsUltraSuperSaveUi" -> current.copy(hyperOsUltraSuperSaveUiEnabled = enabled)
                "hyperOsLock60Hz" -> {
                    val targetHz = if (enabled) 60 else 0
                    current.copy(
                        hyperOsLock60HzEnabled = enabled,
                        lock60HzRefreshRateEnabled = enabled,
                        refreshRateHz = targetHz
                    )
                }
                "systemBatterySaver" -> current.copy(
                    systemBatterySaverEnabled = enabled,
                    systemBatterySaverWithoutDarkEnabled = if (!enabled) false else current.systemBatterySaverWithoutDarkEnabled
                )
                "systemDarkMode" -> current.copy(systemDarkModeEnabled = enabled)
                "controlCenterBlur" -> current.copy(controlCenterBlurEnabled = enabled)
                "lock60Hz" -> {
                    val targetHz = if (enabled) 60 else 0
                    current.copy(
                        lock60HzRefreshRateEnabled = enabled,
                        hyperOsLock60HzEnabled = if (!enabled) false else current.hyperOsLock60HzEnabled,
                        refreshRateHz = targetHz
                    )
                }
                "deepSleepScreenOff" -> current.copy(deepSleepScreenOffEnabled = enabled)
                "developerProcessLimit" -> current.copy(developerProcessLimitEnabled = enabled)
                "balanceCpuTweaks" -> current.copy(
                    balanceCpuTweaksEnabled = enabled,
                    cpuFreqCapEnabled = enabled,
                    schedutilGovernorEnabled = enabled,
                    schedutilRateLimitsEnabled = enabled
                )
                "balanceRamScaling" -> current.copy(balanceRamScalingEnabled = enabled)
                "balanceCfsScheduler" -> current.copy(balanceCfsSchedulerEnabled = enabled)
                "balanceGpuOptimization" -> current.copy(balanceGpuOptimizationEnabled = enabled)
                "balanceStorageIo" -> current.copy(balanceStorageIoEnabled = enabled)
                "balanceNetworkBbr" -> current.copy(balanceNetworkBbrEnabled = enabled)
                "perfGovLock" -> current.copy(perfCpuGovernorLockEnabled = enabled)
                "perfAllCores" -> current.copy(perfAllCoresOnlineEnabled = enabled)
                "perfSchedtune" -> current.copy(perfSchedtuneBoostEnabled = enabled)
                "perfGpuBoost" -> current.copy(perfGpuAdrenoBoostEnabled = enabled)
                "perfGpuNoNap" -> current.copy(perfGpuNoNapEnabled = enabled)
                "perfDdrBus" -> current.copy(perfDdrBusBoostEnabled = enabled)
                "perfStorage512" -> current.copy(perfStorageQueue512Enabled = enabled)
                "perfTouchBoost" -> current.copy(perfTouchBoostEnabled = enabled)
                "perfLmkTuning" -> current.copy(perfLmkTuningEnabled = enabled)
                else -> current
            }
            dao.update(updated)
            _hasUnappliedChanges.value = true
        }
    }

    fun onToggleHyperOsPowerSaver(enabled: Boolean) {
        viewModelScope.launch {
            val current = configState.value
            val updated = if (enabled) {
                // Switch view to HyperOS Battery Saver tweaks without applying yet (wait for Swipe to apply)
                // NOTE: hyperOsUltraSuperSaveUiEnabled MUST remain OFF (false) by default unless manually enabled by the user!
                val anyHyperOsSelected = current.hyperOsAodDisabled ||
                    current.hyperOsAurogonFreezerEnabled ||
                    current.hyperOsSuperPowerCleanEnabled ||
                    current.hyperOsTouchBoostDisabled ||
                    current.hyperOsFiveGPowerOptEnabled ||
                    current.hyperOsLock60HzEnabled ||
                    current.controlCenterBlurEnabled ||
                    current.systemBatterySaverEnabled
                if (anyHyperOsSelected) {
                    current.copy(
                        activeProfile = "POWERSAVE",
                        hyperOsPowerSaverEnabled = true
                    )
                } else {
                    current.copy(
                        activeProfile = "POWERSAVE",
                        hyperOsPowerSaverEnabled = true,
                        hyperOsAodDisabled = true,
                        hyperOsAurogonFreezerEnabled = true,
                        hyperOsSuperPowerCleanEnabled = true,
                        hyperOsTouchBoostDisabled = true,
                        touchDozeModeEnabled = true,
                        backgroundScanningDisabled = true,
                        hyperOsFiveGPowerOptEnabled = true,
                        hyperOsLock60HzEnabled = true,
                        lock60HzRefreshRateEnabled = true,
                        refreshRateHz = 60,
                        controlCenterBlurEnabled = true,
                        systemBatterySaverEnabled = true,
                        systemBatterySaverWithoutDarkEnabled = true,
                        hyperOsUltraSuperSaveUiEnabled = false
                    )
                }
            } else {
                current.copy(
                    hyperOsPowerSaverEnabled = false
                )
            }
            dao.update(updated)
            _hasUnappliedChanges.value = true
        }
    }

    fun onSelectRefreshRate(hz: Int) {
        viewModelScope.launch {
            val current = configState.value
            val updated = current.copy(
                refreshRateHz = hz,
                lock60HzRefreshRateEnabled = (hz == 60),
                hyperOsLock60HzEnabled = (hz == 60)
            )
            dao.update(updated)
            _hasUnappliedChanges.value = true
        }
    }

    fun onUpdateBackgroundAllowedApps(apps: Set<String>) {
        viewModelScope.launch {
            val current = configState.value
            val pkgString = apps.joinToString(",")
            val updated = current.copy(backgroundAllowedApps = pkgString)
            dao.update(updated)
            _hasUnappliedChanges.value = true
        }
    }

    fun onSelectPerformanceSubMode(subMode: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val normalized = when (subMode.uppercase()) {
                "HEAVY", "EXTREME" -> "EXTREME"
                "ULTRA" -> "ULTRA"
                else -> "LITE"
            }
            val current = configState.value
            val hasRoot = _telemetry.value.isRootGranted || RootBridge.isRootAvailable()
            val updated = applyPerformanceTierPreset(current, normalized).copy(
                isMasterApplied = if (hasRoot) true else current.isMasterApplied,
                lastAppliedTimestamp = System.currentTimeMillis()
            )
            dao.update(updated)

            val tierLabel = when (normalized) {
                "ULTRA" -> "Ultra"
                "EXTREME" -> "Extreme"
                else -> "Lite"
            }
            val logMsg = "Performance mode set to $tierLabel"
            _statusMessage.value = logMsg
            LogRepository.addLog(
                category = LogCategory.PROFILE,
                message = logMsg,
                isSuccess = true
            )
            if (hasRoot) {
                RootBridge.revertAllTweaks(getApplication())
                RootBridge.applyPowersaveTweaks(getApplication(), updated)
                dao.updateBackupStatus(true, RootBridge.BACKUP_FILE_PATH)
                refreshTelemetry()
            }
            _isBusy.value = false
        }
    }

    fun onUpdateDeepSleepWhitelist(whitelist: String) {
        viewModelScope.launch {
            val current = configState.value
            val updated = current.copy(deepSleepWhitelist = whitelist)
            dao.update(updated)
            _hasUnappliedChanges.value = true
        }
    }

    fun onUpdateDeepSleepDelay(minutes: Int) {
        viewModelScope.launch {
            val current = configState.value
            val clp = minutes.coerceIn(1, 30)
            val updated = current.copy(deepSleepDelayMinutes = clp)
            dao.update(updated)
            _hasUnappliedChanges.value = true
        }
    }

    fun onUpdateDeveloperProcessLimit(limit: Int) {
        viewModelScope.launch {
            val current = configState.value
            val clp = limit.coerceIn(1, 8)
            val updated = current.copy(developerProcessLimit = clp)
            dao.update(updated)
            _hasUnappliedChanges.value = true
        }
    }

    fun onUpdateBalanceCpuCap(percent: Int) {
        viewModelScope.launch {
            val current = configState.value
            val clp = percent.coerceIn(50, 95)
            val updated = current.copy(balanceCpuCapPercent = clp)
            dao.update(updated)
            _hasUnappliedChanges.value = true
        }
    }

    fun onUpdateCoreTopology(coreCount: Int, threshold: Int) {
        viewModelScope.launch {
            val current = configState.value
            val clpCores = coreCount.coerceIn(1, 2)
            val updated = current.copy(
                offlineCoreCount = clpCores,
                offlineBatteryThreshold = threshold
            )
            dao.update(updated)
            _hasUnappliedChanges.value = true
        }
    }
}
