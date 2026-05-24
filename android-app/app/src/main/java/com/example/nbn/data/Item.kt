package com.example.nbn.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String?,
    val price: Double,
    val quantity: Int,
    val imageUrl: String?,
    val showOnWebsite: Boolean,
    val availableOnWhatsapp: Boolean,
    val isSynced: Boolean = false // Flag to track if synced with remote backend
)
