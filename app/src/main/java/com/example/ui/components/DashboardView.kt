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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TweakConfigEntity
import com.example.data.log.LogEntry
import com.example.data.telemetry.CpuCoreInfo
import com.example.data.telemetry.DeviceTelemetry
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MiuiAmber
import com.example.ui.theme.MiuiBlue
import com.example.ui.theme.MiuiCyan
import com.example.ui.theme.MiuiGreen
import com.example.ui.theme.MiuiGreenDark
import com.example.ui.theme.MiuiOrange
import com.example.ui.theme.MiuiPurple
import com.example.ui.theme.MiuiRed
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.isAppDark
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

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
        Spacer(modifier = Modifier.height(6.dp))

        // 1. Stock Factory Snapshot & Safe Revert Vault Banner
        BackupStatusBanner(
            backupExists = telemetry.backupFileExists || config.backupCreated,
            backupFilePath = config.backupFilePath,
            onRevertStock = onRevertStock
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Native Real-Time Battery & Thermal Hero Cockpit Card (Strictly NO Health, NO Cycles)
        LiveBatteryOverviewCard(
            telemetry = telemetry
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Unified RAM Tweak Engine & Power Profile Command Deck
        DashboardMasterAndProfileCard(
            activeProfile = config.activeProfile,
            isMasterApplied = config.isMasterApplied,
            performanceSubMode = config.performanceSubMode,
            onSelectProfile = onSelectProfile,
            onToggleMaster = onToggleMaster,
            onSelectPerformanceSubMode = onSelectPerformanceSubMode
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Octa-Core CPU Cluster Matrix (Strictly NO Average Frequency Graph)
        DashboardCpuTelemetryCard(
            telemetry = telemetry
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Unified Real-Time Kernel Execution Terminal
        DashboardRealTimeLogsCard(
            recentLogs = recentLogs,
            onClearLogs = onClearLogs
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun LiveBatteryOverviewCard(
    telemetry: DeviceTelemetry,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDark()
    val greenColor = if (isDark) MiuiGreen else MiuiGreenDark
    val primaryAccent = if (telemetry.isCharging) greenColor else MiuiBlue
    val thermalColor = when {
        telemetry.batteryTempC >= 41f -> MiuiRed
        telemetry.batteryTempC >= 37.5f -> MiuiAmber
        else -> greenColor
    }

    val animatedBatteryFraction by animateFloatAsState(
        targetValue = (telemetry.batteryLevel.coerceIn(0, 100)) / 100f,
        animationSpec = tween(750, easing = FastOutSlowInEasing),
        label = "BatteryArcProgress"
    )

    val voltageVolts = telemetry.batteryVoltageMv / 1000f
    val currentMaAbs = abs(telemetry.batteryCurrentMa)
    val wattage = (voltageVolts * (currentMaAbs / 1000f))

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDark) primaryAccent.copy(alpha = 0.32f) else Color(0xFFD2DBE8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryAccent.copy(alpha = if (isDark) 0.14f else 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.2f, size.height * 0.25f),
                            radius = size.width * 0.65f
                        )
                    )
                }
                .padding(18.dp)
        ) {
            // Top Section: Radial Arc Dial + Live Power Telemetry Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Custom Radial Telemetry Dial
                Box(
                    modifier = Modifier.size(116.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val trackColor = if (isDark) Color(0xFF202738) else Color(0xFFE4EAF4)
                    val tickColor = if (isDark) Color(0xFF333D54) else Color(0xFFC5D0E0)

                    Canvas(modifier = Modifier.size(110.dp)) {
                        val strokeWidth = 10.dp.toPx()
                        val arcSize = size.width - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                        // Background 240-degree track
                        drawArc(
                            color = trackColor,
                            startAngle = 150f,
                            sweepAngle = 240f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Subtle precision tick marks inside the dial
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val innerRadius = (arcSize / 2f) - 11.dp.toPx()
                        for (i in 0..12) {
                            val angleDeg = 150f + (i * 20f)
                            val angleRad = Math.toRadians(angleDeg.toDouble())
                            val startX = center.x + (innerRadius - 3.dp.toPx()) * cos(angleRad).toFloat()
                            val startY = center.y + (innerRadius - 3.dp.toPx()) * sin(angleRad).toFloat()
                            val endX = center.x + innerRadius * cos(angleRad).toFloat()
                            val endY = center.y + innerRadius * sin(angleRad).toFloat()
                            drawLine(
                                color = if (i / 12f <= animatedBatteryFraction) primaryAccent.copy(alpha = 0.65f) else tickColor,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 1.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }

                        // Active sweep arc with gradient
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    primaryAccent.copy(alpha = 0.7f),
                                    MiuiCyan,
                                    primaryAccent
                                )
                            ),
                            startAngle = 150f,
                            sweepAngle = 240f * animatedBatteryFraction,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (telemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                            contentDescription = "Battery",
                            tint = primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${telemetry.batteryLevel}%",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (telemetry.isCharging) "CHARGING" else "ACTIVE",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryAccent,
                            letterSpacing = 0.6.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right Column: Power State, Live Wattage & Thermal Zone
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "POWER TELEMETRY",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Thermal Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(thermalColor.copy(alpha = if (isDark) 0.18f else 0.12f))
                                .border(
                                    width = 1.dp,
                                    color = thermalColor.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (telemetry.batteryTempC < 38f) "COOL" else "WARM",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = thermalColor
                            )
                        }
                    }

                    Text(
                        text = if (telemetry.isCharging) "Charging (${telemetry.batteryLevel}%)"
                        else "Discharging (${telemetry.batteryLevel}%)",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Live Power Rate & Thermal Readout Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) Color(0xFF191F2E) else Color(0xFFF3F6FC))
                            .border(
                                width = 1.dp,
                                color = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                else Color(0xFFDEE5F0),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "POWER FLOW",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 8.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (currentMaAbs > 0) String.format(Locale.US, "%.2f W", wattage) else "Dynamic W",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryAccent
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "THERMAL SENSOR",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 8.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Thermostat,
                                    contentDescription = null,
                                    tint = thermalColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = String.format(Locale.US, "%.1f°C", telemetry.batteryTempC),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = thermalColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Vital Native Telemetry Bento Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BatteryMetricTile(
                    title = "VOLTAGE",
                    value = String.format(Locale.US, "%.2f V", voltageVolts),
                    accentColor = greenColor,
                    modifier = Modifier.weight(1f)
                )

                val currentMa = telemetry.batteryCurrentMa
                val currentText = if (currentMa != 0) "${currentMa.coerceAtLeast(0)} mA" else "Dynamic"
                BatteryMetricTile(
                    title = "CURRENT",
                    value = currentText,
                    accentColor = MiuiCyan,
                    modifier = Modifier.weight(1f)
                )

                BatteryMetricTile(
                    title = "THERMAL",
                    value = String.format(Locale.US, "%.1f°C", telemetry.batteryTempC),
                    accentColor = thermalColor,
                    modifier = Modifier.weight(1f)
                )

                BatteryMetricTile(
                    title = "CAPACITY",
                    value = if (telemetry.batteryCapacityMah > 0) "${telemetry.batteryCapacityMah} mAh" else "Standard",
                    accentColor = MiuiBlue,
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
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDark) Color(0xFF181D2B) else Color(0xFFF3F6FC))
            .border(
                width = 1.dp,
                color = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.45f) else Color(0xFFDCE3EF),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.5.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.85f))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Unified Master Switch & Power Profile Command Deck Card
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

    val profileAccent by animateColorAsState(
        targetValue = when (activeProfile.uppercase()) {
            "PERFORMANCE" -> MiuiRed
            "BALANCE" -> MiuiBlue
            else -> greenColor
        },
        label = "DeckProfileAccent"
    )

    val masterScale by animateFloatAsState(
        targetValue = if (isMasterApplied) 1.0f else 0.992f,
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
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isMasterApplied) 1.2.dp else 1.dp,
            color = if (isMasterApplied) profileAccent.copy(alpha = if (isDark) 0.55f else 0.45f)
            else if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else Color(0xFFD2DBE8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (isMasterApplied) {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    profileAccent.copy(alpha = if (isDark) 0.10f else 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                    }
                }
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
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isMasterApplied) profileAccent.copy(alpha = if (isDark) 0.22f else 0.14f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.dp,
                                color = if (isMasterApplied) profileAccent.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Master Switch",
                            tint = if (isMasterApplied) profileAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "RAM Tweak Engine",
                                fontFamily = SpaceGroteskFontFamily,
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
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMasterApplied) greenColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isMasterApplied) "Volatile RAM execution active • Safe reboot reset"
                            else "Stock frequencies • All tweaks bypassed",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isMasterApplied,
                    onCheckedChange = onToggleMaster,
                    modifier = Modifier.testTag("master_engine_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = greenColor,
                        checkedBorderColor = Color.Transparent,
                        uncheckedThumbColor = if (isDark) Color(0xFF8E99AB) else Color(0xFF727E91),
                        uncheckedTrackColor = if (isDark) Color(0xFF1F2637) else Color(0xFFE2E8F2),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                thickness = 0.8.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Profile Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE POWER PROFILE",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.9.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(profileAccent.copy(alpha = if (isDark) 0.16f else 0.12f))
                        .padding(horizontal = 9.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = activeProfile.uppercase(),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = profileAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3 Sculpted Profile Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DashboardProfileChip(
                    title = "Powersave",
                    subtitle = "Cool & Long",
                    icon = Icons.Default.EnergySavingsLeaf,
                    isSelected = activeProfile.equals("POWERSAVE", ignoreCase = true),
                    activeColor = greenColor,
                    onClick = { onSelectProfile("POWERSAVE") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_chip_powersave")
                )

                DashboardProfileChip(
                    title = "Balance",
                    subtitle = "Adaptive Daily",
                    icon = Icons.Default.Speed,
                    isSelected = activeProfile.equals("BALANCE", ignoreCase = true),
                    activeColor = MiuiBlue,
                    onClick = { onSelectProfile("BALANCE") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_chip_balance")
                )

                DashboardProfileChip(
                    title = "Performance",
                    subtitle = "High Clocks",
                    icon = Icons.Default.ElectricBolt,
                    isSelected = activeProfile.equals("PERFORMANCE", ignoreCase = true),
                    activeColor = MiuiRed,
                    onClick = { onSelectProfile("PERFORMANCE") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_chip_performance")
                )
            }

            // Inline Performance Sub-Mode Selector
            AnimatedVisibility(visible = activeProfile.equals("PERFORMANCE", ignoreCase = true)) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Text(
                        text = "PERFORMANCE SUB-MODE",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.7.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "LITE" to "Fluid 90/120Hz",
                            "HEAVY" to "60/90 FPS Boost",
                            "ULTRA" to "Max Clock Lock"
                        ).forEach { (mode, desc) ->
                            val isSubSelected = performanceSubMode.equals(mode, ignoreCase = true)
                            val subColor = when (mode) {
                                "ULTRA" -> MiuiRed
                                "HEAVY" -> MiuiOrange
                                else -> greenColor
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(
                                        if (isSubSelected) subColor.copy(alpha = if (isDark) 0.22f else 0.14f)
                                        else if (isDark) Color(0xFF181D2B) else Color(0xFFF3F6FC)
                                    )
                                    .border(
                                        width = if (isSubSelected) 1.5.dp else 1.dp,
                                        color = if (isSubSelected) subColor
                                        else if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else Color(0xFFDCE3EF),
                                        shape = RoundedCornerShape(13.dp)
                                    )
                                    .clickable { onSelectPerformanceSubMode(mode) }
                                    .padding(vertical = 9.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = mode,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSubSelected) subColor else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 8.5.sp,
                                        color = if (isSubSelected) subColor.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
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
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDark()
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.97f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ProfileChipScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = if (isSelected) {
                    Brush.verticalGradient(
                        colors = listOf(
                            activeColor.copy(alpha = if (isDark) 0.25f else 0.16f),
                            activeColor.copy(alpha = if (isDark) 0.10f else 0.06f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = if (isDark) listOf(Color(0xFF181D2B), Color(0xFF181D2B))
                        else listOf(Color(0xFFF3F6FC), Color(0xFFF3F6FC))
                    )
                }
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) activeColor
                else if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else Color(0xFFDCE3EF),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) activeColor.copy(alpha = if (isDark) 0.25f else 0.18f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 9.sp,
                color = if (isSelected) activeColor.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
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
    val greenColor = if (isDark) MiuiGreen else MiuiGreenDark

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.55f) else Color(0xFFD2DBE8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: CPU Cluster & Governor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(MiuiBlue.copy(alpha = if (isDark) 0.22f else 0.14f))
                            .border(
                                width = 1.dp,
                                color = MiuiBlue.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(13.dp)
                            ),
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
                            text = "Octa-Core CPU Matrix",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "GOVERNOR:",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = telemetry.activeGovernor.ifBlank { "schedutil" },
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MiuiCyan
                            )
                        }
                    }
                }

                // Overall CPU Active Cores Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (onlineCount < 8) greenColor.copy(alpha = 0.16f)
                            else MiuiBlue.copy(alpha = 0.14f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (onlineCount < 8) greenColor.copy(alpha = 0.4f)
                            else MiuiBlue.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "$onlineCount/8 Cores",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (onlineCount < 8) greenColor else MiuiBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Efficiency Cluster 0-3 Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CLUSTER 0 • LITTLE EFFICIENCY (CORES 0–3)",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.7.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val row1 = cores.take(4)
            val row2 = cores.drop(4).take(4)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                for (core in row1) {
                    CpuCoreTile(
                        core = core,
                        clusterAccent = MiuiCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (row2.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "CLUSTER 1 • PERFORMANCE / PRIME (CORES 4–7)",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.7.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    for (core in row2) {
                        CpuCoreTile(
                            core = core,
                            clusterAccent = if (core.coreIndex == 7) MiuiPurple else MiuiBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CpuCoreTile(
    core: CpuCoreInfo,
    clusterAccent: Color,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDark()
    val isOnline = core.isOnline
    val freqRatio = if (isOnline && core.maxFreqMhz > 0) {
        (core.currentFreqMhz.toFloat() / core.maxFreqMhz.toFloat()).coerceIn(0.08f, 1f)
    } else {
        0f
    }
    val animatedRatio by animateFloatAsState(
        targetValue = freqRatio,
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "CoreFreqRatio"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (!isOnline) {
                    if (isDark) Color(0xFF23161B) else Color(0xFFFEECEC)
                } else {
                    if (isDark) Color(0xFF181D2B) else Color(0xFFF3F6FC)
                }
            )
            .border(
                width = 1.dp,
                color = if (!isOnline) MiuiRed.copy(alpha = 0.45f)
                else if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.45f) else Color(0xFFDCE3EF),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(vertical = 9.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) clusterAccent else MiuiRed)
                )
                Text(
                    text = "Core ${core.coreIndex}",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isOnline) "${core.currentFreqMhz}" else "OFF",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOnline) MaterialTheme.colorScheme.onSurface else MiuiRed
            )
            Text(
                text = if (isOnline) "MHz" else "Parked",
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 8.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Per-Core Clock Scaling Utilization Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF252D40) else Color(0xFFDCE3EF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedRatio)
                        .height(3.5.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) clusterAccent else MiuiRed)
                )
            }
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

    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.55f) else Color(0xFFD2DBE8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(greenColor.copy(alpha = if (isDark) 0.18f else 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Terminal",
                            tint = greenColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LIVE EXECUTION LOGS",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.7.sp
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(greenColor.copy(alpha = pulseAlpha))
                            )
                        }
                        Text(
                            text = "Volatile RAM sysfs & service transitions",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (recentLogs.isNotEmpty()) {
                    IconButton(
                        onClick = onClearLogs,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isDark) Color(0xFF1D2333) else Color(0xFFF0F4FA)
                            )
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

            Spacer(modifier = Modifier.height(12.dp))

            // Terminal Recessed Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDark) Color(0xFF0C1018) else Color(0xFFF4F7FC))
                    .border(
                        width = 1.dp,
                        color = if (isDark) Color(0xFF222B3D) else Color(0xFFDCE3EF),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(10.dp)
            ) {
                // Table Column Headers: TIME | TWEAK / SETTING | APPLIED VALUE | STATUS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0xFF161C2A) else Color(0xFFE7ECF6))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TIME",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(54.dp)
                    )
                    Text(
                        text = "TWEAK / SETTING",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "APPLIED VALUE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(0.85f)
                    )
                    Text(
                        text = "STATUS",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (recentLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Engine Idle • Ready for volatile RAM tweak execution",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
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
            Text(
                text = entry.timestamp,
                fontSize = 9.5.sp,
                fontFamily = JetBrainsMonoFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(54.dp)
            )

            Text(
                text = paramNameDisplay,
                fontSize = 11.sp,
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = paramValueDisplay,
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Medium,
                color = if (entry.isSuccess) (if (isDark) MiuiCyan else Color(0xFF00838F)) else MiuiRed,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.85f)
            )

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
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.isSuccess) (if (isDark) MiuiGreen else MiuiGreenDark) else MiuiRed
                )
            }
        }

        if (!isLast) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
                thickness = 0.6.dp
            )
        }
    }
}
