package com.example.nbn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var currentStep by remember { mutableStateOf(1) }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (currentStep) {
            1 -> {
                Text("Step 1 of 2", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Let's build your store.",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Take a picture of your store/workspace or upload your logo.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = { currentStep = 2 },
                    modifier = Modifier.size(120.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera", modifier = Modifier.size(48.dp))
                }
            }
            
            2 -> {
                Text("Step 2 of 2", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Tell me about your business.",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Hold the mic and tell me what you do. (e.g. 'I run a bakery making custom cakes')",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = { currentStep = 3 },
                    modifier = Modifier.size(120.dp),
                    shape = RoundedCornerShape(60.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Mic", modifier = Modifier.size(48.dp))
                }
            }
            
            3 -> {
                var showButton by remember { mutableStateOf(false) }
                
                LaunchedEffect(Unit) {
                    delay(5000)
                    showButton = true
                }
                
                if (!showButton) {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "DevOps Agent is building your cloud infrastructure...",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Your store is ready!",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("View Your New Website")
                    }
                }
            }
        }
    }
}
