package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.SecurityUpdateGood
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.apps.InstalledAppItem
import com.example.data.db.TweakConfigEntity
import com.example.ui.theme.MiuiAmber
import com.example.ui.theme.MiuiBlue
import com.example.ui.theme.MiuiCyan
import com.example.ui.theme.MiuiGreen
import com.example.ui.theme.MiuiOrange
import com.example.ui.theme.MiuiPurple
import com.example.ui.theme.MiuiRed
import kotlin.math.roundToInt

@Composable
fun TweakCategories(
    config: TweakConfigEntity,
    onUpdateTweak: (String, Boolean) -> Unit,
    onUpdateCoreTopology: (Int, Int) -> Unit = { _, _ -> },
    onUpdateDeepSleepWhitelist: (String) -> Unit = {},
    onSelectPerformanceSubMode: (String) -> Unit = {},
    onToggleHyperOs: (Boolean) -> Unit = {},
    onSaveBackgroundAllowedApps: (Set<String>) -> Unit = {},
    installedApps: List<InstalledAppItem> = emptyList(),
    modifier: Modifier = Modifier
) {
    var showWhitelistDialog by remember { mutableStateOf(false) }
    var whitelistText by remember(config.deepSleepWhitelist) { mutableStateOf(config.deepSleepWhitelist) }
    var showAppPickerDialog by remember { mutableStateOf(false) }

    if (showAppPickerDialog) {
        val selectedSet = remember(config.backgroundAllowedApps) {
            config.backgroundAllowedApps.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        }
        AppPickerDialog(
            installedApps = installedApps,
            initiallySelectedPackages = selectedSet,
            onSaveSelection = { newSelected ->
                onSaveBackgroundAllowedApps(newSelected)
            },
            onDismiss = { showAppPickerDialog = false }
        )
    }

    if (showWhitelistDialog) {
        AlertDialog(
            onDismissRequest = { showWhitelistDialog = false },
            title = {
                Text(
                    text = "Deep Sleep App Whitelist",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter package names separated by commas. These apps will NOT be restricted during 5-minute deep sleep doze (they receive notifications and calls normally):",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = whitelistText,
                        onValueChange = { whitelistText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Package Names") },
                        placeholder = { Text("com.whatsapp, org.telegram.messenger, com.spotify.music") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateDeepSleepWhitelist(whitelistText)
                        showWhitelistDialog = false
                    }
                ) {
                    Text("Save Whitelist")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWhitelistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (config.activeProfile) {
            "BALANCE" -> {
                BalanceTweakList(
                    config = config,
                    onUpdateTweak = onUpdateTweak,
                    onOpenWhitelistDialog = { showWhitelistDialog = true }
                )
            }
            "PERFORMANCE" -> {
                PerformanceTweakList(
                    config = config,
                    onUpdateTweak = onUpdateTweak,
                    onSelectSubMode = onSelectPerformanceSubMode
                )
            }
            else -> {
                // Top prominent toggle for HyperOS / MIUI Mode
                HyperOsModeBanner(
                    isEnabled = config.hyperOsPowerSaverEnabled,
                    onToggle = onToggleHyperOs
                )

                // If HyperOS toggle is enabled, replace standard powersave tweaks with the HyperOS suite
                if (config.hyperOsPowerSaverEnabled) {
                    HyperOsPowersaveTweakList(
                        config = config,
                        onUpdateTweak = onUpdateTweak,
                        onUpdateCoreTopology = onUpdateCoreTopology,
                        onOpenAppPicker = { showAppPickerDialog = true }
                    )
                } else {
                    PowersaveTweakList(
                        config = config,
                        onUpdateTweak = onUpdateTweak,
                        onUpdateCoreTopology = onUpdateCoreTopology
                    )
                }
            }
        }
    }
}

/**
 * Top HyperOS / MIUI Mode Switcher Card
 */
@Composable
fun HyperOsModeBanner(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) MiuiOrange.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isEnabled) MiuiOrange.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isEnabled) MiuiOrange.copy(alpha = 0.2f) else MiuiGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEnabled) Icons.Default.ElectricBolt else Icons.Default.EnergySavingsLeaf,
                        contentDescription = null,
                        tint = if (isEnabled) MiuiOrange else MiuiGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MIUI / HyperOS Super Power Mode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEnabled) "ACTIVE" else "OFF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEnabled) MiuiOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isEnabled) MiuiOrange.copy(alpha = 0.18f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = if (isEnabled)
                            "Replaces standard powersave with Xiaomi HyperOS system & kernel suite (AOD disabled, Aurogon Freezer, SuperPowerClean, Touch Boost Throttle)"
                        else
                            "Enable to replace standard powersave with Xiaomi HyperOS kernel & system power-saving engine",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MiuiOrange
                )
            )
        }
    }
}

/**
 * Dedicated Balance Mode Tweaks
 */
@Composable
private fun BalanceTweakList(
    config: TweakConfigEntity,
    onUpdateTweak: (String, Boolean) -> Unit,
    onOpenWhitelistDialog: () -> Unit
) {
    Text(
        text = "BALANCE KERNEL OPTIMIZATIONS",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MiuiCyan,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp, start = 4.dp)
    )

    // 1. CPU & Energy Scaling
    TweakCategoryGroup(
        title = "CPU Energy Scaling & Clocks",
        badgeText = "4 Tweaks",
        icon = Icons.Default.Speed,
        iconColor = MiuiCyan,
        defaultExpanded = true
    ) {
        TweakItem(
            title = "70% CPU Max Frequency Cap",
            description = "Caps all CPU clusters at 70% clock in RAM for optimal balance between fluid 120Hz/90Hz UI and battery longevity",
            icon = Icons.Default.Speed,
            iconColor = MiuiCyan,
            isChecked = config.cpuFreqCapEnabled,
            onCheckedChange = { onUpdateTweak("cpuFreqCap", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Schedutil Energy-Aware Governor",
            description = "Sets 'schedutil' governor with priority fallback for dynamic energy-aware scaling with instant frame bursts",
            icon = Icons.Default.Speed,
            iconColor = MiuiGreen,
            isChecked = config.schedutilGovernorEnabled,
            onCheckedChange = { onUpdateTweak("schedutilGov", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Asymmetric Cluster Rate Limits",
            description = "Little cores: 500us/20ms; Big cores: 500us/10ms for instant frame response without battery spikes",
            icon = Icons.Default.FastForward,
            iconColor = MiuiAmber,
            isChecked = config.schedutilRateLimitsEnabled,
            onCheckedChange = { onUpdateTweak("schedutilRate", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Foreground Active App Priority",
            description = "Throttles background apps to efficiency cores (0-3) while prioritizing the active foreground app",
            icon = Icons.Default.DashboardCustomize,
            iconColor = MiuiPurple,
            isChecked = config.schedtuneTopAppEnabled,
            onCheckedChange = { onUpdateTweak("schedtuneTopApp", it) }
        )
    }

    // 2. RAM-Adaptive Virtual Memory (3-Tier Engine)
    TweakCategoryGroup(
        title = "RAM-Adaptive Virtual Memory",
        badgeText = "4 Tweaks",
        icon = Icons.Default.Memory,
        iconColor = MiuiBlue,
        defaultExpanded = true
    ) {
        TweakItem(
            title = "3-Tier RAM & Swappiness Scaling",
            description = "Auto-detects RAM: ≥8GB (swappiness 60, dirty 20/10, vfs 60), 4–8GB (80, 15/8, vfs 80), <4GB (100, 10/5, vfs 100)",
            icon = Icons.Default.Memory,
            iconColor = MiuiBlue,
            isChecked = config.balanceRamScalingEnabled,
            onCheckedChange = { onUpdateTweak("balanceRamScaling", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "30-Second Dirty Writeback Delay",
            description = "Extends flash storage dirty writeback delay to 3000 centisecs (30s) so storage controllers sleep longer",
            icon = Icons.Default.Storage,
            iconColor = MiuiPurple,
            isChecked = config.vmDirtyWritebackEnabled,
            onCheckedChange = { onUpdateTweak("vmDirtyWriteback", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "ZRAM Zero Latency (page-cluster 0)",
            description = "Reads and writes 1 single page (4KB) at a time to ZRAM, eliminating swap latency spikes",
            icon = Icons.Default.SdCard,
            iconColor = MiuiGreen,
            isChecked = config.balanceRamScalingEnabled,
            onCheckedChange = { onUpdateTweak("balanceRamScaling", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "VM Stat Interval (10s) & Panic Protection",
            description = "Reduces timer wakeups for VM statistics from 1s to 10s and disables panic on OOM",
            icon = Icons.Default.SecurityUpdateGood,
            iconColor = MiuiAmber,
            isChecked = config.balanceRamScalingEnabled,
            onCheckedChange = { onUpdateTweak("balanceRamScaling", it) }
        )
    }

    // 3. CFS Scheduler & Preemption
    TweakCategoryGroup(
        title = "CFS Scheduler & Task Placement",
        badgeText = "4 Tweaks",
        icon = Icons.Default.DashboardCustomize,
        iconColor = MiuiPurple,
        defaultExpanded = false
    ) {
        TweakItem(
            title = "CFS Autogroup & Child Runs First",
            description = "Isolates background tasks from foreground apps and executes newly created processes immediately",
            icon = Icons.Default.DashboardCustomize,
            iconColor = MiuiPurple,
            isChecked = config.balanceCfsSchedulerEnabled,
            onCheckedChange = { onUpdateTweak("balanceCfsScheduler", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Task Migration Cost (5ms / 5,000,000ns)",
            description = "Preserves L1/L2 CPU cache locality by preventing threads from bouncing unnecessarily across cores",
            icon = Icons.Default.Speed,
            iconColor = MiuiCyan,
            isChecked = config.balanceCfsSchedulerEnabled,
            onCheckedChange = { onUpdateTweak("balanceCfsScheduler", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Memory-Scaled CFS Latencies",
            description = "Devices with ≥6GB RAM use 4ms latency / 0.4ms min-granularity for smooth 120Hz/90Hz navigation",
            icon = Icons.Default.Memory,
            iconColor = MiuiGreen,
            isChecked = config.balanceCfsSchedulerEnabled,
            onCheckedChange = { onUpdateTweak("balanceCfsScheduler", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Linux Kernel Sched Features",
            description = "Enables NEXT_BUDDY, TTWU_QUEUE, NO_HRTICK, and WAKEUP_PREEMPTION for low-latency task wakeups",
            icon = Icons.Default.FastForward,
            iconColor = MiuiAmber,
            isChecked = config.balanceCfsSchedulerEnabled,
            onCheckedChange = { onUpdateTweak("balanceCfsScheduler", it) }
        )
    }

    // 4. GPU Optimization Engine (Balance)
    TweakCategoryGroup(
        title = "GPU Optimization Engine",
        badgeText = "4 Tweaks",
        icon = Icons.Default.VideogameAsset,
        iconColor = MiuiAmber,
        defaultExpanded = false
    ) {
        TweakItem(
            title = "Adreno Clock & Rail Stabilization",
            description = "Stabilizes Adreno kgsl power rails and clock gates without disabling thermal safeguards",
            icon = Icons.Default.VideogameAsset,
            iconColor = MiuiAmber,
            isChecked = config.balanceGpuOptimizationEnabled,
            onCheckedChange = { onUpdateTweak("balanceGpuOptimization", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "GPU Micro-Sleep Nap (force_no_nap 0)",
            description = "Allows GPU to enter low-power micro-sleep states between frame rendering to conserve battery",
            icon = Icons.Default.BatteryChargingFull,
            iconColor = MiuiGreen,
            isChecked = config.balanceGpuOptimizationEnabled,
            onCheckedChange = { onUpdateTweak("balanceGpuOptimization", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "80ms GPU Idle Sleep Timer",
            description = "Waits 80ms before entering sleep to balance responsiveness with power conservation",
            icon = Icons.Default.Speed,
            iconColor = MiuiCyan,
            isChecked = config.balanceGpuOptimizationEnabled,
            onCheckedChange = { onUpdateTweak("balanceGpuOptimization", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Mali & MediaTek Dynamic Scaling",
            description = "Mali DVFS coarse demand, MediaTek gpufreq aging_mode disable, and GED display boosts",
            icon = Icons.Default.DisplaySettings,
            iconColor = MiuiPurple,
            isChecked = config.balanceGpuOptimizationEnabled,
            onCheckedChange = { onUpdateTweak("balanceGpuOptimization", it) }
        )
    }

    // 5. Storage I/O Queue Engine (Balance)
    TweakCategoryGroup(
        title = "Storage I/O Multi-Queue Engine",
        badgeText = "4 Tweaks",
        icon = Icons.Default.Storage,
        iconColor = MiuiGreen,
        defaultExpanded = false
    ) {
        TweakItem(
            title = "Adaptive Multi-Queue Scheduler",
            description = "Selects modern low-latency schedulers: Kyber → BFQ → MQ-Deadline across all flash storage nodes",
            icon = Icons.Default.Storage,
            iconColor = MiuiGreen,
            isChecked = config.balanceStorageIoEnabled,
            onCheckedChange = { onUpdateTweak("balanceStorageIo", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "RAM-Scaled Read-Ahead & nr_requests",
            description = "Sets 256KB/512 requests (>6GB RAM), 128KB/256 (3–6GB), or 64KB/128 (≤3GB) for optimized I/O throughput",
            icon = Icons.Default.SdCard,
            iconColor = MiuiBlue,
            isChecked = config.balanceStorageIoEnabled,
            onCheckedChange = { onUpdateTweak("balanceStorageIo", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "CPU Request Affinity (rq_affinity 2)",
            description = "Completes storage I/O on the exact CPU core that initiated the request to avoid cache invalidation",
            icon = Icons.Default.Memory,
            iconColor = MiuiPurple,
            isChecked = config.balanceStorageIoEnabled,
            onCheckedChange = { onUpdateTweak("balanceStorageIo", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Disable I/O Statistics Overhead (iostats 0)",
            description = "Disables kernel disk metric accounting overhead to conserve CPU cycles",
            icon = Icons.Default.Speed,
            iconColor = MiuiAmber,
            isChecked = config.balanceStorageIoEnabled,
            onCheckedChange = { onUpdateTweak("balanceStorageIo", it) }
        )
    }

    // 6. Network & TCP Acceleration
    TweakCategoryGroup(
        title = "Network & TCP Acceleration",
        badgeText = "3 Tweaks",
        icon = Icons.Default.NetworkWifi,
        iconColor = MiuiGreen,
        defaultExpanded = false
    ) {
        TweakItem(
            title = "Google BBR Congestion Control & FQ",
            description = "BBR algorithm maximizes throughput and minimizes packet buffering on Wi-Fi and 4G/5G",
            icon = Icons.Default.NetworkWifi,
            iconColor = MiuiGreen,
            isChecked = config.balanceNetworkBbrEnabled,
            onCheckedChange = { onUpdateTweak("balanceNetworkBbr", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "TCP Fast Open (Level 3 Client + Server)",
            description = "Skips TCP handshake round-trips for repeat connections to accelerate web and app loads",
            icon = Icons.Default.FastForward,
            iconColor = MiuiCyan,
            isChecked = config.balanceNetworkBbrEnabled,
            onCheckedChange = { onUpdateTweak("balanceNetworkBbr", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "SYN Flood Protection & TIME_WAIT Reuse",
            description = "Enables syncookies and tw_reuse for fast, secure network socket recycling",
            icon = Icons.Default.SecurityUpdateGood,
            iconColor = MiuiBlue,
            isChecked = config.balanceNetworkBbrEnabled,
            onCheckedChange = { onUpdateTweak("balanceNetworkBbr", it) }
        )
    }

    // 7. Deep Sleep & Process Optimization
    TweakCategoryGroup(
        title = "Deep Sleep & Process Optimization",
        badgeText = "3 Tweaks",
        icon = Icons.Default.SecurityUpdateGood,
        iconColor = MiuiPurple,
        defaultExpanded = true
    ) {
        TweakItem(
            title = "5-Minute Screen-Off Deep Sleep Doze",
            description = "Monitors display state in volatile memory and forces deep sleep doze after 5 minutes screen off. Wakes instantly on display on.",
            icon = Icons.Default.SecurityUpdateGood,
            iconColor = MiuiPurple,
            isChecked = config.deepSleepScreenOffEnabled,
            onCheckedChange = { onUpdateTweak("deepSleepScreenOff", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Deep Sleep App Exception Whitelist",
            description = "Exempted: ${config.deepSleepWhitelist.split(",").filter { it.isNotBlank() }.size} apps (Tap to edit exempt package names)",
            icon = Icons.Default.DashboardCustomize,
            iconColor = MiuiBlue,
            isChecked = config.deepSleepWhitelist.isNotBlank(),
            onCheckedChange = { onOpenWhitelistDialog() }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Developer Background Process Limit (${config.developerProcessLimit})",
            description = "Sets system ActivityManager process limit to ${config.developerProcessLimit} (matches Developer Options). Prevents RAM bloat without freezing UI or crashing services.",
            icon = Icons.Default.Memory,
            iconColor = MiuiAmber,
            isChecked = config.developerProcessLimitEnabled,
            onCheckedChange = { onUpdateTweak("developerProcessLimit", it) }
        )
    }
}

/**
 * Dedicated Powersave Mode Tweaks
 */
@Composable
private fun PowersaveTweakList(
    config: TweakConfigEntity,
    onUpdateTweak: (String, Boolean) -> Unit,
    onUpdateCoreTopology: (Int, Int) -> Unit
) {
    Text(
        text = "POWERSAVE KERNEL CONTROLS",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MiuiGreen,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp, start = 4.dp)
    )

    // 1. CPU Frequency & Clocks
    TweakCategoryGroup(
        title = "CPU Clocks & Governors",
        badgeText = "4 Tweaks",
        icon = Icons.Default.Speed,
        iconColor = MiuiGreen,
        defaultExpanded = true
    ) {
        TweakItem(
            title = "Powersave CPU Governor",
            description = "Switches CPU scaling governor to 'powersave' (minimum frequency lock) for maximum battery life",
            icon = Icons.Default.Speed,
            iconColor = MiuiGreen,
            isChecked = config.schedutilGovernorEnabled,
            onCheckedChange = { onUpdateTweak("schedutilGov", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "50% CPU Max Frequency Cap",
            description = "Caps all CPU clusters at 50% max clock in RAM to save up to 40% battery",
            icon = Icons.Default.Speed,
            iconColor = MiuiCyan,
            isChecked = config.cpuFreqCapEnabled,
            onCheckedChange = { onUpdateTweak("cpuFreqCap", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Schedutil Slow Ramp-Up & 99% Threshold",
            description = "Delays frequency ramp-up and sets hispeed load to 99% to prevent micro-spikes",
            icon = Icons.Default.FastForward,
            iconColor = MiuiAmber,
            isChecked = config.schedutilRateLimitsEnabled,
            onCheckedChange = { onUpdateTweak("schedutilRate", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Background Power Throttle & Active Priority",
            description = "Throttles background apps to efficiency cores (0-3) while prioritizing the active app. Switching between apps stays instantaneous.",
            icon = Icons.Default.DashboardCustomize,
            iconColor = MiuiPurple,
            isChecked = config.schedtuneTopAppEnabled,
            onCheckedChange = { onUpdateTweak("schedtuneTopApp", it) }
        )
    }

    // 2. Dynamic Core Topology
    TweakCategoryGroup(
        title = "Dynamic Core Topology",
        badgeText = "${config.offlineCoreCount} ${if (config.offlineCoreCount == 1) "Core" else "Cores"} • ${if (config.offlineBatteryThreshold >= 100) "Always" else "≤${config.offlineBatteryThreshold}%"}",
        icon = Icons.Default.Memory,
        iconColor = MiuiPurple,
        defaultExpanded = true
    ) {
        TweakItem(
            title = "Dynamic Core Parking",
            description = "Parks heavy CPU cores to extend battery runtime. Customise core count and battery threshold below.",
            icon = Icons.Default.Memory,
            iconColor = MiuiPurple,
            isChecked = config.twoCoresOfflineBelow20Enabled,
            onCheckedChange = { onUpdateTweak("twoCoresOffline", it) }
        )

        AnimatedVisibility(visible = config.twoCoresOfflineBelow20Enabled) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Number of Cores to Offline:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1 to "1 Core (Core 7)", 2 to "2 Cores (6 & 7)").forEach { (count, label) ->
                        val selected = config.offlineCoreCount == count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) MiuiPurple.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) MiuiPurple else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onUpdateCoreTopology(count, config.offlineBatteryThreshold) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MiuiPurple else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Activation Battery Threshold:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (config.offlineBatteryThreshold >= 100) "Always Active" else "≤ ${config.offlineBatteryThreshold}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuiPurple
                    )
                }

                var sliderVal by remember(config.offlineBatteryThreshold) {
                    mutableFloatStateOf(config.offlineBatteryThreshold.toFloat())
                }

                Slider(
                    value = sliderVal,
                    onValueChange = { sliderVal = it },
                    onValueChangeFinished = {
                        val rounded = (sliderVal / 5f).roundToInt() * 5
                        onUpdateCoreTopology(config.offlineCoreCount, rounded.coerceIn(5, 100))
                    },
                    valueRange = 5f..100f,
                    steps = 18,
                    colors = SliderDefaults.colors(
                        thumbColor = MiuiPurple,
                        activeTrackColor = MiuiPurple
                    )
                )
            }
        }
    }

    // 3. Touch & Input Boost Throttle
    TweakCategoryGroup(
        title = "Touch & Input Boost Throttle",
        badgeText = "1 Tweak",
        icon = Icons.Default.TouchApp,
        iconColor = MiuiCyan
    ) {
        TweakItem(
            title = "Disable Touch & App-Launch Boost",
            description = "Prevents CPU frequency spikes when scrolling or opening apps. Saves 8-15% battery during active screen time.",
            icon = Icons.Default.TouchApp,
            iconColor = MiuiCyan,
            isChecked = config.inputTouchBoostDisabled,
            onCheckedChange = { onUpdateTweak("inputTouchBoost", it) }
        )
    }

    // 4. GPU Power Throttling & Idler
    TweakCategoryGroup(
        title = "GPU Power Throttling & Idler",
        badgeText = "2 Tweaks",
        icon = Icons.Default.VideogameAsset,
        iconColor = MiuiAmber
    ) {
        TweakItem(
            title = "GPU Power Throttling",
            description = "Enables kgsl power throttling & caps GPU max clock to 50% in RAM",
            icon = Icons.Default.VideogameAsset,
            iconColor = MiuiAmber,
            isChecked = config.gpuPowerLimitEnabled,
            onCheckedChange = { onUpdateTweak("gpuPowerLimit", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Adreno Idler (Snapdragon)",
            description = "Aggressively drops Adreno GPU clock when UI rendering idle workload is detected",
            icon = Icons.Default.Speed,
            iconColor = MiuiOrange,
            isChecked = config.adrenoIdlerEnabled,
            onCheckedChange = { onUpdateTweak("adrenoIdler", it) }
        )
    }

    // 5. Storage & I/O
    TweakCategoryGroup(
        title = "Storage & Dynamic Fsync",
        badgeText = "2 Tweaks",
        icon = Icons.Default.SdCard,
        iconColor = MiuiBlue
    ) {
        TweakItem(
            title = "I/O Queue Depths (128KB)",
            description = "Caps block queue read-ahead to 128KB and nr_requests to 64 to save storage CPU cycles",
            icon = Icons.Default.Storage,
            iconColor = MiuiBlue,
            isChecked = config.storageIoQueueEnabled,
            onCheckedChange = { onUpdateTweak("storageIo", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Dynamic Fsync",
            description = "Suspends filesystem fsync writes when screen is ON, flushes when screen turns OFF",
            icon = Icons.Default.SdCard,
            iconColor = MiuiGreen,
            isChecked = config.dynamicFsyncEnabled,
            onCheckedChange = { onUpdateTweak("dynamicFsync", it) }
        )
    }

    // 6. Virtual Memory (VM)
    TweakCategoryGroup(
        title = "Virtual Memory & Swappiness",
        badgeText = "1 Tweak",
        icon = Icons.Default.DashboardCustomize,
        iconColor = MiuiPurple
    ) {
        TweakItem(
            title = "30-Second Dirty Writeback Delay",
            description = "Holds dirty page cache for 30s before waking flash storage controller",
            icon = Icons.Default.DashboardCustomize,
            iconColor = MiuiPurple,
            isChecked = config.vmDirtyWritebackEnabled,
            onCheckedChange = { onUpdateTweak("vmDirtyWriteback", it) }
        )
    }

    // 7. Network Optimization
    TweakCategoryGroup(
        title = "Network Optimization",
        badgeText = "1 Tweak",
        icon = Icons.Default.NetworkWifi,
        iconColor = MiuiGreen
    ) {
        TweakItem(
            title = "TCP BBR Congestion Control",
            description = "Enables Google BBR algorithm with Fast Open for efficient networking",
            icon = Icons.Default.NetworkWifi,
            iconColor = MiuiGreen,
            isChecked = config.tcpBbrCongestionEnabled,
            onCheckedChange = { onUpdateTweak("tcpBbr", it) }
        )
    }

    // 8. Safe Display & Hardware Extras
    TweakCategoryGroup(
        title = "Hardware & Display Extras",
        badgeText = "5 Tweaks",
        icon = Icons.Default.DisplaySettings,
        iconColor = MiuiBlue
    ) {
        TweakItem(
            title = "Power Efficient Workqueue",
            description = "Pins kernel workqueues to efficiency cores and enables deepest C-states",
            icon = Icons.Default.DisplaySettings,
            iconColor = MiuiBlue,
            isChecked = config.powerEfficientWorkqueueEnabled,
            onCheckedChange = { onUpdateTweak("powerEfficientWorkqueue", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Lock Refresh Rate to 60 Hz",
            description = "Forces system and SurfaceFlinger display rendering to 60 Hz. Saves 350-500 mA current during scrolling.",
            icon = Icons.Default.Speed,
            iconColor = MiuiAmber,
            isChecked = config.lock60HzRefreshRateEnabled,
            onCheckedChange = { onUpdateTweak("lock60Hz", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "System Battery Saver (Keep Light Mode)",
            description = "Enables system power saver while strictly preserving Light Mode (suppresses forced Dark Mode).",
            icon = Icons.Default.BatteryChargingFull,
            iconColor = MiuiGreen,
            isChecked = config.systemBatterySaverWithoutDarkEnabled,
            onCheckedChange = { onUpdateTweak("batterySaverNoDark", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Preserve Double-Tap to Wake",
            description = "Ensures wake gestures stay active (safely skips missing LCD nodes on AMOLED screens)",
            icon = Icons.Default.Gesture,
            iconColor = MiuiCyan,
            isChecked = config.doubleTapWakePreserved,
            onCheckedChange = { onUpdateTweak("doubleTapWake", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Qualcomm Low Power Mode (LPM)",
            description = "Enables deep suspend sleep on Qualcomm Snapdragon chipsets",
            icon = Icons.Default.FastForward,
            iconColor = MiuiGreen,
            isChecked = config.lpmSleepEnabled,
            onCheckedChange = { onUpdateTweak("lpmSleep", it) }
        )
    }
}

/**
 * Dedicated HyperOS / MIUI Super Power Saving Engine Tweaks
 * Directly implements Xiaomi's Aurogon, SuperPowerClean, setAodEnable, and TouchBoost policies
 */
@Composable
private fun HyperOsPowersaveTweakList(
    config: TweakConfigEntity,
    onUpdateTweak: (String, Boolean) -> Unit,
    onUpdateCoreTopology: (Int, Int) -> Unit,
    onOpenAppPicker: () -> Unit
) {
    Text(
        text = "HYPEROS / MIUI SUPER POWER SUITE",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MiuiOrange,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp, start = 4.dp)
    )

    val userAllowedList = remember(config.backgroundAllowedApps) {
        config.backgroundAllowedApps.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    // 1. Display & Always-On Display (AOD) Engine
    TweakCategoryGroup(
        title = "Display & Always-On Display (AOD)",
        badgeText = "3 Tweaks",
        icon = Icons.Default.DisplaySettings,
        iconColor = MiuiAmber,
        defaultExpanded = true
    ) {
        TweakItem(
            title = "Disable Always-On Display (AOD)",
            description = "Toggles 'MiuiAod.Utils setAodEnable: false' • Turns off AOD rendering and panel wakeups during battery saving to eliminate idle drain.",
            icon = Icons.Default.VisibilityOff,
            iconColor = MiuiAmber,
            isChecked = config.hyperOsAodDisabled,
            onCheckedChange = { onUpdateTweak("hyperOsAod", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Disable RefreshRateSelector Touch Boost",
            description = "Suppresses CPU/display frequency spikes triggered by touch events in HyperOS. Saves 8-15% battery during active touches.",
            icon = Icons.Default.TouchApp,
            iconColor = MiuiCyan,
            isChecked = config.hyperOsTouchBoostDisabled,
            onCheckedChange = { onUpdateTweak("hyperOsTouchBoost", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Lock Refresh Rate to 60Hz",
            description = "Sets 'miui_refresh_rate 60' and SurfaceFlinger lock. Saves 350-500mA current during screen-on use.",
            icon = Icons.Default.Speed,
            iconColor = MiuiGreen,
            isChecked = config.hyperOsLock60HzEnabled,
            onCheckedChange = { onUpdateTweak("hyperOsLock60Hz", it) }
        )
    }

    // 2. Background App Exception Manager (SuperPowerClean Whitelist)
    TweakCategoryGroup(
        title = "Background App Exceptions (SuperPowerClean)",
        badgeText = "${userAllowedList.size} Allowed",
        icon = Icons.Default.Apps,
        iconColor = MiuiBlue,
        defaultExpanded = true
    ) {
        // Permanent System Components Shield (Always in RAM, non-editable)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MiuiGreen.copy(alpha = 0.08f))
                .border(1.dp, MiuiGreen.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MiuiGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Permanent System Exception Shield",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuiGreen
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• com.mi.android.globallauncher (HyperOS Launcher)\n" +
                            "• com.miui.home (MIUI Home Launcher)\n" +
                            "• com.android.systemui (System UI Core)\n" +
                            "• miui.systemui.plugin (SystemUI Plugin)\n" +
                            "These system components are permanently protected in RAM and immune to termination or freezing.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))

        // Custom User-Selected Background Apps
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Custom Allowed Background Apps",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (userAllowedList.isEmpty()) "No user apps selected (all non-whitelisted apps will be cleaned on screen lock)"
                        else "${userAllowedList.size} apps selected: ${userAllowedList.joinToString(", ")}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onOpenAppPicker,
                    colors = ButtonDefaults.buttonColors(containerColor = MiuiBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Select Apps", fontSize = 12.sp)
                }
            }
        }
    }

    // 3. Aurogon Process Freezer & SuperPowerClean
    TweakCategoryGroup(
        title = "Aurogon Freezer & Process Clean",
        badgeText = "2 Tweaks",
        icon = Icons.Default.AcUnit,
        iconColor = MiuiPurple,
        defaultExpanded = false
    ) {
        TweakItem(
            title = "Xiaomi Aurogon Immobulus Freezer",
            description = "Enforces Aurogon cgroup freezer rules. Keeps background processes at 0% CPU without crashing apps, allowing instant resume.",
            icon = Icons.Default.AcUnit,
            iconColor = MiuiCyan,
            isChecked = config.hyperOsAurogonFreezerEnabled,
            onCheckedChange = { onUpdateTweak("hyperOsAurogon", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "SuperPowerClean Background Purge",
            description = "Terminates unwhitelisted background processes in volatile RAM on screen lock while preserving messaging apps (WhatsApp, SMS, etc.).",
            icon = Icons.Default.SecurityUpdateGood,
            iconColor = MiuiPurple,
            isChecked = config.hyperOsSuperPowerCleanEnabled,
            onCheckedChange = { onUpdateTweak("hyperOsSuperPowerClean", it) }
        )
    }

    // 4. Qualcomm 5G & Modem Power Optimization (FiveGPowerController)
    TweakCategoryGroup(
        title = "Qualcomm 5G & Modem Optimization",
        badgeText = "1 Tweak",
        icon = Icons.Default.SignalCellularAlt,
        iconColor = MiuiBlue,
        defaultExpanded = false
    ) {
        TweakItem(
            title = "FiveGPowerController Modem Optimization",
            description = "Restricts 5G carrier aggregation and lowers radio power consumption during screen-off sleep (from HyperOS logs).",
            icon = Icons.Default.SignalCellularAlt,
            iconColor = MiuiBlue,
            isChecked = config.hyperOsFiveGPowerOptEnabled,
            onCheckedChange = { onUpdateTweak("hyperOsFiveG", it) }
        )
    }

    // 5. CPU Frequency & Dynamic Core Parking
    TweakCategoryGroup(
        title = "CPU Frequency & Core Parking",
        badgeText = "${config.offlineCoreCount} Cores • 50% Cap",
        icon = Icons.Default.Speed,
        iconColor = MiuiGreen,
        defaultExpanded = true
    ) {
        TweakItem(
            title = "50% CPU Max Frequency Cap",
            description = "Caps all CPU clusters at 50% max clock in RAM to save up to 40% battery.",
            icon = Icons.Default.Speed,
            iconColor = MiuiCyan,
            isChecked = config.cpuFreqCapEnabled,
            onCheckedChange = { onUpdateTweak("cpuFreqCap", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Dynamic Core Parking",
            description = "Parks heavy CPU cores in volatile memory. Customise core count and battery threshold below.",
            icon = Icons.Default.Memory,
            iconColor = MiuiPurple,
            isChecked = config.twoCoresOfflineBelow20Enabled,
            onCheckedChange = { onUpdateTweak("twoCoresOffline", it) }
        )

        AnimatedVisibility(visible = config.twoCoresOfflineBelow20Enabled) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Number of Cores to Offline:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1 to "1 Core (Core 7)", 2 to "2 Cores (6 & 7)").forEach { (count, label) ->
                        val selected = config.offlineCoreCount == count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) MiuiPurple.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) MiuiPurple else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onUpdateCoreTopology(count, config.offlineBatteryThreshold) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MiuiPurple else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Activation Battery Threshold:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (config.offlineBatteryThreshold >= 100) "Always Active" else "≤ ${config.offlineBatteryThreshold}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuiPurple
                    )
                }

                var sliderVal by remember(config.offlineBatteryThreshold) {
                    mutableFloatStateOf(config.offlineBatteryThreshold.toFloat())
                }

                Slider(
                    value = sliderVal,
                    onValueChange = { sliderVal = it },
                    onValueChangeFinished = {
                        val rounded = (sliderVal / 5f).roundToInt() * 5
                        onUpdateCoreTopology(config.offlineCoreCount, rounded.coerceIn(5, 100))
                    },
                    valueRange = 5f..100f,
                    steps = 18,
                    colors = SliderDefaults.colors(
                        thumbColor = MiuiPurple,
                        activeTrackColor = MiuiPurple
                    )
                )
            }
        }
    }
}

/**
 * Dedicated Performance Mode Tweaks with Lite, Heavy, and Ultra Sub-Modes
 */
@Composable
private fun PerformanceTweakList(
    config: TweakConfigEntity,
    onUpdateTweak: (String, Boolean) -> Unit,
    onSelectSubMode: (String) -> Unit
) {
    val activeSubMode = config.performanceSubMode.uppercase()
    val isUltra = activeSubMode == "ULTRA"
    val isHeavy = activeSubMode == "HEAVY"
    val isLite = activeSubMode == "LITE"

    val subModeColor = when {
        isUltra -> MiuiRed
        isHeavy -> MiuiOrange
        else -> MiuiGreen
    }

    Text(
        text = "PERFORMANCE OPTIMIZATION TIER",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = subModeColor,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp, start = 4.dp)
    )

    // Sub-Mode Segmented Selector (Lite, Heavy, Ultra)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PerformanceSubModeButton(
                title = "Lite",
                subtitle = "90/120Hz Fluid",
                icon = Icons.Default.Speed,
                accentColor = MiuiGreen,
                isSelected = isLite,
                onClick = { onSelectSubMode("LITE") },
                modifier = Modifier.weight(1f)
            )

            PerformanceSubModeButton(
                title = "Heavy",
                subtitle = "60/90 FPS Gaming",
                icon = Icons.Default.ElectricBolt,
                accentColor = MiuiOrange,
                isSelected = isHeavy,
                onClick = { onSelectSubMode("HEAVY") },
                modifier = Modifier.weight(1f)
            )

            PerformanceSubModeButton(
                title = "Ultra",
                subtitle = "Esports & Clocks",
                icon = Icons.Default.FastForward,
                accentColor = MiuiRed,
                isSelected = isUltra,
                onClick = { onSelectSubMode("ULTRA") },
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Safety & Architecture Verification Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = subModeColor.copy(alpha = 0.08f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            subModeColor.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(subModeColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Safe",
                    tint = subModeColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = when {
                        isUltra -> "Ultra: Maximum Hardware Clock & Bus Lock"
                        isHeavy -> "Heavy: Elevated Frequency Floor & DDR Bus Boost"
                        else -> "Lite: Fluid Schedutil & Zero Adreno Idle Throttling"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    // 1. CPU & Scheduler Engine
    TweakCategoryGroup(
        title = "CPU & Scheduler Engine",
        badgeText = "4 Tweaks",
        icon = Icons.Default.Speed,
        iconColor = subModeColor,
        defaultExpanded = true
    ) {
        val govTitle = when {
            isUltra -> "Performance Governor (Full Clock Lock)"
            isHeavy -> "Schedutil Fast-Ramp + 45% Clock Floor"
            else -> "Schedutil Responsive (500µs Rate Limit)"
        }
        val govDesc = when {
            isUltra -> "Locks all cores at maximum hardware frequency tables in RAM for zero thermal throttling during intense gaming"
            isHeavy -> "Elevates base frequency floor to 45% of max clock with 400µs up-rate limit to eliminate 1% low frame dips"
            else -> "Dynamic energy-aware governor with 500µs ramp-up for fluid 90Hz/120Hz system navigation"
        }
        TweakItem(
            title = govTitle,
            description = govDesc,
            icon = Icons.Default.Speed,
            iconColor = subModeColor,
            isChecked = config.perfCpuGovernorLockEnabled,
            onCheckedChange = { onUpdateTweak("perfGovLock", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))

        TweakItem(
            title = if (isUltra) "All 8 Cores Online & Core_Ctl Disabled" else "All 8 CPU Cores Online 100%",
            description = if (isUltra) "Keeps Little, Mid, and Big cores active and disables core_ctl parking to eliminate scheduling wake delays" else "Forces all 8 CPU cores online in volatile memory with zero sleep parking",
            icon = Icons.Default.Memory,
            iconColor = MiuiPurple,
            isChecked = config.perfAllCoresOnlineEnabled,
            onCheckedChange = { onUpdateTweak("perfAllCores", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))

        val schedtuneBoostVal = if (isUltra) "50%" else if (isHeavy) "45%" else "25%"
        TweakItem(
            title = "SchedTune Top-App Boost ($schedtuneBoostVal)",
            description = "Boosts active foreground game thread affinity to $schedtuneBoostVal and grants top-app 1024 CPU shares for zero frame jitter",
            icon = Icons.Default.DashboardCustomize,
            iconColor = MiuiAmber,
            isChecked = config.perfSchedtuneBoostEnabled,
            onCheckedChange = { onUpdateTweak("perfSchedtune", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))

        val boostDuration = if (isUltra) "256ms" else "128ms"
        TweakItem(
            title = "Touch & Input Boost ($boostDuration)",
            description = "Enables instantaneous frequency boost upon finger touch to register fast swipe, aim, and tap gestures",
            icon = Icons.Default.TouchApp,
            iconColor = MiuiCyan,
            isChecked = config.perfTouchBoostEnabled,
            onCheckedChange = { onUpdateTweak("perfTouchBoost", it) }
        )
    }

    // 2. GPU & Graphic Acceleration
    TweakCategoryGroup(
        title = "GPU & Graphic Acceleration",
        badgeText = "2 Tweaks",
        icon = Icons.Default.VideogameAsset,
        iconColor = MiuiCyan,
        defaultExpanded = true
    ) {
        val boostLevel = if (isUltra) "Level 3 + Rail Lock" else if (isHeavy) "Level 2 (80ms timer)" else "Level 1"
        TweakItem(
            title = "AdrenoBoost & Rail Power ($boostLevel)",
            description = "Bypasses artificial GPU throttling flags, disables Adreno Idler, and elevates Adreno/Mali clock gating",
            icon = Icons.Default.VideogameAsset,
            iconColor = MiuiCyan,
            isChecked = config.perfGpuAdrenoBoostEnabled,
            onCheckedChange = { onUpdateTweak("perfGpuBoost", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))

        TweakItem(
            title = "GPU Force No-Nap",
            description = "Prevents GPU micro-sleep nap states between rendered frame cycles to maintain flat frametimes",
            icon = Icons.Default.BatteryChargingFull,
            iconColor = MiuiGreen,
            isChecked = config.perfGpuNoNapEnabled,
            onCheckedChange = { onUpdateTweak("perfGpuNoNap", it) }
        )
    }

    // 3. DDR Memory & Bus Bandwidth
    TweakCategoryGroup(
        title = "DDR Memory & Bus Acceleration",
        badgeText = "2 Tweaks",
        icon = Icons.Default.SdCard,
        iconColor = MiuiBlue,
        defaultExpanded = false
    ) {
        val busDesc = when {
            isUltra -> "Locks Qualcomm & universal devfreq memory buses (cpubw, gpubw, memlat) to maximum bandwidth"
            isHeavy -> "Accelerates CPU/GPU memory bandwidth devfreq governors for smooth 3D asset streaming"
            else -> "Dynamic memory bus scaling active to balance bandwidth and thermals"
        }
        TweakItem(
            title = "Devfreq RAM Bus Bandwidth",
            description = busDesc,
            icon = Icons.Default.SdCard,
            iconColor = MiuiBlue,
            isChecked = config.perfDdrBusBoostEnabled,
            onCheckedChange = { onUpdateTweak("perfDdrBus", it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))

        TweakItem(
            title = "LMK Gaming Free-Pool Allocation",
            description = "Reserves 21MB extra free RAM pool (swappiness 75) and expands background app limit to 34 to avoid game reload stutter",
            icon = Icons.Default.Memory,
            iconColor = MiuiPurple,
            isChecked = config.perfLmkTuningEnabled,
            onCheckedChange = { onUpdateTweak("perfLmkTuning", it) }
        )
    }

    // 4. Storage I/O Multi-Queue Engine
    TweakCategoryGroup(
        title = "Storage I/O Multi-Queue Engine",
        badgeText = "1 Tweak",
        icon = Icons.Default.Storage,
        iconColor = MiuiGreen,
        defaultExpanded = false
    ) {
        TweakItem(
            title = "High-Throughput Storage I/O (512KB)",
            description = "Expands block queue read-ahead to 512KB, nr_requests to 256, and enables rq_affinity 2 for rapid game asset loading",
            icon = Icons.Default.Storage,
            iconColor = MiuiGreen,
            isChecked = config.perfStorageQueue512Enabled,
            onCheckedChange = { onUpdateTweak("perfStorage512", it) }
        )
    }
}

@Composable
private fun PerformanceSubModeButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.16f) else Color.Transparent,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "SubModeBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "SubModeContent"
    )

    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) accentColor.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(15.dp)
            )
            .hyperBounceClick(scaleDown = 0.94f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = contentColor.copy(alpha = if (isSelected) 0.9f else 0.6f),
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun TweakCategoryGroup(
    title: String,
    badgeText: String,
    icon: ImageVector,
    iconColor: Color,
    defaultExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(defaultExpanded) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "ArrowRotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = badgeText,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(arrowRotation)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        thickness = 0.8.dp
                    )
                    content()
                }
            }
        }
    }
}
