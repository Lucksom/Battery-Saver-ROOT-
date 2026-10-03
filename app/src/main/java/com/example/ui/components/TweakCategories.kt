package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
fun TweakCategories(
    config: TweakConfigEntity,
    onUpdateTweak: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "GRANULAR KERNEL CONTROLS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 10.dp, bottom = 2.dp, start = 4.dp)
        )

        // 1. CPU Frequency & Clocks
        TweakCategoryGroup(
            title = "CPU Clocks & Governors",
            badgeText = "2 Tweaks",
            icon = Icons.Default.Speed,
            iconColor = MiuiGreen,
            defaultExpanded = true
        ) {
            TweakItem(
                title = "50% CPU Max Frequency Cap",
                description = "Caps all CPU clusters at 50% max clock in RAM to save up to 40% battery",
                icon = Icons.Default.Speed,
                iconColor = MiuiGreen,
                isChecked = config.cpuFreqCapEnabled,
                onCheckedChange = { onUpdateTweak("cpuFreqCap", it) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
            TweakItem(
                title = "Schedutil Slow Ramp-Up & 99% Threshold",
                description = "Delays frequency ramp-up and sets hispeed load to 99% to prevent micro-spikes",
                icon = Icons.Default.FastForward,
                iconColor = MiuiCyan,
                isChecked = config.schedutilRateLimitsEnabled,
                onCheckedChange = { onUpdateTweak("schedutilRate", it) }
            )
        }

        // 2. Smart Core Control (2 Cores)
        TweakCategoryGroup(
            title = "Dynamic Core Topology",
            badgeText = "Safe 2-Core",
            icon = Icons.Default.Memory,
            iconColor = MiuiPurple,
            defaultExpanded = true
        ) {
            TweakItem(
                title = "Offline 2 Prime Cores Below 20%",
                description = "Smart battery protection: Shuts down only Cores 6 & 7 (Prime high-power) when battery <= 20%, keeping 6 responsive cores active",
                icon = Icons.Default.Memory,
                iconColor = MiuiPurple,
                isChecked = config.twoCoresOfflineBelow20Enabled,
                onCheckedChange = { onUpdateTweak("twoCoresOffline", it) }
            )
        }

        // 3. Google Play Services (GMS) Doze
        TweakCategoryGroup(
            title = "Google Services & Background",
            badgeText = "Safe Native Doze",
            icon = Icons.Default.CloudSync,
            iconColor = MiuiBlue,
            defaultExpanded = true
        ) {
            TweakItem(
                title = "GMS Deep Doze (Safe Whitelist Toggle)",
                description = "Removes Google Play Services from battery saver exemption whitelist. Eliminates idle drain without breaking apps or packages!",
                icon = Icons.Default.SecurityUpdateGood,
                iconColor = MiuiBlue,
                isChecked = config.gmsDozeEnabled,
                onCheckedChange = { onUpdateTweak("gmsDoze", it) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
            TweakItem(
                title = "Disable Touch & Input Boost",
                description = "Stops CPU clocks from spiking during screen touches and fingerprint unlocks",
                icon = Icons.Default.TouchApp,
                iconColor = MiuiAmber,
                isChecked = config.inputTouchBoostDisabled,
                onCheckedChange = { onUpdateTweak("inputTouchBoost", it) }
            )
        }

        // 4. GPU & Graphics
        TweakCategoryGroup(
            title = "GPU & Graphics",
            badgeText = "2 Tweaks",
            icon = Icons.Default.VideogameAsset,
            iconColor = MiuiOrange
        ) {
            TweakItem(
                title = "50% GPU Max Frequency Cap",
                description = "Reduces maximum GPU rendering clocks by 50% for lower thermals and power draw",
                icon = Icons.Default.VideogameAsset,
                iconColor = MiuiOrange,
                isChecked = config.gpuPowerLimitEnabled,
                onCheckedChange = { onUpdateTweak("gpuPowerLimit", it) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
            TweakItem(
                title = "Enable Adreno Idler",
                description = "Aggressively puts Adreno graphics engine into idle state between frames",
                icon = Icons.Default.Thermostat,
                iconColor = MiuiCyan,
                isChecked = config.adrenoIdlerEnabled,
                onCheckedChange = { onUpdateTweak("adrenoIdler", it) }
            )
        }

        // 5. Storage, I/O Queue & Dynamic Fsync
        TweakCategoryGroup(
            title = "Storage & Filesystem",
            badgeText = "2 Tweaks",
            icon = Icons.Default.Storage,
            iconColor = MiuiCyan
        ) {
            TweakItem(
                title = "I/O Queue Optimization",
                description = "Reduces read-ahead to 128KB and queue depth to 64 for lower storage wakeups",
                icon = Icons.Default.Storage,
                iconColor = MiuiCyan,
                isChecked = config.storageIoQueueEnabled,
                onCheckedChange = { onUpdateTweak("storageIo", it) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
            TweakItem(
                title = "Dynamic Fsync",
                description = "Enables Dynamic Fsync in RAM (flushes disk commits only when screen is off)",
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
            badgeText = "3 Tweaks",
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
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "ExpandRot"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .hyperBounceClick(scaleDown = 0.98f) { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconColor.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    content()
                }
            }
        }
    }
}
