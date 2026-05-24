package com.example.nbn.ui.screens

import android.graphics.Bitmap
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.nbn.network.ApiClient
import com.example.nbn.network.MessageRequest
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    modifier: Modifier = Modifier,
    viewModel: VaultViewModel = viewModel()
) {
    val inventory by viewModel.inventory.collectAsState()
    
    var showBottomSheet by remember { mutableStateOf(false) }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            capturedImage = bitmap
            showBottomSheet = true
        }
    }
    
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = { cameraLauncher.launch(null) },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AddAPhoto, contentDescription = "Scan")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Scan New Item", style = MaterialTheme.typography.titleMedium)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (inventory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No items in vault. Scan one to get started!", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(inventory.size) { index ->
                    val item = inventory[index]
                    InventoryCard(item)
                }
            }
        }
    }
    
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false; capturedImage = null },
            sheetState = sheetState
        ) {
            var itemName by remember { mutableStateOf("") }
            var itemDesc by remember { mutableStateOf("") }
            var itemPrice by remember { mutableStateOf("") }
            var itemQty by remember { mutableStateOf("") }
            
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Vision AI Auto-Fill", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                if (capturedImage != null) {
                    Image(
                        bitmap = capturedImage!!.asImageBitmap(),
                        contentDescription = "Captured",
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Detected Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = itemDesc,
                    onValueChange = { itemDesc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = itemPrice,
                        onValueChange = { itemPrice = it },
                        label = { Text("Price (₹)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = itemQty,
                        onValueChange = { itemQty = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Convert Bitmap to Base64 for the API
                        val baos = ByteArrayOutputStream()
                        capturedImage?.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                        val b64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                        val dataUri = "data:image/jpeg;base64,$b64"

                        viewModel.addInventoryItem(
                            name = itemName.ifBlank { "Unknown Item" },
                            quantity = itemQty.toIntOrNull() ?: 0,
                            price = itemPrice.toDoubleOrNull() ?: 0.0,
                            description = itemDesc,
                            imageUrl = dataUri
                        )
                        showBottomSheet = false
                        capturedImage = null
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Confirm & Sync")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun InventoryCard(item: com.example.nbn.network.InventoryItem) {
    var showDealerMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomStart) {
            // Check if it's a base64 data URI
            val decodedBitmap = remember(item.image_url) {
                if (item.image_url?.startsWith("data:image/") == true) {
                    try {
                        val base64String = item.image_url.substringAfter("base64,")
                        val bytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) { null }
                } else null
            }

            if (decodedBitmap != null) {
                Image(
                    bitmap = decodedBitmap.asImageBitmap(),
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize().background(Color.White),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = item.image_url,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize().background(Color.White),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(modifier = Modifier.background(Color(0x88000000)).fillMaxWidth().padding(8.dp)) {
                Text(item.name, color = Color.White, fontWeight = FontWeight.Bold)
                Text("₹${item.price} • ${item.quantity} in stock", color = Color.White, style = MaterialTheme.typography.bodySmall)
                
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        Text(
                            text = "Order Stock",
                            color = MaterialTheme.colorScheme.primaryContainer,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showDealerMenu = true }.padding(4.dp)
                        )
                        DropdownMenu(
                            expanded = showDealerMenu,
                            onDismissRequest = { showDealerMenu = false }
                        ) {
                            val dealers = listOf("Rajesh Suppliers", "Global Imports", "Local Clay Co.")
                            dealers.forEach { dealer ->
                                DropdownMenuItem(text = { Text(dealer) }, onClick = { 
                                    showDealerMenu = false 
                                    Toast.makeText(context, "Order sent to $dealer! Check Chat.", Toast.LENGTH_LONG).show()
                                    
                                    // Append user intent to shared Chat UI
                                    DemoState.addSystemMessage("You ordered more ${item.name} from $dealer.")
                                    
                                    // Send to backend Orchestrator AI
                                    coroutineScope.launch {
                                        try {
                                            val response = ApiClient.apiService.sendChat(
                                                MessageRequest("demo_msme_user", "Order 50 more of ${item.name} from $dealer")
                                            )
                                            DemoState.addSystemMessage("AI Ops Agent: ${response.reply}")
                                        } catch (e: Exception) {
                                            DemoState.addSystemMessage("AI Ops Agent: Processed order offline.")
                                        }
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}
