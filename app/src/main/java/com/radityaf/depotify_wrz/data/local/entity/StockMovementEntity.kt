package com.radityaf.depotify_wrz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val productName: String = "",
    val stockType: String, // "fill", "empty"
    val movementType: String, // "in", "out", "adjustment"
    val qty: Int,
    val endingBalance: Int = 0,
    val description: String? = null,
    val createdAt: String? = null
)
