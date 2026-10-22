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

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        viewModelScope.launch {
            if (dao.getConfigDirect() == null) {
                dao.insertOrUpdate(TweakConfigEntity())
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

    private fun applyPerformanceTierPreset(
        base: TweakConfigEntity,
        rawSubMode: String
    ): TweakConfigEntity {
        val subMode = when (rawSubMode.uppercase()) {
            "HEAVY", "EXTREME" -> "EXTREME"
            "ULTRA" -> "ULTRA"
            else -> "LITE"
        }
        return when (subMode) {
            "ULTRA" -> base.copy(
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
            "EXTREME" -> base.copy(
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
            else -> base.copy(
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
                            current.copy(
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
                            current.copy(
                                isMasterApplied = true,
                                lastAppliedTimestamp = System.currentTimeMillis(),
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
                                gmsDozeEnabled = true,
                                lpmSleepEnabled = true,
                                doubleTapWakePreserved = true,
                                schedtuneTopAppEnabled = true,
                                powerEfficientWorkqueueEnabled = true
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
                val allOff = current.copy(
                    isMasterApplied = false,
                    lastAppliedTimestamp = System.currentTimeMillis(),
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
                    systemBatterySaverWithoutDarkEnabled = false,
                    lock60HzRefreshRateEnabled = false,
                    deepSleepScreenOffEnabled = false,
                    developerProcessLimitEnabled = false,
                    hyperOsPowerSaverEnabled = false
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
            if (_telemetry.value.isRootGranted) {
                RootBridge.revertAllTweaks(getApplication())
            }
            val current = configState.value
            val allOff = current.copy(
                isMasterApplied = false,
                lastAppliedTimestamp = System.currentTimeMillis(),
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
                systemBatterySaverEnabled = false,
                systemBatterySaverWithoutDarkEnabled = false,
                lock60HzRefreshRateEnabled = false,
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
                hyperOsLock60HzEnabled = false
            )
            dao.update(allOff)
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
            val current = configState.value
            val updated = when (profile) {
                "PERFORMANCE" -> {
                    val tier = when (current.performanceSubMode.uppercase()) {
                        "HEAVY", "EXTREME" -> "EXTREME"
                        "ULTRA" -> "ULTRA"
                        else -> "LITE"
                    }
                    applyPerformanceTierPreset(current, tier)
                }
                "BALANCE" -> {
                    val anyBal = current.balanceCpuTweaksEnabled ||
                        current.balanceRamScalingEnabled ||
                        current.balanceCfsSchedulerEnabled ||
                        current.balanceGpuOptimizationEnabled ||
                        current.balanceStorageIoEnabled ||
                        current.balanceNetworkBbrEnabled ||
                        current.deepSleepScreenOffEnabled
                    if (!anyBal) {
                        current.copy(
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
                    } else {
                        current.copy(activeProfile = "BALANCE")
                    }
                }
                else -> current.copy(activeProfile = "POWERSAVE")
            }

            dao.update(updated)

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

            _statusMessage.value = "$profileLogLabel selected"
            LogRepository.addLog(
                category = LogCategory.PROFILE,
                message = "$profileLogLabel enabled",
                isSuccess = true
            )

            if (updated.isMasterApplied && _telemetry.value.isRootGranted) {
                RootBridge.applyPowersaveTweaks(getApplication(), updated)
                refreshTelemetry()
            }
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
            "systemBatterySaver" -> if (enabled) "HyperOS PowerKeeper SuperSave enabled" else "HyperOS PowerKeeper SuperSave disabled"
            "batterySaverNoDark" -> if (enabled) "Preserve light mode in battery saver" else "Battery saver dark mode default"
            "systemDarkMode" -> if (enabled) "Force AMOLED dark mode enabled" else "Force AMOLED dark mode disabled"
            "controlCenterBlur" -> if (enabled) "Remove Control Center blur" else "Control Center blur restored"
            "lock60Hz" -> if (enabled) "Refresh rate set 60Hz" else "Refresh rate set Auto"
            "hyperOsAodDisabled" -> if (enabled) "Disable Always-On Display (AOD)" else "Always-On Display (AOD) enabled"
            "hyperOsAurogonFreezer" -> if (enabled) "HyperOS Aurogon process freezer enabled" else "HyperOS Aurogon process freezer disabled"
            "hyperOsSuperPowerClean" -> if (enabled) "HyperOS SuperPowerClean RAM purge enabled" else "HyperOS SuperPowerClean RAM purge disabled"
            "hyperOsTouchBoostDisabled" -> if (enabled) "Clamp Xiaomi pnpmgr & touch boost" else "Xiaomi pnpmgr & touch boost restored"
            "hyperOsFiveGPowerOpt" -> if (enabled) "5G smart modem power saving enabled" else "5G smart modem power saving disabled"
            "hyperOsLock60Hz" -> if (enabled) "HyperOS 60Hz refresh rate lock enabled" else "HyperOS 60Hz refresh rate lock disabled"
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
                "batterySaverNoDark" -> current.copy(systemBatterySaverWithoutDarkEnabled = enabled)
                "hyperOsAodDisabled" -> current.copy(hyperOsAodDisabled = enabled)
                "hyperOsAurogonFreezer" -> current.copy(hyperOsAurogonFreezerEnabled = enabled)
                "hyperOsSuperPowerClean" -> current.copy(hyperOsSuperPowerCleanEnabled = enabled)
                "hyperOsTouchBoostDisabled" -> current.copy(hyperOsTouchBoostDisabled = enabled)
                "hyperOsFiveGPowerOpt" -> current.copy(hyperOsFiveGPowerOptEnabled = enabled)
                "hyperOsLock60Hz" -> {
                    val targetHz = if (enabled) 60 else 0
                    if (_telemetry.value.isRootGranted) {
                        RootBridge.setDisplayRefreshRate(targetHz)
                    }
                    current.copy(
                        hyperOsLock60HzEnabled = enabled,
                        lock60HzRefreshRateEnabled = enabled,
                        refreshRateHz = targetHz
                    )
                }
                "systemBatterySaver" -> {
                    if (_telemetry.value.isRootGranted) {
                        RootBridge.setSystemBatterySaver(enabled)
                    }
                    current.copy(systemBatterySaverEnabled = enabled)
                }
                "systemDarkMode" -> {
                    if (_telemetry.value.isRootGranted) {
                        RootBridge.setSystemDarkMode(enabled)
                    }
                    current.copy(systemDarkModeEnabled = enabled)
                }
                "controlCenterBlur" -> {
                    if (_telemetry.value.isRootGranted) {
                        RootBridge.setControlCenterBlur(enabled)
                    }
                    current.copy(controlCenterBlurEnabled = enabled)
                }
                "lock60Hz" -> {
                    val targetHz = if (enabled) 60 else 0
                    if (_telemetry.value.isRootGranted) {
                        RootBridge.setDisplayRefreshRate(targetHz)
                    }
                    current.copy(lock60HzRefreshRateEnabled = enabled, refreshRateHz = targetHz)
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
                "hyperOsPowerSaver" -> current.copy(
                    hyperOsPowerSaverEnabled = enabled,
                    hyperOsAodDisabled = enabled,
                    hyperOsAurogonFreezerEnabled = enabled,
                    hyperOsSuperPowerCleanEnabled = enabled,
                    hyperOsTouchBoostDisabled = enabled,
                    touchDozeModeEnabled = enabled,
                    backgroundScanningDisabled = enabled,
                    hyperOsFiveGPowerOptEnabled = enabled,
                    hyperOsLock60HzEnabled = enabled,
                    refreshRateHz = if (enabled) 60 else 0,
                    controlCenterBlurEnabled = enabled,
                    systemBatterySaverEnabled = enabled
                )
                else -> current
            }
            dao.update(updated)

            LogRepository.addLog(
                category = LogCategory.TWEAK,
                message = humanReadableTweakLog(toggleKey, enabled),
                isSuccess = true
            )

            if (current.isMasterApplied && _telemetry.value.isRootGranted) {
                _statusMessage.value = humanReadableTweakLog(toggleKey, enabled)
                RootBridge.applyPowersaveTweaks(getApplication(), updated)
                refreshTelemetry()
            }
        }
    }

    fun onToggleHyperOsPowerSaver(enabled: Boolean) {
        viewModelScope.launch {
            val current = configState.value
            val updated = current.copy(
                hyperOsPowerSaverEnabled = enabled,
                hyperOsAodDisabled = enabled,
                hyperOsAurogonFreezerEnabled = enabled,
                hyperOsSuperPowerCleanEnabled = enabled,
                hyperOsTouchBoostDisabled = enabled,
                touchDozeModeEnabled = enabled,
                backgroundScanningDisabled = enabled,
                hyperOsFiveGPowerOptEnabled = enabled,
                hyperOsLock60HzEnabled = enabled,
                refreshRateHz = if (enabled) 60 else 0,
                controlCenterBlurEnabled = enabled,
                systemBatterySaverEnabled = enabled
            )
            dao.update(updated)
            val logMsg = if (enabled) {
                "HyperOS Power Saver all tweaks enabled"
            } else {
                "HyperOS Power Saver disabled"
            }
            _statusMessage.value = logMsg
            LogRepository.addLog(
                category = LogCategory.TWEAK,
                message = logMsg,
                isSuccess = true
            )
            if (current.isMasterApplied && _telemetry.value.isRootGranted) {
                RootBridge.applyPowersaveTweaks(getApplication(), updated)
                refreshTelemetry()
            }
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
            if (_telemetry.value.isRootGranted) {
                RootBridge.setDisplayRefreshRate(hz)
            }
            val logMsg = if (hz > 0) "Refresh rate set ${hz}Hz" else "Refresh rate set Auto"
            _statusMessage.value = logMsg
            LogRepository.addLog(
                category = LogCategory.TWEAK,
                message = logMsg,
                isSuccess = true
            )
            if (current.isMasterApplied && _telemetry.value.isRootGranted) {
                RootBridge.applyPowersaveTweaks(getApplication(), updated)
                refreshTelemetry()
            }
        }
    }

    fun onUpdateBackgroundAllowedApps(apps: Set<String>) {
        viewModelScope.launch {
            val current = configState.value
            val pkgString = apps.joinToString(",")
            val updated = current.copy(backgroundAllowedApps = pkgString)
            dao.update(updated)
            _statusMessage.value = "Background app whitelist updated (${apps.size} apps allowed)"
            if (current.isMasterApplied && _telemetry.value.isRootGranted && current.hyperOsPowerSaverEnabled) {
                RootBridge.applyPowersaveTweaks(getApplication(), updated)
            }
        }
    }

    fun onSelectPerformanceSubMode(subMode: String) {
        viewModelScope.launch {
            val normalized = when (subMode.uppercase()) {
                "HEAVY", "EXTREME" -> "EXTREME"
                "ULTRA" -> "ULTRA"
                else -> "LITE"
            }
            val current = configState.value
            val updated = applyPerformanceTierPreset(current, normalized)
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
            if (updated.isMasterApplied && _telemetry.value.isRootGranted) {
                RootBridge.applyPowersaveTweaks(getApplication(), updated)
                refreshTelemetry()
            }
        }
    }

    fun onUpdateDeepSleepWhitelist(whitelist: String) {
        viewModelScope.launch {
            val current = configState.value
            val updated = current.copy(deepSleepWhitelist = whitelist)
            dao.update(updated)
            if (current.isMasterApplied && _telemetry.value.isRootGranted && current.deepSleepScreenOffEnabled) {
                RootBridge.applyPowersaveTweaks(getApplication(), updated)
            }
        }
    }

    fun onUpdateDeepSleepDelay(minutes: Int) {
        viewModelScope.launch {
            val current = configState.value
            val clp = minutes.coerceIn(1, 30)
            val updated = current.copy(deepSleepDelayMinutes = clp)
            dao.update(updated)
            LogRepository.addLog(
                category = LogCategory.TWEAK,
                message = "Deep sleep delay set to ${clp}m",
                isSuccess = true
            )
            if (current.isMasterApplied && _telemetry.value.isRootGranted && current.deepSleepScreenOffEnabled) {
                RootBridge.applyPowersaveTweaks(getApplication(), updated)
            }
        }
    }

    fun onUpdateDeveloperProcessLimit(limit: Int) {
        viewModelScope.launch {
            val current = configState.value
            val clp = limit.coerceIn(1, 8)
            val updated = current.copy(developerProcessLimit = clp)
            dao.update(updated)
            LogRepository.addLog(
                category = LogCategory.TWEAK,
                message = "Background process limit set to $clp",
                isSuccess = true
            )
            if (current.isMasterApplied && _telemetry.value.isRootGranted && current.developerProcessLimitEnabled) {
                RootBridge.applyPowersaveTweaks(getApplication(), updated)
            }
        }
    }

    fun onUpdateBalanceCpuCap(percent: Int) {
        viewModelScope.launch {
            val current = configState.value
            val clp = percent.coerceIn(50, 95)
            val updated = current.copy(balanceCpuCapPercent = clp)
            dao.update(updated)
            LogRepository.addLog(
                category = LogCategory.TWEAK,
                message = "Balance CPU cap set ${clp}%",
                isSuccess = true
            )
            if (current.isMasterApplied && _telemetry.value.isRootGranted && current.activeProfile == "BALANCE") {
                RootBridge.applyPowersaveTweaks(getApplication(), updated)
            }
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
            LogRepository.addLog(
                category = LogCategory.TWEAK,
                message = "Park $clpCores big cores below ${threshold}%",
                isSuccess = true
            )
            if (current.isMasterApplied && _telemetry.value.isRootGranted && current.twoCoresOfflineBelow20Enabled) {
                RootBridge.applyPowersaveTweaks(getApplication(), updated)
                refreshTelemetry()
            }
        }
    }
}
