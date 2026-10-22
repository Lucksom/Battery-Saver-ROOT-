package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MiuiAmber
import com.example.ui.theme.MiuiBlue
import com.example.ui.theme.MiuiGreen
import com.example.ui.theme.MiuiGreenDark
import com.example.ui.theme.MiuiRed
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.isAppDark

@Composable
fun HeaderBar(
    isRootGranted: Boolean,
    themeMode: String,
    onCycleTheme: () -> Unit,
    onRevertStock: () -> Unit,
    activeProfile: String = "POWERSAVE",
    isMasterApplied: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDark()
    val badgeGreen = if (isDark) MiuiGreen else MiuiGreenDark
    val rootBadgeColor = if (isRootGranted) badgeGreen else MiuiAmber

    val profileAccent by animateColorAsState(
        targetValue = when (activeProfile.uppercase()) {
            "PERFORMANCE" -> MiuiRed
            "BALANCE" -> MiuiBlue
            else -> badgeGreen
        },
        animationSpec = tween(400),
        label = "HeaderProfileAccent"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "HeaderPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HeaderDotAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // Subtle ambient top glow matching active profile
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            profileAccent.copy(alpha = if (isDark) 0.14f else 0.08f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height
                    )
                )
            },
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Brand Emblem + Title & Status Pills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Glowing Emblem Box
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        profileAccent.copy(alpha = if (isDark) 0.28f else 0.18f),
                                        profileAccent.copy(alpha = if (isDark) 0.08f else 0.05f)
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(100f, 100f)
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = profileAccent.copy(alpha = if (isDark) 0.55f else 0.35f),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "VoltPower Emblem",
                            tint = profileAccent,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "VOLTPOWER",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            // Root Status Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(rootBadgeColor.copy(alpha = if (isDark) 0.16f else 0.12f))
                                    .border(
                                        width = 1.dp,
                                        color = rootBadgeColor.copy(alpha = if (isDark) 0.6f else 0.45f),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(rootBadgeColor.copy(alpha = pulseAlpha))
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isRootGranted) "ROOT" else "DEMO",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = rootBadgeColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "HYPEROS KERNEL ENGINE",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.4.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "•",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = if (isMasterApplied) activeProfile.uppercase() else "BYPASSED",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMasterApplied) profileAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Right Tactile Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Revert to Stock Pill Button
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = 1.dp,
                                color = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(13.dp)
                            )
                            .clickable(onClick = onRevertStock)
                            .padding(horizontal = 11.dp)
                            .testTag("header_revert_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Revert to Stock",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = "Stock",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Theme Mode Selector (System -> Dark -> Light)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = 1.dp,
                                color = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(13.dp)
                            )
                            .clickable(onClick = onCycleTheme)
                            .testTag("header_theme_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(targetState = themeMode, label = "ThemeIcon") { mode ->
                            when (mode) {
                                "DARK" -> Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = "Dark Theme",
                                    tint = MiuiBlue,
                                    modifier = Modifier.size(19.dp)
                                )
                                "LIGHT" -> Icon(
                                    imageVector = Icons.Default.LightMode,
                                    contentDescription = "Light Theme",
                                    tint = MiuiAmber,
                                    modifier = Modifier.size(19.dp)
                                )
                                else -> Icon(
                                    imageVector = Icons.Default.BrightnessAuto,
                                    contentDescription = "System Theme",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
