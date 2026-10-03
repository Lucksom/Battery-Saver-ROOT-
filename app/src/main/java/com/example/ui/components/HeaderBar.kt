package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MiuiAmber
import com.example.ui.theme.MiuiGreen

@Composable
fun HeaderBar(
    isRootGranted: Boolean,
    themeMode: String,
    onCycleTheme: () -> Unit,
    onRevertStock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "VoltPower",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isRootGranted) MiuiGreen.copy(alpha = 0.15f)
                                else MiuiAmber.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Root Status",
                                tint = if (isRootGranted) MiuiGreen else MiuiAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isRootGranted) "ROOT" else "NO ROOT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRootGranted) MiuiGreen else MiuiAmber,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "v1.1.0",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Text(
                    text = "Kernel & Power Governor Engine",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Revert to Stock Button
                IconButton(
                    onClick = onRevertStock,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Revert to Stock",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Theme Mode Selector (System -> Dark -> Light)
                IconButton(
                    onClick = onCycleTheme,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    AnimatedContent(targetState = themeMode, label = "ThemeIcon") { mode ->
                        when (mode) {
                            "DARK" -> Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = "Dark Theme",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            "LIGHT" -> Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = "Light Theme",
                                tint = MiuiAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            else -> Icon(
                                imageVector = Icons.Default.BrightnessAuto,
                                contentDescription = "System Theme",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
