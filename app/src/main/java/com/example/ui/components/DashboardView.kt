package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TweakConfigEntity
import com.example.data.log.LogEntry
import com.example.data.telemetry.DeviceTelemetry
import com.example.ui.theme.MiuiAmber
import com.example.ui.theme.MiuiBlue
import com.example.ui.theme.MiuiCyan
import com.example.ui.theme.MiuiGreen
import com.example.ui.theme.MiuiGreenDark
import com.example.ui.theme.MiuiOrange
import java.util.Locale

@Composable
fun DashboardView(
    telemetry: DeviceTelemetry,
    config: TweakConfigEntity,
    recentLogs: List<LogEntry>,
    onSelectProfile: (String) -> Unit,
    onNavigateToLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Circular Battery Dial (SVG Matched)
        CircularBatteryDial(
            telemetry = telemetry,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Metric Quick Cards (Thermal, Health, Cycles in a single row)
        MetricQuickCardsRow(
            telemetry = telemetry
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Active Profile Selector Card (SVG Matched)
        DashboardProfileSelectorCard(
            activeProfile = config.activeProfile,
            onSelectProfile = onSelectProfile
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Octa-Core CPU Telemetry Card (SVG Matched)
        DashboardCpuTelemetryCard(
            telemetry = telemetry
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Real-Time Logs Preview Card with Live Tail
        DashboardRealTimeLogsCard(
            recentLogs = recentLogs,
            onNavigateToLogs = onNavigateToLogs
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun CircularBatteryDial(
    telemetry: DeviceTelemetry,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val animatedProgress by animateFloatAsState(
        targetValue = (telemetry.batteryLevel.coerceIn(0, 100)) / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "BatteryProgress"
    )

    // Base track circle: In dark mode subtle dark slate, in light mode clean light slate
    val ringTrackColor = if (isDark) Color(0xFF1F2433) else Color(0xFFDCE2EC)
    val gradientColors = if (isDark) {
        listOf(MiuiGreen, MiuiCyan, MiuiBlue)
    } else {
        listOf(MiuiGreenDark, Color(0xFF0091EA), MiuiBlue)
    }

    Box(
        modifier = modifier
            .size(210.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = 13.dp.toPx()
            val diameter = size.minDimension - strokeWidthPx
            val topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)
            val arcSize = Size(diameter, diameter)

            // Base track circle
            drawArc(
                color = ringTrackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // Active progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = gradientColors,
                    center = Offset(size.width / 2f, size.height / 2f)
                ),
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        // Inside Dial Information
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${telemetry.batteryLevel}%",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Charging Speed / Discharging Current Badge
            val speedColor = if (telemetry.isCharging) {
                if (isDark) MiuiGreen else MiuiGreenDark
            } else {
                MiuiBlue
            }

            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(speedColor.copy(alpha = if (isDark) 0.16f else 0.10f))
                    .border(
                        1.dp,
                        speedColor.copy(alpha = if (isDark) 0.35f else 0.25f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 2.5.dp)
            ) {
                Text(
                    text = telemetry.chargingSpeedLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = speedColor
                )
            }

            // Voltage & Capacity
            Text(
                text = "${String.format(Locale.US, "%,d", telemetry.batteryVoltageMv)} mV · ${String.format(Locale.US, "%,d", telemetry.batteryCapacityMah)} mAh",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }
}

@Composable
fun MetricQuickCardsRow(
    telemetry: DeviceTelemetry,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val thermalColor = if (telemetry.batteryTempC < 38f) {
        if (isDark) MiuiGreen else MiuiGreenDark
    } else {
        MiuiAmber
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Card 1: THERMAL
        MetricCard(
            modifier = Modifier.weight(1f),
            label = "THERMAL",
            value = "${telemetry.batteryTempC}°C",
            valueColor = thermalColor,
            subtitle = if (telemetry.batteryTempC < 38f) "Optimal Zone" else "Warm Zone"
        )

        // Card 2: HEALTH
        MetricCard(
            modifier = Modifier.weight(1f),
            label = "HEALTH",
            value = telemetry.batteryHealth,
            valueColor = MiuiBlue,
            subtitle = "Good Condition"
        )

        // Card 3: CYCLES
        MetricCard(
            modifier = Modifier.weight(1f),
            label = "CYCLES",
            value = "${telemetry.batteryCycleCount}",
            valueColor = MiuiAmber,
            subtitle = "Count Logged"
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    valueColor: Color,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = modifier.height(78.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
fun DashboardProfileSelectorCard(
    activeProfile: String,
    onSelectProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Profile",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MiuiBlue.copy(alpha = if (isDark) 0.18f else 0.12f))
                        .border(
                            1.dp,
                            MiuiBlue.copy(alpha = if (isDark) 0.4f else 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = activeProfile.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuiBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Profile Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileChip(
                    modifier = Modifier.weight(1f),
                    title = "⚡ Powersave",
                    subtitle = "HyperOS Mode",
                    isSelected = activeProfile == "POWERSAVE",
                    activeColor = MiuiBlue,
                    onClick = { onSelectProfile("POWERSAVE") }
                )

                ProfileChip(
                    modifier = Modifier.weight(1f),
                    title = "⚖ Balanced",
                    subtitle = "Auto Governed",
                    isSelected = activeProfile == "BALANCE",
                    activeColor = if (isDark) MiuiCyan else Color(0xFF0091EA),
                    onClick = { onSelectProfile("BALANCE") }
                )

                ProfileChip(
                    modifier = Modifier.weight(1f),
                    title = "🔥 Performance",
                    subtitle = "Uncapped",
                    isSelected = activeProfile == "PERFORMANCE",
                    activeColor = MiuiOrange,
                    onClick = { onSelectProfile("PERFORMANCE") }
                )
            }
        }
    }
}

@Composable
private fun ProfileChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) activeColor.copy(alpha = if (isDark) 0.18f else 0.12f)
                else if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else Color(0xFFF3F5FA)
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) activeColor else if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else Color(0xFFD3D9E2),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun DashboardCpuTelemetryCard(
    telemetry: DeviceTelemetry,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val greenColor = if (isDark) MiuiGreen else MiuiGreenDark

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Octa-Core CPU Telemetry",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = telemetry.activeGovernor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = greenColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cores 0-3 Little
            val littleFreq = telemetry.cpuCores.filter { it.coreIndex in 0..3 && it.isOnline }
                .map { it.currentFreqMhz }
                .average().toInt().coerceAtLeast(1200)
            val littleProgress = (littleFreq / 1800f).coerceIn(0.1f, 1f)

            CpuClusterRow(
                label = "Cores 0-3 (Cortex-A510)",
                freqText = "${String.format(Locale.US, "%,d", littleFreq)} MHz",
                progress = littleProgress,
                barColor = if (isDark) MiuiCyan else Color(0xFF0097A7)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cores 4-6 Mid
            val midFreq = telemetry.cpuCores.filter { it.coreIndex in 4..6 && it.isOnline }
                .map { it.currentFreqMhz }
                .average().toInt().coerceAtLeast(1651)
            val midProgress = (midFreq / 2400f).coerceIn(0.1f, 1f)

            CpuClusterRow(
                label = "Cores 4-6 (Cortex-A715)",
                freqText = "${String.format(Locale.US, "%,d", midFreq)} MHz",
                progress = midProgress,
                barColor = MiuiBlue
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Core 7 Prime
            val primeOnline = telemetry.cpuCores.find { it.coreIndex == 7 }?.isOnline ?: true
            val primeFreq = telemetry.cpuCores.find { it.coreIndex == 7 }?.currentFreqMhz ?: 0
            val primeLabel = if (!primeOnline || primeFreq == 0) "Parked / Eco" else "${String.format(Locale.US, "%,d", primeFreq)} MHz"
            val primeProgress = if (!primeOnline || primeFreq == 0) 0.15f else (primeFreq / 2840f).coerceIn(0.1f, 1f)

            CpuClusterRow(
                label = "Core 7 (Cortex-X3 Prime)",
                freqText = primeLabel,
                progress = primeProgress,
                barColor = MiuiAmber
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Footer GPU and ZRAM
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = telemetry.gpuFreqInfo,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = telemetry.zramInfo,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = greenColor
                )
            }
        }
    }
}

@Composable
private fun CpuClusterRow(
    label: String,
    freqText: String,
    progress: Float,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = freqText,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun DashboardRealTimeLogsCard(
    recentLogs: List<LogEntry>,
    onNavigateToLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val greenColor = if (isDark) MiuiGreen else MiuiGreenDark

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(greenColor)
                    )
                    Text(
                        text = "Real-Time Execution Logs",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 7.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(greenColor.copy(alpha = if (isDark) 0.15f else 0.10f))
                        .border(
                            1.dp,
                            greenColor.copy(alpha = if (isDark) 0.35f else 0.25f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "LIVE TAIL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = greenColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Professional Monospace Terminal Box
            val terminalBg = if (isDark) Color(0xFF0F1219) else Color(0xFF191D26)
            val terminalBorder = if (isDark) Color(0xFF1F2533) else Color(0xFF2C3342)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(terminalBg)
                    .border(1.dp, terminalBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val logsToShow = recentLogs.take(4)
                    if (logsToShow.isEmpty()) {
                        Text(
                            text = "[SYSTEM] Ready. Listening for root events...",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF8C95A6)
                        )
                    } else {
                        logsToShow.forEach { entry ->
                            val prefixColor = if (entry.isSuccess) (if (isDark) MiuiGreen else Color(0xFF81C784)) else MiuiOrange
                            Text(
                                text = "[${entry.timestamp}] ${entry.displayName}: ${entry.displayValue}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = prefixColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLogs() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View All Logs",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuiBlue
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View Logs",
                    tint = MiuiBlue,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(start = 4.dp)
                )
            }
        }
    }
}
