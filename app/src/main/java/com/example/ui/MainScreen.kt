package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BackupStatusBanner
import com.example.ui.components.DashboardView
import com.example.ui.components.HeaderBar
import com.example.ui.components.TweakCategories
import com.example.ui.components.VoltPowerBottomBar
import com.example.ui.components.VoltPowerTab

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val config by viewModel.configState.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val cpuFreqHistory by viewModel.cpuFreqHistory.collectAsState()
    val isBusy by viewModel.isBusy.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val logs by viewModel.logs.collectAsState()

    var currentTab by rememberSaveable { mutableStateOf(VoltPowerTab.DASHBOARD) }

    // Predictive back / BackHandler: if not on Dashboard, return to Dashboard
    if (currentTab != VoltPowerTab.DASHBOARD) {
        BackHandler {
            currentTab = VoltPowerTab.DASHBOARD
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            if (it != "Ready") {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HeaderBar(
                isRootGranted = telemetry.isRootGranted,
                themeMode = config.themeMode,
                onCycleTheme = {
                    val next = when (config.themeMode) {
                        "SYSTEM" -> "DARK"
                        "DARK" -> "LIGHT"
                        else -> "SYSTEM"
                    }
                    viewModel.onToggleTheme(next)
                },
                onRevertStock = {
                    viewModel.onRevertToStock()
                }
            )
        },
        bottomBar = {
            VoltPowerBottomBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabContentTransition"
            ) { targetTab ->
                when (targetTab) {
                    VoltPowerTab.DASHBOARD -> {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        ) {
                            DashboardView(
                                telemetry = telemetry,
                                config = config,
                                recentLogs = logs,
                                onSelectProfile = { viewModel.onSelectProfile(it) },
                                onToggleMaster = { viewModel.onToggleMaster(it) },
                                onRevertStock = { viewModel.onRevertToStock() },
                                onSelectPerformanceSubMode = { viewModel.onSelectPerformanceSubMode(it) },
                                onClearLogs = { viewModel.onClearLogs() }
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "VoltPower • 100% Volatile RAM Execution",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    VoltPowerTab.TWEAKS -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                BackupStatusBanner(
                                    backupExists = telemetry.backupFileExists || config.backupCreated,
                                    backupFilePath = config.backupFilePath,
                                    onRevertStock = { viewModel.onRevertToStock() }
                                )
                            }

                            item {
                                TweakCategories(
                                    config = config,
                                    onUpdateTweak = { key, enabled ->
                                        viewModel.onUpdateTweak(key, enabled)
                                    },
                                    onUpdateCoreTopology = { coreCount, threshold ->
                                        viewModel.onUpdateCoreTopology(coreCount, threshold)
                                    },
                                    onUpdateDeepSleepWhitelist = { whitelist ->
                                        viewModel.onUpdateDeepSleepWhitelist(whitelist)
                                    },
                                    onSelectPerformanceSubMode = { subMode ->
                                        viewModel.onSelectPerformanceSubMode(subMode)
                                    },
                                    onToggleHyperOs = { enabled ->
                                        viewModel.onToggleHyperOsPowerSaver(enabled)
                                    },
                                    onSaveBackgroundAllowedApps = { apps ->
                                        viewModel.onUpdateBackgroundAllowedApps(apps)
                                    },
                                    onSelectRefreshRate = { hz ->
                                        viewModel.onSelectRefreshRate(hz)
                                    },
                                    installedApps = installedApps
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(28.dp))
                            }
                        }
                    }
                }
            }

            if (isBusy) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
