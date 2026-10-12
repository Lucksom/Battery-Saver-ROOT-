package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TweakConfigEntity
import com.example.data.log.LogEntry
import com.example.data.telemetry.CpuCoreInfo
import com.example.data.telemetry.DeviceTelemetry
import com.example.ui.theme.MiuiAmber
import com.example.ui.theme.MiuiBlue
import com.example.ui.theme.MiuiCyan
import com.example.ui.theme.MiuiGreen
import com.example.ui.theme.MiuiGreenDark
import com.example.ui.theme.MiuiOrange
import com.example.ui.theme.MiuiRed
import com.example.ui.theme.isAppDark
import java.util.Locale

@Composable
fun DashboardView(
    telemetry: DeviceTelemetry,
    config: TweakConfigEntity,
    recentLogs: List<LogEntry>,
    onSelectProfile: (String) -> Unit,
    onToggleMaster: (Boolean) -> Unit,
    onRevertStock: () -> Unit,
    onSelectPerformanceSubMode: (String) -> Unit = {},
    onClearLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Stock Factory Snapshot & Safe Revert Banner (Merged from Profile tab)
        BackupStatusBanner(
            backupExists = telemetry.backupFileExists || config.backupCreated,
            backupFilePath = config.backupFilePath,
            onRevertStock = onRevertStock
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Native Real-Time Battery & Thermal Overview Card (Strictly NO Health, NO Cycles)
        LiveBatteryOverviewCard(
            telemetry = telemetry
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Merged Profile & Master Switch Engine Card (Unified Command Center)
        DashboardMasterAndProfileCard(
            activeProfile = config.activeProfile,
            isMasterApplied = config.isMasterApplied,
            performanceSubMode = config.performanceSubMode,
            onSelectProfile = onSelectProfile,
            onToggleMaster = onToggleMaster,
            onSelectPerformanceSubMode = onSelectPerformanceSubMode
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Octa-Core CPU Telemetry Card (Strictly NO Average Frequency Graph)
        DashboardCpuTelemetryCard(
            telemetry = telemetry
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Single Unified Real-Time Execution Logs Card showing applying tweaks
        DashboardRealTimeLogsCard(
            recentLogs = recentLogs,
            onClearLogs = onClearLogs
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
fun LiveBatteryOverviewCard(
    telemetry: DeviceTelemetry,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDark()
    val greenColor = if (isDark) MiuiGreen else MiuiGreenDark
    val thermalColor = if (telemetry.batteryTempC < 38f) greenColor else MiuiAmber

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else Color(0xFFD6DCE6)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Battery & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                if (telemetry.isCharging) greenColor.copy(alpha = if (isDark) 0.22f else 0.14f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.22f else 0.14f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (telemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                            contentDescription = "Battery",
                            tint = if (telemetry.isCharging) greenColor else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = "Battery Status",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (telemetry.isCharging) "Charging (${telemetry.batteryLevel}%)" else "Discharging (${telemetry.batteryLevel}%)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Battery Level Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (telemetry.isCharging) greenColor.copy(alpha = if (isDark) 0.2f else 0.14f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.16f else 0.1f)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${telemetry.batteryLevel}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (telemetry.isCharging) greenColor else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Animated Smooth Battery Progress Bar
            val animatedBatteryLevel by animateFloatAsState(
                targetValue = (telemetry.batteryLevel.coerceIn(0, 100)) / 100f,
                animationSpec = tween(600, easing = FastOutSlowInEasing),
                label = "BatteryBar"
            )

            LinearProgressIndicator(
                progress = { animatedBatteryLevel },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (telemetry.isCharging) greenColor else MaterialTheme.colorScheme.primary,
                trackColor = if (isDark) Color(0xFF222736) else Color(0xFFE5EAF2)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Vital Native Telemetry Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Voltage Tile
                BatteryMetricTile(
                    title = "Voltage",
                    value = String.format(Locale.US, "%.2f V", telemetry.batteryVoltageMv / 1000f),
                    accentColor = greenColor,
                    modifier = Modifier.weight(1f)
                )

                // Current Flow Tile
                val currentMa = telemetry.batteryCurrentMa
                val currentText = if (currentMa != 0) "${currentMa.coerceAtLeast(0)} mA" else "Dynamic"
                BatteryMetricTile(
                    title = "Current",
                    value = currentText,
                    accentColor = MiuiCyan,
                    modifier = Modifier.weight(1f)
                )

                // Thermal Temperature Tile
                BatteryMetricTile(
                    title = "Thermal",
                    value = String.format(Locale.US, "%.1f°C", telemetry.batteryTempC),
                    accentColor = thermalColor,
                    modifier = Modifier.weight(1f)
                )

                // Battery Capacity Tile
                BatteryMetricTile(
                    title = "Capacity",
                    value = if (telemetry.batteryCapacityMah > 0) "${telemetry.batteryCapacityMah} mAh" else "Standard",
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BatteryMetricTile(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDark()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDark) Color(0xFF1E2330) else Color(0xFFF2F5FA))
            .border(
                width = 1.dp,
                color = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.35f) else Color(0xFFDFE4ED),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Unified Master Switch & Power Profile Card (Merged from Profile Tab)
 */
@Composable
fun DashboardMasterAndProfileCard(
    activeProfile: String,
    isMasterApplied: Boolean,
    performanceSubMode: String,
    onSelectProfile: (String) -> Unit,
    onToggleMaster: (Boolean) -> Unit,
    onSelectPerformanceSubMode: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isAppDark()
    val greenColor = if (isDark) MiuiGreen else MiuiGreenDark

    val masterScale by animateFloatAsState(
        targetValue = if (isMasterApplied) 1.01f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "MasterScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = masterScale
                scaleY = masterScale
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isMasterApplied) (if (isDark) MiuiGreen.copy(alpha = 0.7f) else MiuiGreenDark)
            else if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else Color(0xFFD6DCE6)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Master Switch Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                if (isMasterApplied) MiuiGreen.copy(alpha = if (isDark) 0.22f else 0.15f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.16f else 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Master Switch",
                            tint = if (isMasterApplied) greenColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "RAM Tweak Engine",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isMasterApplied) greenColor.copy(alpha = 0.18f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isMasterApplied) "ARMED" else "STANDBY",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMasterApplied) greenColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = if (isMasterApplied) "Safe volatile tweaks applied to RAM" else "Stock frequencies • All tweaks bypassed",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isMasterApplied,
                    onCheckedChange = onToggleMaster,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = greenColor,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 0.8.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Profile Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE POWER PROFILE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = activeProfile.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (activeProfile.uppercase()) {
                        "POWERSAVE" -> greenColor
                        "PERFORMANCE" -> MiuiRed
                        else -> MiuiBlue
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3 Profile Buttons Segmented Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Powersave Profile Chip
                DashboardProfileChip(
                    title = "Powersave",
                    subtitle = "Cool & Long",
                    isSelected = activeProfile.equals("POWERSAVE", ignoreCase = true),
                    activeColor = greenColor,
                    onClick = { onSelectProfile("POWERSAVE") },
                    modifier = Modifier.weight(1f)
                )

                // Balanced Profile Chip
                DashboardProfileChip(
                    title = "Balance",
                    subtitle = "Adaptive Daily",
                    isSelected = activeProfile.equals("BALANCE", ignoreCase = true),
                    activeColor = MiuiBlue,
                    onClick = { onSelectProfile("BALANCE") },
                    modifier = Modifier.weight(1f)
                )

                // Performance Profile Chip
                DashboardProfileChip(
                    title = "Performance",
                    subtitle = "High Clocks",
                    isSelected = activeProfile.equals("PERFORMANCE", ignoreCase = true),
                    activeColor = MiuiRed,
                    onClick = { onSelectProfile("PERFORMANCE") },
                    modifier = Modifier.weight(1f)
                )
            }

            // If Performance profile is selected, show Sub-Mode Selector inline
            AnimatedVisibility(visible = activeProfile.equals("PERFORMANCE", ignoreCase = true)) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Text(
                        text = "PERFORMANCE SUB-MODE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("LITE" to "Fluid 90Hz", "HEAVY" to "60/90 FPS", "ULTRA" to "Max Clocks").forEach { (mode, desc) ->
                            val isSubSelected = performanceSubMode.equals(mode, ignoreCase = true)
                            val subColor = if (mode == "ULTRA") MiuiRed else if (mode == "HEAVY") MiuiOrange else greenColor

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSubSelected) subColor.copy(alpha = if (isDark) 0.22f else 0.14f)
                                        else if (isDark) Color(0xFF1E2330) else Color(0xFFF2F5FA)
                                    )
                                    .border(
                                        width = if (isSubSelected) 1.5.dp else 1.dp,
                                        color = if (isSubSelected) subColor else if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.35f) else Color(0xFFDFE4ED),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSelectPerformanceSubMode(mode) }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = mode,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSubSelected) subColor else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 8.sp,
                                        color = if (isSubSelected) subColor.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardProfileChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDark()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) activeColor.copy(alpha = if (isDark) 0.22f else 0.14f)
                else if (isDark) Color(0xFF1E2330)
                else Color(0xFFF2F5FA)
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) activeColor else if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.35f) else Color(0xFFDFE4ED),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = if (isSelected) activeColor.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Octa-Core CPU Telemetry Card (Cores 0-7 frequencies and states)
 * Strictly NO average frequency graph
 */
@Composable
fun DashboardCpuTelemetryCard(
    telemetry: DeviceTelemetry,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDark()
    val cores = telemetry.cpuCores.ifEmpty {
        List(8) { idx ->
            CpuCoreInfo(coreIndex = idx, isOnline = true, currentFreqMhz = 1400, maxFreqMhz = 2400)
        }
    }
    val onlineCount = cores.count { it.isOnline }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else Color(0xFFD6DCE6)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: CPU Load & Governor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MiuiBlue.copy(alpha = if (isDark) 0.22f else 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "CPU",
                            tint = MiuiBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = "Octa-Core CPU",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Governor: ${telemetry.activeGovernor.ifBlank { "schedutil" }}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Overall CPU Active Cores Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (onlineCount < 6) MiuiGreen.copy(alpha = 0.16f)
                            else MiuiBlue.copy(alpha = 0.14f)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "$onlineCount/8 Cores",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (onlineCount < 6) (if (isDark) MiuiGreen else MiuiGreenDark)
                        else MiuiBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cores 0-7 Grid (Top row 0-3, bottom row 4-7)
            val row1 = cores.take(4)
            val row2 = cores.drop(4).take(4)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (core in row1) {
                    CpuCoreTile(core = core, modifier = Modifier.weight(1f))
                }
            }

            if (row2.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (core in row2) {
                        CpuCoreTile(core = core, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CpuCoreTile(
    core: CpuCoreInfo,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDark()
    val isOnline = core.isOnline

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (!isOnline) {
                    if (isDark) Color(0xFF261818) else Color(0xFFFDE8E8)
                } else {
                    if (isDark) Color(0xFF1E2330) else Color(0xFFF2F5FA)
                }
            )
            .border(
                width = 1.dp,
                color = if (!isOnline) MiuiRed.copy(alpha = 0.5f) else (if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.35f) else Color(0xFFDFE4ED)),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Core ${core.coreIndex}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isOnline) "${core.currentFreqMhz}" else "OFF",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOnline) MaterialTheme.colorScheme.onSurface else MiuiRed
            )
            Text(
                text = if (isOnline) "MHz" else "Sleep",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Single Unified Real-Time Execution Logs Card showing applying tweaks
 */
@Composable
fun DashboardRealTimeLogsCard(
    recentLogs: List<LogEntry>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDark()
    val greenColor = if (isDark) MiuiGreen else MiuiGreenDark

    // Live pulsing dot transition
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else Color(0xFFD6DCE6)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Title with Pulsing Live Dot + Clear Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pulsing Green Live Dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(greenColor.copy(alpha = pulseAlpha))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE EXECUTION LOGS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.8.sp
                    )
                }

                if (recentLogs.isNotEmpty()) {
                    IconButton(
                        onClick = onClearLogs,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Logs",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = "Volatile parameter commands logged dynamically",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Table Column Headers: TIME | PARAMETER / TWEAK | VALUE | STATUS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) Color(0xFF1E2330) else Color(0xFFF2F5FA))
                    .border(
                        1.dp,
                        if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.35f) else Color(0xFFDFE4ED),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TIME",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(55.dp)
                )
                Text(
                    text = "TWEAK / SETTING",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "APPLIED VALUE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.9f)
                )
                Text(
                    text = "STATUS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (recentLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Engine Idle • Ready for volatile RAM tweak execution",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val displayLogs = recentLogs.take(8)
                displayLogs.forEachIndexed { index, entry ->
                    DashboardLogRow(
                        entry = entry,
                        isLast = (index == displayLogs.size - 1)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardLogRow(
    entry: LogEntry,
    isLast: Boolean
) {
    val isDark = isAppDark()
    val paramNameDisplay = entry.paramName?.takeIf { it.isNotBlank() } ?: when {
        entry.message.contains("disable_window_blurs") -> "disable_window_blurs"
        entry.message.contains("refresh rate", ignoreCase = true) -> "Display Refresh Rate"
        entry.message.contains("Touch Boost", ignoreCase = true) -> "Touch Boost"
        entry.message.contains("AOD", ignoreCase = true) -> "Always-On Display"
        entry.message.contains("PowerSaver", ignoreCase = true) || entry.message.contains("Battery Saver", ignoreCase = true) -> "System PowerMode"
        entry.message.contains("CPU", ignoreCase = true) -> "CPU Frequency"
        entry.message.contains("Governor", ignoreCase = true) -> "CPU Governor"
        else -> entry.category.name
    }

    val paramValueDisplay = entry.paramValue?.takeIf { it.isNotBlank() } ?: when {
        entry.message.contains("disable_window_blurs 1") -> "1 (Blur Removed)"
        entry.message.contains("disable_window_blurs 0") -> "0 (Stock Blur)"
        entry.message.contains("60Hz") -> "60 Hz"
        entry.message.contains("90Hz") -> "90 Hz"
        entry.message.contains("120Hz") -> "120 Hz"
        entry.message.contains("true") -> "true"
        entry.message.contains("false") -> "false"
        else -> "Applied"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time
            Text(
                text = entry.timestamp,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(55.dp)
            )

            // Tweak / Parameter Name
            Text(
                text = paramNameDisplay,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // Applied Value
            Text(
                text = paramValueDisplay,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = if (entry.isSuccess) (if (isDark) MiuiCyan else Color(0xFF00838F)) else MiuiRed,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.9f)
            )

            // Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (entry.isSuccess) (if (isDark) MiuiGreen.copy(alpha = 0.22f) else Color(0xFFDFF8E6))
                        else (if (isDark) MiuiRed.copy(alpha = 0.22f) else Color(0xFFFDE8E8))
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (entry.isSuccess) "OK" else "ERR",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.isSuccess) (if (isDark) MiuiGreen else MiuiGreenDark) else MiuiRed
                )
            }
        }

        if (!isLast) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                thickness = 0.6.dp
            )
        }
    }
}
