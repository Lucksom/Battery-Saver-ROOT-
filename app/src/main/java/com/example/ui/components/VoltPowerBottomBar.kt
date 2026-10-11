package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MiuiBlue

enum class VoltPowerTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Speed),
    TWEAKS("Tweaks", Icons.Default.Settings),
    PROFILE("Profile", Icons.Default.Bolt),
    LOGS("Logs", Icons.Default.Article)
}

@Composable
fun VoltPowerBottomBar(
    currentTab: VoltPowerTab,
    onTabSelected: (VoltPowerTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
            ),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            VoltPowerTab.values().forEach { tab ->
                val isSelected = (currentTab == tab)
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MiuiBlue else unselectedColor,
                    label = "tabTextColor"
                )
                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) MiuiBlue else unselectedColor,
                    label = "tabIconTint"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .clickable {
                            onTabSelected(tab)
                        }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MiuiBlue.copy(alpha = if (isDark) 0.2f else 0.12f)
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
