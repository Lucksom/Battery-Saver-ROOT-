package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MiuiBlue
import com.example.ui.theme.MiuiGreen
import com.example.ui.theme.MiuiGreenDark
import com.example.ui.theme.MiuiRed
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.isAppDark

enum class VoltPowerTab(
    val title: String,
    val subtitle: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD(
        title = "Cockpit",
        subtitle = "Telemetry & Profiles",
        selectedIcon = Icons.Filled.Speed,
        unselectedIcon = Icons.Outlined.Speed
    ),
    TWEAKS(
        title = "Kernel Tweaks",
        subtitle = "Governor & Modules",
        selectedIcon = Icons.Filled.Tune,
        unselectedIcon = Icons.Outlined.Tune
    )
}

@Composable
fun VoltPowerBottomBar(
    currentTab: VoltPowerTab,
    onTabSelected: (VoltPowerTab) -> Unit,
    activeProfile: String = "POWERSAVE",
    modifier: Modifier = Modifier
) {
    val isDark = isAppDark()
    val greenAccent = if (isDark) MiuiGreen else MiuiGreenDark
    val activeAccent by animateColorAsState(
        targetValue = when (activeProfile.uppercase()) {
            "PERFORMANCE" -> MiuiRed
            "BALANCE" -> MiuiBlue
            else -> greenAccent
        },
        label = "BottomBarAccent"
    )
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            color = if (isDark) Color(0xFF141926).copy(alpha = 0.96f) else Color(0xFFFFFFFF).copy(alpha = 0.96f),
            shadowElevation = if (isDark) 14.dp else 8.dp,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VoltPowerTab.values().forEach { tab ->
                    val isSelected = (currentTab == tab)
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.0f else 0.97f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "TabScale"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) activeAccent else unselectedColor,
                        label = "tabTextColor"
                    )
                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) activeAccent else unselectedColor,
                        label = "tabIconTint"
                    )

                    val interactionSource = remember { MutableInteractionSource() }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                brush = if (isSelected) {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            activeAccent.copy(alpha = if (isDark) 0.22f else 0.14f),
                                            activeAccent.copy(alpha = if (isDark) 0.10f else 0.06f)
                                        )
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color.Transparent, Color.Transparent)
                                    )
                                }
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) activeAccent.copy(alpha = if (isDark) 0.5f else 0.35f)
                                else Color.Transparent,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(),
                                onClick = { onTabSelected(tab) }
                            )
                            .padding(horizontal = 12.dp)
                            .testTag("bottom_tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tab.title,
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = textColor
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(activeAccent)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
