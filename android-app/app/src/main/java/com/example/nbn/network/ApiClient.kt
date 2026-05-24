package com.example.nbn.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Models matching FastAPI
@Serializable
data class InventoryItem(
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    val price: Double,
    val quantity: Int,
    val image_url: String? = null
)

@Serializable
data class InventoryAddRequest(
    val name: String,
    val quantity: Int,
    val price: Double,
    val description: String,
    val image_url: String = ""
)

@Serializable
data class MessageRequest(
    val user_id: String,
    val message: String
)

@Serializable
data class ActionData(
    val type: String,
    val message: String,
    val url: String? = null,
    val item: String? = null
)

@Serializable
data class ChatResponse(
    val reply: String,
    val actions: List<ActionData>
)

interface ApiService {
    @POST("orchestrator/chat")
    suspend fun sendChat(@Body request: MessageRequest): ChatResponse

    @GET("inventory")
    suspend fun getInventory(): List<InventoryItem>

    @POST("inventory")
    suspend fun addInventory(@Body request: InventoryAddRequest): InventoryItem
}

object ApiClient {
    // Using localhost since we successfully set up ADB Reverse
    private const val BASE_URL = "http://localhost:8000/"
    private const val WS_URL = "ws://localhost:8000/ws/"

    private val json = Json { ignoreUnknownKeys = true }

    val okHttpClient = OkHttpClient.Builder().build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }

    fun createWebSocket(clientId: String, listener: WebSocketListener): WebSocket {
        val request = Request.Builder().url("$WS_URL$clientId").build()
        return okHttpClient.newWebSocket(request, listener)
    }
}
