package com.example.nbn.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nbn.network.ApiClient
import com.example.nbn.network.InventoryAddRequest
import com.example.nbn.network.InventoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VaultViewModel : ViewModel() {

    private val _inventory = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventory: StateFlow<List<InventoryItem>> = _inventory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchInventory()
    }

    fun fetchInventory() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val items = ApiClient.apiService.getInventory()
                _inventory.value = items
            } catch (e: Exception) {
                // For demo purposes, we ignore errors, but ideally we show a snackbar
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addInventoryItem(name: String, quantity: Int, price: Double, description: String, imageUrl: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val request = InventoryAddRequest(
                    name = name,
                    quantity = quantity,
                    price = price,
                    description = description,
                    image_url = imageUrl
                )
                val newItem = ApiClient.apiService.addInventory(request)
                
                // Add to top of list locally to avoid needing another fetch
                val current = _inventory.value.toMutableList()
                current.add(0, newItem)
                _inventory.value = current
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
