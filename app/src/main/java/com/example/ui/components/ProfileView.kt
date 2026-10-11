package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.db.TweakConfigEntity
import com.example.data.telemetry.DeviceTelemetry

@Composable
fun ProfileView(
    config: TweakConfigEntity,
    telemetry: DeviceTelemetry,
    cpuFreqHistory: List<Int>,
    onSelectProfile: (String) -> Unit,
    onToggleMaster: (Boolean) -> Unit,
    onSelectPerformanceSubMode: (String) -> Unit,
    onRevertStock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Backup Status Banner
        BackupStatusBanner(
            backupExists = telemetry.backupFileExists || config.backupCreated,
            backupFilePath = config.backupFilePath,
            onRevertStock = onRevertStock
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Master Switch & Profile Selector Cards
        ProfileSelector(
            activeProfile = config.activeProfile,
            isMasterApplied = config.isMasterApplied,
            onSelectProfile = onSelectProfile,
            onToggleMaster = onToggleMaster
        )

        Spacer(modifier = Modifier.height(16.dp))

        // CPU Frequency Graph Card
        CpuFrequencyGraphCard(
            history = cpuFreqHistory,
            activeProfile = config.activeProfile,
            performanceSubMode = config.performanceSubMode,
            isMasterApplied = config.isMasterApplied,
            cpuFreqCapEnabled = config.cpuFreqCapEnabled,
            activeGovernor = telemetry.activeGovernor
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}
