package com.example.nbn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun OrchestratorScreen(
    modifier: Modifier = Modifier,
    viewModel: OrchestratorViewModel = viewModel()
) {
    var textInput by remember { mutableStateOf("") }
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        // Chat Thread
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(chatHistory.size) { index ->
                val msg = chatHistory[index]
                if (msg.isUser) {
                    UserMessageBubble(text = msg.text)
                } else {
                    AiMessageBubble(
                        text = msg.text,
                        showWebsitePreview = msg.action?.type == "website_preview",
                        previewUrl = msg.action?.url ?: ""
                    )
                }
            }
            if (isLoading) {
                item {
                    Text("Agent is typing...", color = Color.Gray, modifier = Modifier.padding(8.dp))
                }
            }
        }
        
        // Bottom Input Row
        Surface(color = Color.White, shadowElevation = 8.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Open Camera */ }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.Gray)
                }
                
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    placeholder = { Text("Message...") },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = Color(0xFFEEEEEE),
                        focusedContainerColor = Color(0xFFEEEEEE)
                    )
                )
                
                if (textInput.isBlank()) {
                    IconButton(onClick = { /* Start Voice */ }, modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))) {
                        Icon(Icons.Default.Mic, contentDescription = "Mic", tint = Color.White)
                    }
                } else {
                    IconButton(
                        onClick = { 
                            viewModel.sendMessage(textInput)
                            textInput = ""
                        }, 
                        modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun UserMessageBubble(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp))
                .padding(12.dp)
        ) {
            Text(text, color = Color.White)
        }
    }
}

@Composable
fun AiMessageBubble(text: String, showWebsitePreview: Boolean = false, previewUrl: String = "") {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Column(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp))
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(text, color = Color.Black)
            
            if (showWebsitePreview) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Preview: $previewUrl", style = MaterialTheme.typography.titleMedium, color = Color.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("DevOps successfully deployed the PWA.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { /* Approve */ }, modifier = Modifier.fillMaxWidth()) {
                            Text("Open Page")
                        }
                    }
                }
            }
        }
    }
}
