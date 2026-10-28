package com.voltpower.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voltpower.data.db.AppDatabase
import com.voltpower.data.db.TweakConfigEntity
import com.voltpower.data.log.LogCategory
import com.voltpower.data.log.LogEntry
import com.voltpower.data.log.LogRepository
import com.voltpower.data.root.HyperOsProfileTemplate
import com.voltpower.data.root.KernelRamLabRepository
import com.voltpower.data.root.KernelRamLiveSnapshot
import com.voltpower.data.root.KernelTweakSpec
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

    private val _kernelSnapshot = MutableStateFlow(
        KernelRamLiveSnapshot(
            nodeValues = KernelRamLabRepository.tweakSpecs.associate { it.id to it.wasValue },
            daemons = KernelRamLabRepository.defaultDaemons
        )
    )
    val kernelSnapshot: StateFlow<KernelRamLiveSnapshot> = _kernelSnapshot.asStateFlow()

    private val _stagedKernelTweaks = MutableStateFlow<Map<String, String>>(emptyMap())
    val stagedKernelTweaks: StateFlow<Map<String, String>> = _stagedKernelTweaks.asStateFlow()

    private val _appliedKernelTweaks = MutableStateFlow<Map<String, String>>(emptyMap())

    private val _selectedHyperOsTemplate = MutableStateFlow<HyperOsProfileTemplate?>(null)
    val selectedHyperOsTemplate: StateFlow<HyperOsProfileTemplate?> = _selectedHyperOsTemplate.asStateFlow()

    private val _isBalanceScreenOffSaverActive = MutableStateFlow(false)
    val isBalanceScreenOffSaverActive: StateFlow<Boolean> = _isBalanceScreenOffSaverActive.asStateFlow()

    private val _lastKernelActionBanner = MutableStateFlow<String?>(null)
    val lastKernelActionBanner: StateFlow<String?> = _lastKernelActionBanner.asStateFlow()

    private var screenReceiverRegistered = false
    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!_isBalanceScreenOffSaverActive.value) return
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    viewModelScope.launch {
                        val msg = KernelRamLabRepository.applyImmediateScreenOffBatterySaver(
                            screenOff = true,
                            keepBatterySaverOnWhenScreenOn = false
                        )
                        _lastKernelActionBanner.value = msg
                        LogRepository.addLog(
                            category = LogCategory.TWEAK,
                            message = msg,
                            isSuccess = true
                        )
                    }
                }
                Intent.ACTION_SCREEN_ON -> {
                    viewModelScope.launch {
                        val cfg = configState.value
                        val keepOn = cfg.activeProfile == "POWERSAVE" &&
                            cfg.hyperOsPowerSaverEnabled &&
                            cfg.systemBatterySaverWithoutDarkEnabled
                        val msg = KernelRamLabRepository.applyImmediateScreenOffBatterySaver(
                            screenOff = false,
                            keepBatterySaverOnWhenScreenOn = keepOn
                        )
                        _lastKernelActionBanner.value = msg
                        LogRepository.addLog(
                            category = LogCategory.TWEAK,
                            message = msg,
                            isSuccess = true
                        )
                    }
                }
            }
        }
    }

    init {
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
            }
            application.registerReceiver(screenStateReceiver, filter)
            screenReceiverRegistered = true
        } catch (_: Exception) {
            screenReceiverRegistered = false
        }

        viewModelScope.launch {
            var existingCfg = dao.getConfigDirect()
            if (existingCfg == null) {
                existingCfg = TweakConfigEntity()
                dao.insertOrUpdate(existingCfg)
            }

            // Restore persisted HyperOS template & applied/staged tweaks so closing app from Recents preserves state
            val restoredTemplate = HyperOsProfileTemplate.entries.find {
                it.id == existingCfg.hyperOsActiveTemplate
            }
            val restoredApplied = parseTweaksMap(existingCfg.hyperOsAppliedTweaks)
            val restoredStaged = parseTweaksMap(existingCfg.hyperOsStagedTweaks)
            _selectedHyperOsTemplate.value = restoredTemplate
            _appliedKernelTweaks.value = restoredApplied
            _stagedKernelTweaks.value = restoredStaged
            _isBalanceScreenOffSaverActive.value = existingCfg.hyperOsBalanceScreenOffArmed

            // Permanently re-enforce applied HyperOS tweaks in RAM on app startup
            if (restoredApplied.isNotEmpty()) {
                KernelRamLabRepository.enforceAppliedTweaksInRam(restoredApplied)
            }

            launch {
                _installedApps.value = appInfoRepository.getInstalledApps()
            }
            refreshTelemetry()
            refreshKernelSnapshot()
            var heartbeatTick = 0
            while (true) {
                delay(2500)
                refreshTelemetry()
                heartbeatTick++
                if (heartbeatTick % 4 == 0 && _appliedKernelTweaks.value.isNotEmpty()) {
                    KernelRamLabRepository.enforceAppliedTweaksInRam(_appliedKernelTweaks.value)
                }
            }
        }
    }

    private fun serializeTweaksMap(map: Map<String, String>): String {
        return map.entries.joinToString(";") { "${it.key}=${it.value}" }
    }

    private fun parseTweaksMap(raw: String): Map<String, String> {
        if (raw.isBlank()) return emptyMap()
        return raw.split(";")
            .mapNotNull { entry ->
                val idx = entry.indexOf('=')
                if (idx <= 0) null
                else entry.substring(0, idx).trim() to entry.substring(idx + 1).trim()
            }
            .toMap()
    }

    private suspend fun persistHyperOsKernelState() {
        val current = dao.getConfigDirect() ?: configState.value
        val updated = current.copy(
            hyperOsActiveTemplate = _selectedHyperOsTemplate.value?.id.orEmpty(),
            hyperOsAppliedTweaks = serializeTweaksMap(_appliedKernelTweaks.value),
            hyperOsStagedTweaks = serializeTweaksMap(_stagedKernelTweaks.value),
            hyperOsBalanceScreenOffArmed = _isBalanceScreenOffSaverActive.value
        )
        dao.update(updated)
    }

    override fun onCleared() {
        super.onCleared()
        if (screenReceiverRegistered) {
            try {
                getApplication<Application>().unregisterReceiver(screenStateReceiver)
            } catch (_: Exception) {
            }
            screenReceiverRegistered = false
        }
    }

    /**
     * Flushes and reverts all HyperOS RAM tweaks back to their original state whenever the user
     * switches to standard Powersave (not HyperOS powersaver), Balance, or Performance profile.
     * HyperOS tweaks are only allowed to coexist alongside HyperOS Power Saver in Tweaks.
     */
    private suspend fun flushHyperOsRamTweaksForProfileSwitch(reasonLabel: String) {
        val hadHyperOsState = _appliedKernelTweaks.value.isNotEmpty() ||
            _stagedKernelTweaks.value.isNotEmpty() ||
            _selectedHyperOsTemplate.value != null ||
            _isBalanceScreenOffSaverActive.value

        _stagedKernelTweaks.value = emptyMap()
        _appliedKernelTweaks.value = emptyMap()
        _selectedHyperOsTemplate.value = null
        _isBalanceScreenOffSaverActive.value = false
        persistHyperOsKernelState()

        val hasRoot = _telemetry.value.isRootGranted || RootBridge.isRootAvailable()
        if (hasRoot) {
            KernelRamLabRepository.restoreAllWasValues()
            refreshKernelSnapshot()
        }

        if (hadHyperOsState) {
            val flushMsg = "HyperOS RAM tweaks flushed & reverted to original state ($reasonLabel)"
            _lastKernelActionBanner.value = flushMsg
            LogRepository.addLog(
                category = LogCategory.TWEAK,
                message = flushMsg,
                isSuccess = true
            )
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
                val isCoexistingHyperOsSaver =
                    toApply.activeProfile == "POWERSAVE" && toApply.hyperOsPowerSaverEnabled
                if (!isCoexistingHyperOsSaver) {
                    flushHyperOsRamTweaksForProfileSwitch("${toApply.activeProfile.lowercase()} profile")
                }
                RootBridge.revertAllTweaks(getApplication())
                RootBridge.applyPowersaveTweaks(getApplication(), toApply)
                if (isCoexistingHyperOsSaver && _appliedKernelTweaks.value.isNotEmpty()) {
                    KernelRamLabRepository.applyBatchKernelTweaks(_appliedKernelTweaks.value)
                    refreshKernelSnapshot()
                }
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
                flushHyperOsRamTweaksForProfileSwitch("engine turned off")
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
            flushHyperOsRamTweaksForProfileSwitch("revert to stock")
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

            // Standard Powersave, Balance, and Performance flush HyperOS RAM tweaks back to original state
            flushHyperOsRamTweaksForProfileSwitch(profileLogLabel)

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
                message = "$profileLogLabel enabled (HyperOS RAM tweaks flushed & previous profile reverted)",
                isSuccess = true
            )
            _isBusy.value = false
        }
    }

    /**
     * Used in the Tweaks section so the user can freely browse/explore Powersave, Balance,
     * and Performance tweak categories without immediately applying them to the system.
     * Switching to Balance, Performance, or non-HyperOS Powersave flushes HyperOS RAM tweaks.
     */
    fun onExploreProfileInTweaks(profile: String) {
        viewModelScope.launch {
            val current = configState.value
            if (current.activeProfile == profile) return@launch
            val updated = current.copy(activeProfile = profile)
            dao.update(updated)
            _hasUnappliedChanges.value = true

            val isCoexistingHyperOsSaver =
                updated.activeProfile == "POWERSAVE" && updated.hyperOsPowerSaverEnabled
            if (!isCoexistingHyperOsSaver) {
                flushHyperOsRamTweaksForProfileSwitch("${profile.lowercase()} profile")
            }
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
            flushHyperOsRamTweaksForProfileSwitch("performance ($normalized) profile")
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
     * HyperOS tweaks can coexist ONLY alongside HyperOS Power Saver; if applying standard Powersave,
     * Balance, or Performance, HyperOS tweaks in RAM are flushed back to original state.
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

            val isCoexistingHyperOsSaver =
                updated.activeProfile == "POWERSAVE" && updated.hyperOsPowerSaverEnabled

            if (!isCoexistingHyperOsSaver) {
                val label = if (updated.activeProfile == "POWERSAVE") {
                    "standard powersave profile"
                } else {
                    "${updated.activeProfile.lowercase()} profile"
                }
                flushHyperOsRamTweaksForProfileSwitch(label)
            }

            if (hasRoot) {
                _statusMessage.value = "Applying selected tweaks..."
                // 1. Revert previous state first so any unchecked tweaks return to system default
                RootBridge.revertAllTweaks(getApplication())

                // 2. Apply all selected kernel, system UI, display, and profile tweaks atomically in order
                RootBridge.applyPowersaveTweaks(getApplication(), updated)

                // 3. If HyperOS Power Saver is active, keep HyperOS RAM tweaks applied alongside it
                if (isCoexistingHyperOsSaver && _appliedKernelTweaks.value.isNotEmpty()) {
                    KernelRamLabRepository.applyBatchKernelTweaks(_appliedKernelTweaks.value)
                    refreshKernelSnapshot()
                }

                dao.updateBackupStatus(true, RootBridge.BACKUP_FILE_PATH)
                refreshTelemetry()

                val msg = if (isCoexistingHyperOsSaver && _appliedKernelTweaks.value.isNotEmpty()) {
                    "HyperOS Power Saver + ${_appliedKernelTweaks.value.size} HyperOS RAM tweaks active"
                } else if (isCoexistingHyperOsSaver) {
                    "HyperOS Power Saver tweaks applied"
                } else {
                    "Selected tweaks applied (${updated.activeProfile.lowercase()} mode · HyperOS RAM flushed)"
                }
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
            "controlCenterBlur" -> if (enabled) "Remove notification blur enabled" else "Notification blur restored"
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
                    systemBatterySaverWithoutDarkEnabled = enabled
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
                    systemBatterySaverEnabled = enabled
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

            // Turning OFF HyperOS Power Saver switches to standard Powersave -> flush HyperOS RAM tweaks
            if (!enabled) {
                flushHyperOsRamTweaksForProfileSwitch("switched to standard powersave")
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
            _hasUnappliedChanges.value = true
        }
    }

    fun onUpdateBackgroundAllowedApps(apps: Set<String>) {
        viewModelScope.launch {
            val current = dao.getConfigDirect() ?: configState.value
            val pkgString = apps.joinToString(",")
            val userEnabledPkgs = _installedApps.value
                .filter { it.isUserEnabledApp }
                .map { it.packageName }
                .toSet()
            // Sync HyperOS App Freezer checked apps with HyperOS Battery Saver exceptions list:
            // User-enabled apps NOT in the exceptions list remain checked to freeze
            val syncedFreezeChecked = if (userEnabledPkgs.isEmpty()) {
                current.hyperOsFreezeCheckedApps
            } else {
                val nonExemptUserApps = userEnabledPkgs - apps
                when {
                    nonExemptUserApps.isEmpty() -> "__NONE__"
                    nonExemptUserApps.size == userEnabledPkgs.size -> "__ALL_USER_ENABLED__"
                    else -> nonExemptUserApps.joinToString(",")
                }
            }
            val updated = current.copy(
                backgroundAllowedApps = pkgString,
                hyperOsFreezeCheckedApps = syncedFreezeChecked
            )
            dao.update(updated)
            _hasUnappliedChanges.value = true
        }
    }

    fun onUpdateHyperOsFreezeCheckedApps(checkedApps: Set<String>, selectAllUserEnabled: Boolean) {
        viewModelScope.launch {
            val current = dao.getConfigDirect() ?: configState.value
            val userEnabledPkgs = _installedApps.value
                .filter { it.isUserEnabledApp }
                .map { it.packageName }
                .toSet()
            val currentExceptions = current.backgroundAllowedApps
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
            val systemExceptionsOnly = currentExceptions - userEnabledPkgs

            val (newFreezeRaw, newExceptionsSet) = when {
                selectAllUserEnabled -> {
                    // When 'Select all user enabled apps' is checked, all user-enabled apps are checked
                    "__ALL_USER_ENABLED__" to systemExceptionsOnly
                }
                checkedApps.isEmpty() -> {
                    "__NONE__" to currentExceptions
                }
                else -> {
                    // Unchecked user-enabled apps are synced into the HyperOS Battery Saver exceptions list
                    val uncheckedUserApps = userEnabledPkgs - checkedApps
                    val freezeCsv = if (checkedApps.size == userEnabledPkgs.size && userEnabledPkgs.isNotEmpty()) {
                        "__ALL_USER_ENABLED__"
                    } else {
                        checkedApps.joinToString(",")
                    }
                    freezeCsv to (systemExceptionsOnly + uncheckedUserApps)
                }
            }

            val updated = current.copy(
                hyperOsFreezeCheckedApps = newFreezeRaw,
                backgroundAllowedApps = newExceptionsSet.joinToString(",")
            )
            dao.update(updated)
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
            flushHyperOsRamTweaksForProfileSwitch("Performance ($tierLabel)")
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

    // ========================================================================
    // HYPEROS KERNEL-ONLY RAM LAB (Sections 1 to 10 from ram_tweaks_with_functions.txt)
    // ========================================================================

    fun refreshKernelSnapshot() {
        viewModelScope.launch {
            val snap = KernelRamLabRepository.readLiveSnapshot(_appliedKernelTweaks.value)
            _kernelSnapshot.value = snap

            // Auto-safety thermal guard: if max_zone >= 46°C while thermal daemons are stopped, restart them
            if (snap.maxZoneTempMilliC >= 46000) {
                val thermalStopped = snap.daemons.any { it.isThermalDaemon && !it.isRunning }
                if (thermalStopped) {
                    KernelRamLabRepository.setDaemonRunning("mi_thermald", true)
                    KernelRamLabRepository.setDaemonRunning("vendor.thermal-hal", true)
                    val warn = "Safety guard: Max zone reached ${snap.maxZoneTempC}°C — restarted thermal daemons"
                    _lastKernelActionBanner.value = warn
                    LogRepository.addLog(category = LogCategory.TWEAK, message = warn, isSuccess = true)
                }
            }
        }
    }

    fun onSelectHyperOsTemplate(template: HyperOsProfileTemplate?) {
        viewModelScope.launch {
            val currentCfg = dao.getConfigDirect() ?: configState.value
            val pocoF7Enabled = currentCfg.hyperOsPocoF7Enabled
            val next = if (_selectedHyperOsTemplate.value == template) null else template
            _selectedHyperOsTemplate.value = next
            if (next != null) {
                val presets = KernelRamLabRepository.getTemplatePresets(next, pocoF7Enabled)
                _stagedKernelTweaks.value = presets
                val pocoSuffix = if (pocoF7Enabled) " [Poco F7 · onyx · 8s Gen 4]" else ""
                val banner = when (next) {
                    HyperOsProfileTemplate.POWERSAVE ->
                        "Powersave$pocoSuffix staged (${presets.size} settings) & sorted in list"
                    HyperOsProfileTemplate.BALANCE_SCREEN_OFF ->
                        "Balance$pocoSuffix staged (${presets.size} settings) · Screen-off Battery Saver ready"
                    HyperOsProfileTemplate.PERFORMANCE ->
                        "Performance$pocoSuffix staged (${presets.size} settings) & sorted in list"
                }
                _lastKernelActionBanner.value = banner
                _statusMessage.value = banner
            } else {
                _stagedKernelTweaks.value = emptyMap()
                _isBalanceScreenOffSaverActive.value = false
                _lastKernelActionBanner.value = "Template cleared · Showing default order"
            }
            persistHyperOsKernelState()
        }
    }

    fun onToggleHyperOsPocoF7Mode(enabled: Boolean) {
        viewModelScope.launch {
            val currentCfg = dao.getConfigDirect() ?: configState.value
            val activeTemplate = _selectedHyperOsTemplate.value
                ?: if (enabled) HyperOsProfileTemplate.POWERSAVE else null
            _selectedHyperOsTemplate.value = activeTemplate

            val updatedStaged = if (activeTemplate != null) {
                KernelRamLabRepository.getTemplatePresets(activeTemplate, pocoF7Enabled = enabled)
            } else {
                val pocoOnlyIds = KernelRamLabRepository.tweakSpecs
                    .filter { it.isPocoF7Only }
                    .map { it.id }
                    .toSet()
                _stagedKernelTweaks.value.filterKeys { enabled || it !in pocoOnlyIds }
            }
            _stagedKernelTweaks.value = updatedStaged

            dao.update(
                currentCfg.copy(
                    hyperOsPocoF7Enabled = enabled,
                    hyperOsActiveTemplate = activeTemplate?.id.orEmpty(),
                    hyperOsStagedTweaks = serializeTweaksMap(updatedStaged)
                )
            )

            val profileName = when (activeTemplate) {
                HyperOsProfileTemplate.POWERSAVE -> "Powersave"
                HyperOsProfileTemplate.BALANCE_SCREEN_OFF -> "Balance"
                HyperOsProfileTemplate.PERFORMANCE -> "Performance"
                null -> "HyperOS"
            }
            val banner = if (enabled) {
                "Poco F7 (onyx, Snapdragon 8s Gen 4, HyperOS 3) enabled · Revealed & sorted in $profileName list (${updatedStaged.size} staged)"
            } else {
                "Poco F7 (onyx, Snapdragon 8s Gen 4, HyperOS 3) disabled · Showing standard $profileName list"
            }
            _lastKernelActionBanner.value = banner
            _statusMessage.value = banner
        }
    }

    fun onStageKernelTweak(id: String, value: String?) {
        viewModelScope.launch {
            val currentMap = _stagedKernelTweaks.value.toMutableMap()
            if (value == null) {
                currentMap.remove(id)
            } else {
                currentMap[id] = value
            }
            _stagedKernelTweaks.value = currentMap
            persistHyperOsKernelState()
        }
    }

    fun onRunSingleKernelLine(spec: KernelTweakSpec, value: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val currentCfg = dao.getConfigDirect() ?: configState.value
            val isNonHyperOsProfile =
                currentCfg.activeProfile != "POWERSAVE" || !currentCfg.hyperOsPowerSaverEnabled
            if (isNonHyperOsProfile && currentCfg.isMasterApplied) {
                // HyperOS tweaks cannot be combined with standard Powersave, Balance, or Performance
                val updatedCfg = currentCfg.copy(
                    activeProfile = "POWERSAVE",
                    hyperOsPowerSaverEnabled = true
                )
                dao.update(updatedCfg)
            }
            val (ok, msg) = KernelRamLabRepository.writeSingleKernelTweak(spec, value)
            if (ok) {
                val appliedMap = _appliedKernelTweaks.value.toMutableMap()
                appliedMap[spec.id] = value
                _appliedKernelTweaks.value = appliedMap
                persistHyperOsKernelState()
            }
            _lastKernelActionBanner.value = msg
            _statusMessage.value = msg
            LogRepository.addLog(
                category = LogCategory.TWEAK,
                message = msg,
                isSuccess = ok
            )
            refreshKernelSnapshot()
            _isBusy.value = false
        }
    }

    fun onUndoSingleKernelLine(spec: KernelTweakSpec) {
        viewModelScope.launch {
            _isBusy.value = true
            val (ok, msg) = KernelRamLabRepository.writeSingleKernelTweak(spec, spec.wasValue)
            val currentMap = _stagedKernelTweaks.value.toMutableMap()
            currentMap.remove(spec.id)
            _stagedKernelTweaks.value = currentMap
            val appliedMap = _appliedKernelTweaks.value.toMutableMap()
            appliedMap.remove(spec.id)
            _appliedKernelTweaks.value = appliedMap
            persistHyperOsKernelState()
            val undoMsg = "Restored default (${spec.formatValue(spec.wasValue)}): $msg"
            _lastKernelActionBanner.value = undoMsg
            _statusMessage.value = undoMsg
            LogRepository.addLog(
                category = LogCategory.TWEAK,
                message = undoMsg,
                isSuccess = ok
            )
            refreshKernelSnapshot()
            _isBusy.value = false
        }
    }

    fun onSwipeApplyStagedKernelTweaks() {
        viewModelScope.launch {
            val staged = _stagedKernelTweaks.value
            if (staged.isEmpty()) {
                _statusMessage.value = "Select a HyperOS template or tweak options above first"
                return@launch
            }
            _isBusy.value = true

            val logs = KernelRamLabRepository.applyBatchKernelTweaks(staged)
            val mergedApplied = _appliedKernelTweaks.value.toMutableMap().apply {
                putAll(staged)
            }
            _appliedKernelTweaks.value = mergedApplied
            _stagedKernelTweaks.value = emptyMap()

            val activeTemplate = _selectedHyperOsTemplate.value
            _isBalanceScreenOffSaverActive.value =
                (activeTemplate == HyperOsProfileTemplate.BALANCE_SCREEN_OFF)

            // Persist active template, applied tweaks, and HyperOS Power Saver compatibility in Room DB
            val currentCfg = dao.getConfigDirect() ?: configState.value
            dao.update(
                currentCfg.copy(
                    activeProfile = "POWERSAVE",
                    hyperOsPowerSaverEnabled = true,
                    hyperOsActiveTemplate = activeTemplate?.id.orEmpty(),
                    hyperOsAppliedTweaks = serializeTweaksMap(mergedApplied),
                    hyperOsStagedTweaks = "",
                    hyperOsBalanceScreenOffArmed = _isBalanceScreenOffSaverActive.value
                )
            )

            val summary = when (activeTemplate) {
                HyperOsProfileTemplate.BALANCE_SCREEN_OFF ->
                    "Applied ${logs.size} HyperOS tweaks in RAM · Screen-off Battery Saver armed"
                HyperOsProfileTemplate.POWERSAVE ->
                    "Applied ${logs.size} HyperOS Powersave tweaks permanently in RAM"
                HyperOsProfileTemplate.PERFORMANCE ->
                    "Applied ${logs.size} HyperOS Performance tweaks permanently in RAM"
                null ->
                    "Applied ${logs.size} HyperOS tweaks permanently in RAM"
            }
            _lastKernelActionBanner.value = summary
            _statusMessage.value = summary
            logs.forEach { line ->
                LogRepository.addLog(category = LogCategory.TWEAK, message = line, isSuccess = true)
            }
            refreshKernelSnapshot()
            refreshTelemetry()
            _isBusy.value = false
        }
    }

    fun onRestoreAllWasKernelValues() {
        viewModelScope.launch {
            _isBusy.value = true
            _stagedKernelTweaks.value = emptyMap()
            _appliedKernelTweaks.value = emptyMap()
            _selectedHyperOsTemplate.value = null
            _isBalanceScreenOffSaverActive.value = false
            persistHyperOsKernelState()
            val logs = KernelRamLabRepository.restoreAllWasValues()
            val summary = logs.firstOrNull() ?: "Restored all HyperOS settings to default values"
            _lastKernelActionBanner.value = summary
            _statusMessage.value = summary
            LogRepository.addLog(category = LogCategory.TWEAK, message = summary, isSuccess = true)
            refreshKernelSnapshot()
            refreshTelemetry()
            _isBusy.value = false
        }
    }

    fun onRunOneShotVmAction(dropCaches: Boolean) {
        viewModelScope.launch {
            _isBusy.value = true
            val msg = KernelRamLabRepository.runOneShotVmAction(dropCaches)
            _lastKernelActionBanner.value = msg
            _statusMessage.value = msg
            LogRepository.addLog(category = LogCategory.TWEAK, message = msg, isSuccess = true)
            refreshKernelSnapshot()
            refreshTelemetry()
            _isBusy.value = false
        }
    }

    fun onToggleXiaomiDaemon(name: String, start: Boolean) {
        viewModelScope.launch {
            _isBusy.value = true
            val msg = KernelRamLabRepository.setDaemonRunning(name, start)
            _lastKernelActionBanner.value = msg
            _statusMessage.value = msg
            LogRepository.addLog(category = LogCategory.TWEAK, message = msg, isSuccess = true)
            refreshKernelSnapshot()
            _isBusy.value = false
        }
    }

    fun onExecuteProcessKernelAction(packageNamesCsvOrToken: String, action: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val msg = when {
                packageNamesCsvOrToken == KernelRamLabRepository.ALL_USER_ENABLED_APPS_TOKEN -> {
                    val userEnabledPackages = _installedApps.value
                        .filter { it.isUserEnabledApp }
                        .map { it.packageName }
                    KernelRamLabRepository.executeBatchProcessKernelAction(userEnabledPackages, action)
                }
                packageNamesCsvOrToken.contains(",") -> {
                    val pkgs = packageNamesCsvOrToken
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    KernelRamLabRepository.executeBatchProcessKernelAction(pkgs, action)
                }
                packageNamesCsvOrToken.isNotBlank() -> {
                    KernelRamLabRepository.executeProcessKernelAction(packageNamesCsvOrToken.trim(), action)
                }
                else -> {
                    "No user-enabled apps checked to update"
                }
            }
            _lastKernelActionBanner.value = msg
            _statusMessage.value = msg
            LogRepository.addLog(category = LogCategory.TWEAK, message = msg, isSuccess = true)
            refreshKernelSnapshot()
            _isBusy.value = false
        }
    }
}
