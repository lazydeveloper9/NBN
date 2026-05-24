package com.example.nbn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PulseScreen(
    modifier: Modifier = Modifier,
    viewModel: PulseViewModel = viewModel()
) {
    val logs by viewModel.agentLogs.collectAsState()

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(logs.size) { index ->
                AgentActionCard(
                    title = "System Log",
                    description = logs[index],
                    timestamp = "Just now",
                    icon = Icons.Default.Info,
                    iconTint = MaterialTheme.colorScheme.primary
                )
            }
            if (logs.isEmpty()) {
                item {
                    Text("No agent activity yet.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun AgentActionCard(
    title: String,
    description: String,
    timestamp: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }
            Text(text = timestamp, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}
