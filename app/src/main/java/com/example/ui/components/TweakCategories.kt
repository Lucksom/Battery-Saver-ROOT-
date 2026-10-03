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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.SecurityUpdateGood
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TweakConfigEntity
import com.example.ui.theme.MiuiAmber
import com.example.ui.theme.MiuiBlue
import com.example.ui.theme.MiuiCyan
import com.example.ui.theme.MiuiGreen
import com.example.ui.theme.MiuiOrange
import com.example.ui.theme.MiuiPurple
import kotlin.math.roundToInt

@Composable
fun TweakCategories(
    config: TweakConfigEntity,
    onUpdateTweak: (String, Boolean) -> Unit,
    onUpdateCoreTopology: (Int, Int) -> Unit = { _, _ -> },
    onUpdateDeepSleepWhitelist: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showWhitelistDialog by remember { mutableStateOf(false) }
    var whitelistText by remember(config.deepSleepWhitelist) { mutableStateOf(config.deepSleepWhitelist) }

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
                    onUpdateTweak = onUpdateTweak
                )
            }
            else -> {
                PowersaveTweakList(
                    config = config,
                    onUpdateTweak = onUpdateTweak,
                    onUpdateCoreTopology = onUpdateCoreTopology
                )
            }
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
 * Dedicated Performance Mode Tweaks
 */
@Composable
private fun PerformanceTweakList(
    config: TweakConfigEntity,
    onUpdateTweak: (String, Boolean) -> Unit
) {
    Text(
        text = "PERFORMANCE KERNEL CONTROLS",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MiuiAmber,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp, start = 4.dp)
    )

    TweakCategoryGroup(
        title = "Maximum Frequency & Clocks",
        badgeText = "3 Tweaks",
        icon = Icons.Default.Speed,
        iconColor = MiuiAmber,
        defaultExpanded = true
    ) {
        TweakItem(
            title = "Performance Governor (Full Clock Lock)",
            description = "Locks CPU cores at highest available frequency tables for zero thermal throttling and maximum FPS",
            icon = Icons.Default.Speed,
            iconColor = MiuiAmber,
            isChecked = true,
            onCheckedChange = { }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "All 8 CPU Cores Online 100%",
            description = "Forces all Big, Mid, and Prime CPU cores to stay awake with zero core parking",
            icon = Icons.Default.Memory,
            iconColor = MiuiPurple,
            isChecked = true,
            onCheckedChange = { }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "Instant 0us Schedutil Rate Limits",
            description = "Zero delay ramp-up on all cores for instant frame rendering during heavy gaming",
            icon = Icons.Default.FastForward,
            iconColor = MiuiGreen,
            isChecked = true,
            onCheckedChange = { }
        )
    }

    TweakCategoryGroup(
        title = "GPU & Storage Acceleration",
        badgeText = "2 Tweaks",
        icon = Icons.Default.VideogameAsset,
        iconColor = MiuiCyan,
        defaultExpanded = true
    ) {
        TweakItem(
            title = "GPU Throttling Bypass",
            description = "Disables artificial GPU throttling flags and unlocks maximum clock tables for rendering",
            icon = Icons.Default.VideogameAsset,
            iconColor = MiuiCyan,
            isChecked = true,
            onCheckedChange = { }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
        TweakItem(
            title = "High-Throughput Storage I/O (512KB)",
            description = "Expands block read-ahead to 512KB and queue depth to 512 for instant game asset streaming",
            icon = Icons.Default.Storage,
            iconColor = MiuiGreen,
            isChecked = true,
            onCheckedChange = { }
        )
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
