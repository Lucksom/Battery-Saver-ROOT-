package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.log.LogCategory
import com.example.data.log.LogEntry
import com.example.ui.theme.MiuiAmber
import com.example.ui.theme.MiuiBlue
import com.example.ui.theme.MiuiCyan
import com.example.ui.theme.MiuiGreen
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
                    entry.message.contains(searchQuery, ignoreCase = true) ||
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
                            .background(MiuiGreen)
                    )
                    Text(
                        text = "Real-Time Execution Logs",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF0F3F8),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = "${filteredLogs.size} recorded events in volatile memory",
                    fontSize = 11.sp,
                    color = Color(0xFF9DA4B0)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Copy button
                IconButton(
                    onClick = {
                        val text = filteredLogs.joinToString("\n") {
                            "[${it.timestamp}] [${it.category}] ${it.message}${it.command?.let { c -> " ($c)" } ?: ""}"
                        }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText("VoltPower Logs", text)
                        clipboard?.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied ${filteredLogs.size} logs to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B202D))
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
                        .background(Color(0xFF1B202D))
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
            placeholder = { Text("Filter logs or shell commands...", fontSize = 12.sp, color = Color(0xFF6B7280)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF9DA4B0),
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = Color(0xFF9DA4B0),
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
                focusedContainerColor = Color(0xFF161A24),
                unfocusedContainerColor = Color(0xFF161A24),
                focusedBorderColor = MiuiBlue,
                unfocusedBorderColor = Color(0xFF222838),
                focusedTextColor = Color(0xFFF0F3F8),
                unfocusedTextColor = Color(0xFFF0F3F8)
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
                label = "ROOT",
                isSelected = selectedCategory == LogCategory.ROOT,
                onClick = { selectedCategory = LogCategory.ROOT },
                activeColor = MiuiGreen,
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
                label = "ERRORS",
                isSelected = selectedCategory == LogCategory.ERROR,
                onClick = { selectedCategory = LogCategory.ERROR },
                activeColor = MiuiOrange,
                modifier = Modifier.weight(1.2f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Logs List
        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Empty",
                        tint = Color(0xFF4B5563),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No logs match current filter",
                        fontSize = 14.sp,
                        color = Color(0xFF9DA4B0),
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredLogs, key = { it.id }) { logItem ->
                    LogCardItem(logItem = logItem)
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
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
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) activeColor.copy(alpha = 0.2f)
                else Color(0xFF161A24)
            )
            .border(
                1.dp,
                if (isSelected) activeColor else Color(0xFF222838),
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
            color = if (isSelected) activeColor else Color(0xFF9DA4B0)
        )
    }
}

@Composable
private fun LogCardItem(logItem: LogEntry) {
    val categoryColor = when (logItem.category) {
        LogCategory.ROOT -> MiuiGreen
        LogCategory.TWEAK -> MiuiCyan
        LogCategory.PROFILE -> MiuiBlue
        LogCategory.SYSTEM -> MiuiPurple
        LogCategory.ERROR -> MiuiOrange
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF141720)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF202636))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(categoryColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = logItem.category.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                    }

                    Text(
                        text = logItem.timestamp,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF9DA4B0),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Icon(
                    imageVector = if (logItem.isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                    contentDescription = "Status",
                    tint = if (logItem.isSuccess) MiuiGreen else MiuiOrange,
                    modifier = Modifier.size(14.dp)
                )
            }

            if (!logItem.command.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C0E14))
                        .border(1.dp, Color(0xFF1E2331), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$ ${logItem.command}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MiuiGreen,
                        maxLines = 3
                    )
                }
            }

            Text(
                text = logItem.message,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFE2E6EE),
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
