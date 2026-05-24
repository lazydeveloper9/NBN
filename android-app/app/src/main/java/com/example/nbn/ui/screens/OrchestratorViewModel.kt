package com.example.nbn.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nbn.network.ApiClient
import com.example.nbn.network.ActionData
import com.example.nbn.network.MessageRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val isUser: Boolean,
    val text: String,
    val action: ActionData? = null
)

class OrchestratorViewModel : ViewModel() {

    val chatHistory: StateFlow<List<ChatMessage>> = DemoState.chatHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // Add user message
        val currentList = DemoState.chatHistory.value.toMutableList()
        currentList.add(ChatMessage(isUser = true, text = text))
        DemoState.chatHistory.value = currentList
        
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Call FastAPI
                val response = ApiClient.apiService.sendChat(
                    MessageRequest(user_id = "demo_msme_user", message = text)
                )
                
                // Add AI reply
                val updatedList = DemoState.chatHistory.value.toMutableList()
                updatedList.add(ChatMessage(
                    isUser = false, 
                    text = response.reply,
                    action = response.actions.firstOrNull()
                ))
                DemoState.chatHistory.value = updatedList
            } catch (e: Exception) {
                val errorList = DemoState.chatHistory.value.toMutableList()
                errorList.add(ChatMessage(isUser = false, text = "Error: Could not reach AI Agent. Please check your backend connection."))
                DemoState.chatHistory.value = errorList
            } finally {
                _isLoading.value = false
            }
        }
    }
}
