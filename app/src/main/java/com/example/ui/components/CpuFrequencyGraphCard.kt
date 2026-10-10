package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MiuiAmber
import com.example.ui.theme.MiuiCyan
import com.example.ui.theme.MiuiGreen
import com.example.ui.theme.MiuiOrange
import com.example.ui.theme.MiuiRed

@Composable
fun CpuFrequencyGraphCard(
    history: List<Int>,
    activeProfile: String = "POWERSAVE",
    performanceSubMode: String = "LITE",
    isMasterApplied: Boolean = false,
    cpuFreqCapEnabled: Boolean = true,
    activeGovernor: String = "schedutil",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseRadius"
    )

    val currentAvg = history.lastOrNull() ?: 800
    val maxScaleFreq = 2400f // Max scale range (MHz)

    // Compute dynamic styling and labels based on the currently active profile and sub-mode
    val isPerformance = activeProfile == "PERFORMANCE"
    val isBalance = activeProfile == "BALANCE"
    val isPowersave = activeProfile == "POWERSAVE"

    val subModeUpper = performanceSubMode.uppercase()
    val isUltra = isPerformance && subModeUpper == "ULTRA"
    val isHeavy = isPerformance && subModeUpper == "HEAVY"
    val isLite = isPerformance && subModeUpper == "LITE"

    val lineColor = when {
        isUltra -> MiuiRed
        isHeavy -> MiuiOrange
        isLite -> MiuiGreen
        isBalance -> MiuiCyan
        isPowersave -> MiuiGreen
        else -> MiuiAmber
    }

    val badgeText = when {
        isMasterApplied -> when {
            isUltra -> "Ultra (Max Clocks)"
            isHeavy -> "Heavy (45% Floor)"
            isLite -> "Lite (90/120Hz)"
            isBalance -> "Balance (70% Cap)"
            isPowersave -> "Powersave (50% Cap)"
            else -> "Gov: $activeGovernor"
        }
        else -> when {
            isUltra -> "Ultra Perf (Idle)"
            isHeavy -> "Heavy Perf (Idle)"
            isLite -> "Lite Perf (Idle)"
            isBalance -> "Balance (Idle)"
            isPowersave -> "Powersave (Idle)"
            else -> "Gov: $activeGovernor"
        }
    }

    val badgeColor = when {
        isUltra -> MiuiRed
        isHeavy -> MiuiOrange
        isLite -> MiuiGreen
        isBalance -> MiuiCyan
        isPowersave -> MiuiGreen
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val subtitleText = when {
        isMasterApplied -> when {
            isUltra -> "Locked at maximum hardware frequencies"
            isHeavy -> "Elevated floor + unconstrained headroom"
            isLite -> "Fluid 90/120Hz unconstrained headroom"
            isBalance -> "Clocks scaled in balanced 70% band"
            isPowersave -> "Clocks locked in low energy band"
            else -> "Stock dynamic frequency scaling"
        }
        else -> when {
            isUltra -> "Target: Uncapped clocks • Tap Master switch to apply"
            isHeavy -> "Target: 45% floor + boost • Tap Master switch to apply"
            isLite -> "Target: Fluid 90/120Hz • Tap Master switch to apply"
            isBalance -> "Target: 70% balanced band • Tap Master switch to apply"
            isPowersave -> "Target: 50% low energy band • Tap Master switch to apply"
            else -> "Stock dynamic frequency scaling"
        }
    }

    val referenceLineFreq: Float? = when {
        isPowersave && cpuFreqCapEnabled -> 1200f
        isBalance && cpuFreqCapEnabled -> 1680f
        isHeavy -> 1100f
        isUltra -> 2100f
        else -> null
    }

    val referenceLineLabel: String? = when {
        isPowersave && cpuFreqCapEnabled -> "─── 50% Cap Level"
        isBalance && cpuFreqCapEnabled -> "─── 70% Cap Level"
        isHeavy -> "─── 45% Floor Level"
        isUltra -> "─── Max Clock Target"
        else -> null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "CPU Frequency Tracker",
                        tint = lineColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Average CPU Clock Frequency",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isMasterApplied) badgeColor.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            // Realtime Frequency Big Display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$currentAvg",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " MHz avg",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }

                Text(
                    text = subtitleText,
                    fontSize = 11.sp,
                    color = if (isMasterApplied) badgeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Canvas Line Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Grid lines (500, 1000, 1500, 2000 MHz)
                    val gridSteps = listOf(0.25f, 0.5f, 0.75f)
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                    gridSteps.forEach { step ->
                        val y = height * (1f - step)
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.15f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f,
                            pathEffect = dashEffect
                        )
                    }

                    // Reference dashed line if active
                    if (referenceLineFreq != null) {
                        val capY = height * (1f - (referenceLineFreq / maxScaleFreq).coerceIn(0f, 1f))
                        drawLine(
                            color = lineColor.copy(alpha = 0.5f),
                            start = Offset(0f, capY),
                            end = Offset(width, capY),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )
                    }

                    // Build line and fill path
                    if (history.isNotEmpty()) {
                        val points = history.takeLast(20)
                        val stepX = if (points.size > 1) width / (points.size - 1) else width

                        val linePath = Path()
                        val fillPath = Path()

                        points.forEachIndexed { index, freq ->
                            val normalized = (freq / maxScaleFreq).coerceIn(0.05f, 0.95f)
                            val x = index * stepX
                            val y = height * (1f - normalized)

                            if (index == 0) {
                                linePath.moveTo(x, y)
                                fillPath.moveTo(x, height)
                                fillPath.lineTo(x, y)
                            } else {
                                linePath.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }
                        }

                        // Close fill path
                        fillPath.lineTo(width, height)
                        fillPath.close()

                        // Draw Gradient Fill under line
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    lineColor.copy(alpha = 0.28f),
                                    lineColor.copy(alpha = 0.02f)
                                )
                            )
                        )

                        // Draw Main Line
                        drawPath(
                            path = linePath,
                            color = lineColor,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Draw glowing pulsating point at current live end
                        val lastPoint = points.last()
                        val lastY = height * (1f - (lastPoint / maxScaleFreq).coerceIn(0.05f, 0.95f))
                        val lastX = width

                        // Outer glowing pulse
                        drawCircle(
                            color = lineColor.copy(alpha = 0.35f),
                            radius = pulseRadius.dp.toPx(),
                            center = Offset(lastX, lastY)
                        )
                        // Inner solid point
                        drawCircle(
                            color = lineColor,
                            radius = 4.dp.toPx(),
                            center = Offset(lastX, lastY)
                        )
                    }
                }
            }

            // Graph Axis Footnotes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "30s ago",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                if (referenceLineLabel != null) {
                    Text(
                        text = referenceLineLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = lineColor
                    )
                }
                Text(
                    text = "Now (Live)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = lineColor
                )
            }
        }
    }
}
