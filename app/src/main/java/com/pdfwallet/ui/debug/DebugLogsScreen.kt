package com.pdfwallet.ui.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugLogsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DebugLogsViewModel = hiltViewModel()
) {
    val logs by viewModel.logs.collectAsState(initial = emptyList())
    val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug Logs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Logs")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(logs) { log ->
                val bgColor = when (log.severity) {
                    "ERROR" -> Color(0xFFFFEBEE)
                    "WARN" -> Color(0xFFFFF8E1)
                    "DEBUG" -> Color(0xFFE8F5E9)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val textColor = when (log.severity) {
                    "ERROR" -> Color(0xFFC62828)
                    "WARN" -> Color(0xFFF9A825)
                    "DEBUG" -> Color(0xFF2E7D32)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "[${log.severity}] ${log.tag}",
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = dateFormat.format(Date(log.timestamp)),
                                color = textColor.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = log.message,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor
                        )
                        if (log.exceptionTrace != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = log.exceptionTrace,
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Red.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}
