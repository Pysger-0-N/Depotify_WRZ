package com.radityaf.depotify_wrz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @SerialName("product_id")
    val productId: Int,
    val productName: String = "",
    @SerialName("stock_type")
    val stockType: String, // "fill", "empty"
    @SerialName("movement_type")
    val movementType: String, // "in", "out", "adjustment"
    val qty: Int,
    @SerialName("ending_balance")
    val endingBalance: Int = 0,
    @SerialName("reference_type")
    val referenceType: String = "manual",
    @SerialName("reference_id")
    val referenceId: Int? = null,
    val description: String? = null,
    @SerialName("created_by")
    val createdBy: Int = 1,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("is_synced")
    val isSynced: Boolean = true
)
