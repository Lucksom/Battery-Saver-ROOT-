package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.theme.isAppDark

@Composable
fun TweakItem(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDark()
    val badgeBorderAlpha = if (isChecked) 0.55f else 0.22f
    val badgeBgColor by animateColorAsState(
        targetValue = if (isChecked) {
            iconColor.copy(alpha = if (isDark) 0.24f else 0.16f)
        } else {
            iconColor.copy(alpha = if (isDark) 0.12f else 0.08f)
        },
        label = "TweakIconBg"
    )

    SwitchPreference(
        title = title,
        summary = description,
        checked = isChecked,
        onCheckedChange = onCheckedChange,
        accentColor = iconColor,
        startAction = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                badgeBgColor,
                                badgeBgColor.copy(alpha = badgeBgColor.alpha * 0.5f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = iconColor.copy(alpha = badgeBorderAlpha),
                        shape = RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isChecked) iconColor else iconColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        modifier = modifier
    )
}
