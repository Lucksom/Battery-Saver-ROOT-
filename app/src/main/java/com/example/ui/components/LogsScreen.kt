package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.log.LogCategory
import com.example.data.log.LogEntry
import com.example.ui.theme.MiuiAmber
import com.example.ui.theme.MiuiBlue
import com.example.ui.theme.MiuiCyan
import com.example.ui.theme.MiuiGreen
import com.example.ui.theme.MiuiGreenDark
import com.example.ui.theme.MiuiOrange
import com.example.ui.theme.MiuiPurple

@Composable
fun LogsScreen(
    logs: List<LogEntry>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf<LogCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredLogs = remember(logs, selectedCategory, searchQuery) {
        logs.filter { entry ->
            val matchCategory = selectedCategory == null || entry.category == selectedCategory
            val matchSearch = searchQuery.isBlank() ||
                    entry.displayName.contains(searchQuery, ignoreCase = true) ||
                    entry.displayValue.contains(searchQuery, ignoreCase = true) ||
                    (entry.command?.contains(searchQuery, ignoreCase = true) == true)
            matchCategory && matchSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        val isDark = isSystemInDarkTheme()
        val greenColor = if (isDark) MiuiGreen else MiuiGreenDark

        // Title and Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(greenColor)
                    )
                    Text(
                        text = "Real-Time Execution Logs",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = "${filteredLogs.size} recorded events in volatile memory",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Copy button
                IconButton(
                    onClick = {
                        val text = filteredLogs.joinToString("\n") {
                            "[${it.timestamp}] [${it.category}] ${it.displayName}: ${it.displayValue}${it.command?.let { c -> " ($c)" } ?: ""}"
                        }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText("VoltPower Logs", text)
                        clipboard?.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied ${filteredLogs.size} logs to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Logs",
                        tint = MiuiBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.size(8.dp))

                // Clear button
                IconButton(
                    onClick = onClearLogs,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear Logs",
                        tint = MiuiOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter logs by tweak name or value...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                label = "ALL",
                isSelected = selectedCategory == null,
                onClick = { selectedCategory = null },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                label = "TWEAKS",
                isSelected = selectedCategory == LogCategory.TWEAK,
                onClick = { selectedCategory = LogCategory.TWEAK },
                activeColor = MiuiCyan,
                modifier = Modifier.weight(1.1f)
            )
            FilterChip(
                label = "PROFILE",
                isSelected = selectedCategory == LogCategory.PROFILE,
                onClick = { selectedCategory = LogCategory.PROFILE },
                activeColor = MiuiBlue,
                modifier = Modifier.weight(1.2f)
            )
            FilterChip(
                label = "SYSTEM",
                isSelected = selectedCategory == LogCategory.SYSTEM,
                onClick = { selectedCategory = LogCategory.SYSTEM },
                activeColor = MiuiPurple,
                modifier = Modifier.weight(1.1f)
            )
            FilterChip(
                label = "ROOT",
                isSelected = selectedCategory == LogCategory.ROOT,
                onClick = { selectedCategory = LogCategory.ROOT },
                activeColor = greenColor,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SINGLE UNIFIED CARD SHOWING APPLYING TWEAKS WITH PROPER NAMES AND VALUES
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 12.dp),
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Card Header: Applying Tweaks Terminal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(greenColor)
                        )
                        Text(
                            text = "Applying Tweaks Console",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(greenColor.copy(alpha = if (isDark) 0.15f else 0.10f))
                            .border(
                                1.dp,
                                greenColor.copy(alpha = if (isDark) 0.4f else 0.25f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE STREAM",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = greenColor
                        )
                    }
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.5f)
                )

                // Header for columns: Time | Tweak Name | Value | Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color(0xFFF3F5FA))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TIME",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(58.dp)
                    )
                    Text(
                        text = "NAME & TWEAK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "APPLIED VALUE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.2f)
                    )
                    Text(
                        text = "STATUS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(42.dp),
                        textAlign = TextAlign.End
                    )
                }

                HorizontalDivider(
                    thickness = 0.8.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.25f else 0.4f)
                )

                // Scrollable log rows inside this single card
                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tweak logs match current filter",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredLogs, key = { it.id }) { logItem ->
                            LogRow(logItem = logItem, isDark = isDark, greenColor = greenColor)
                            HorizontalDivider(
                                thickness = 0.6.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.2f else 0.35f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogRow(
    logItem: LogEntry,
    isDark: Boolean,
    greenColor: Color
) {
    val categoryColor = when (logItem.category) {
        LogCategory.ROOT -> greenColor
        LogCategory.TWEAK -> if (isDark) MiuiCyan else Color(0xFF00838F)
        LogCategory.PROFILE -> MiuiBlue
        LogCategory.SYSTEM -> MiuiPurple
        LogCategory.ERROR -> MiuiOrange
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time
            Text(
                text = logItem.timestamp,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(58.dp)
            )

            // Name
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(categoryColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = logItem.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Value
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else Color(0xFFF3F5FA)
                    )
                    .border(
                        0.8.dp,
                        if (isDark) Color.Transparent else Color(0xFFDFE4ED),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = logItem.displayValue,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = if (logItem.isSuccess) (if (isDark) MiuiCyan else Color(0xFF00838F)) else MiuiOrange,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Status Icon
            Box(
                modifier = Modifier.width(42.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = if (logItem.isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                    contentDescription = if (logItem.isSuccess) "Success" else "Error",
                    tint = if (logItem.isSuccess) greenColor else MiuiOrange,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // Secondary command / sysfs detail line if present
        if (!logItem.command.isNullOrBlank() && logItem.command != logItem.displayValue) {
            Text(
                text = "$ ${logItem.command}",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.padding(start = 58.dp, top = 3.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    activeColor: Color = MiuiBlue,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) activeColor.copy(alpha = if (isDark) 0.2f else 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.6f else 0.5f)
            )
            .border(
                1.dp,
                if (isSelected) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
