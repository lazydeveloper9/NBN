package com.example.nbn.ui.screens

import kotlinx.coroutines.flow.MutableStateFlow

object DemoState {
    val chatHistory = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(isUser = false, text = "Good morning! ☀️ Here's your daily digest:\n\n• Sales: ₹14,500 yesterday\n• Alerts: Clay Pots are low on stock\n\nHow can I help you today?"),
            ChatMessage(isUser = true, text = "Can you order 50 more clay pots from our supplier?"),
            ChatMessage(isUser = false, text = "Done. I've routed this to the Ops Agent. A Purchase Order for 50 Clay Pots has been sent to 'Rajesh Suppliers' via WhatsApp."),
            ChatMessage(isUser = true, text = "Also, launch a 10% discount campaign for Diwali."),
            ChatMessage(isUser = false, text = "I have instructed the DevOps Agent to update the website banner, and the Comms Agent is currently blasting the 10% discount code to 142 loyal customers on WhatsApp. 🚀")
        )
    )
    
    fun addSystemMessage(text: String) {
        val list = chatHistory.value.toMutableList()
        list.add(ChatMessage(isUser = false, text = text))
        chatHistory.value = list
    }
}
