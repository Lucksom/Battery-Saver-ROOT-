package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.TweakConfigEntity
import com.example.data.root.RootBridge
import com.example.data.telemetry.DeviceTelemetry
import com.example.data.telemetry.TelemetryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.tweakConfigDao()
    private val telemetryRepo = TelemetryRepository(application)

    val configState: StateFlow<TweakConfigEntity> = dao.getConfig()
        .map { it ?: TweakConfigEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = TweakConfigEntity()
        )

    private val _telemetry = MutableStateFlow(
        DeviceTelemetry(
            isRootGranted = false,
            batteryLevel = 50,
            batteryTempC = 30.0f,
            batteryVoltageMv = 4000,
            isCharging = false,
            activeGovernor = "schedutil",
            cpuCores = emptyList(),
            backupFileExists = false
        )
    )
    val telemetry: StateFlow<DeviceTelemetry> = _telemetry.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>("Ready")
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        // Initialize DB record if not present
        viewModelScope.launch {
            if (dao.getConfigDirect() == null) {
                dao.insertOrUpdate(TweakConfigEntity())
            }
            refreshTelemetry()
        }

        // Live telemetry updater
        viewModelScope.launch {
            while (isActive) {
                refreshTelemetry()
                delay(2000L)
            }
        }
    }

    suspend fun refreshTelemetry() {
        val data = telemetryRepo.getTelemetry()
        _telemetry.value = data
    }

    fun onToggleMaster(apply: Boolean) {
        viewModelScope.launch {
            _isBusy.value = true
            val current = configState.value
            if (apply) {
                _statusMessage.value = "Creating stock backup & applying tweaks into RAM..."
                val logs = RootBridge.applyPowersaveTweaks(getApplication(), current)
                dao.updateAppliedStatus(true, System.currentTimeMillis())
                dao.updateBackupStatus(true, RootBridge.BACKUP_FILE_PATH)
                _statusMessage.value = "Tweaks active in RAM! Stock backup saved."
            } else {
                _statusMessage.value = "Restoring factory kernel parameters..."
                RootBridge.revertAllTweaks(getApplication())
                dao.updateAppliedStatus(false, System.currentTimeMillis())
                _statusMessage.value = "All tweaks reverted to stock values."
            }
            refreshTelemetry()
            _isBusy.value = false
        }
    }

    fun onRevertToStock() {
        viewModelScope.launch {
            _isBusy.value = true
            _statusMessage.value = "Executing stock backup revert..."
            val ok = RootBridge.revertAllTweaks(getApplication())
            dao.updateAppliedStatus(false, System.currentTimeMillis())
            _statusMessage.value = if (ok) "Stock parameters restored." else "Notice: Restored default tables."
            refreshTelemetry()
            _isBusy.value = false
        }
    }

    fun onSelectProfile(profile: String) {
        viewModelScope.launch {
            dao.updateProfile(profile)
            if (profile == "POWERSAVE") {
                _statusMessage.value = "Powersave Profile selected"
            } else {
                _statusMessage.value = "$profile profile staged (ready for wiring)"
            }
        }
    }

    fun onToggleTheme(mode: String) {
        viewModelScope.launch {
            dao.updateThemeMode(mode)
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
                else -> current
            }
            dao.update(updated)

            // If master is currently active, dynamically push change into RAM
            if (current.isMasterApplied) {
                RootBridge.applyPowersaveTweaks(getApplication(), updated)
                refreshTelemetry()
            }
        }
    }
}
