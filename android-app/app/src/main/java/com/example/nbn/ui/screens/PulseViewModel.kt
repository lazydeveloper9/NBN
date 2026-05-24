package com.example.nbn.ui.screens

import androidx.lifecycle.ViewModel
import com.example.nbn.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class PulseViewModel : ViewModel() {

    private val _agentLogs = MutableStateFlow<List<String>>(
        listOf(
            "[08:00 AM] Comms Agent: Replied to 12 WhatsApp queries overnight. 3 resulted in sales.",
            "[09:15 AM] Ops Agent: Noticed 'Clay Pots' are running low (Only 5 left). Drafted a PO for your default supplier.",
            "[11:30 AM] DevOps Agent: High traffic detected from Instagram. Suggesting a 5% discount banner on the homepage."
        )
    )
    val agentLogs: StateFlow<List<String>> = _agentLogs.asStateFlow()

    private var webSocket: WebSocket? = null

    init {
        connectWebSocket()
    }

    private fun connectWebSocket() {
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                addLog("Connected to Agent StateFlow.")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                addLog(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                addLog("Disconnected: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                addLog("Connection failed: ${t.message}")
            }
        }
        
        webSocket = ApiClient.createWebSocket("demo_msme_user", listener)
    }

    private fun addLog(message: String) {
        val currentList = _agentLogs.value.toMutableList()
        currentList.add(message)
        _agentLogs.value = currentList
    }

    override fun onCleared() {
        super.onCleared()
        webSocket?.close(1000, "ViewModel cleared")
    }
}
