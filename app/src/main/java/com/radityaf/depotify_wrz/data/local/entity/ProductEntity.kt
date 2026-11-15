package com.radityaf.depotify_wrz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @SerialName("vendor_id")
    val vendorId: Int? = null,
    val name: String,
    val sku: String? = null,
    @SerialName("category_id")
    val categoryId: Int = 1,
    val categoryName: String = "",
    val variant: String? = null,
    val unit: String = "canister", // "canister", "gallon", "pcs", "other"
    @SerialName("selling_price")
    val sellingPrice: Double = 0.0,
    @SerialName("deposit_price")
    val depositPrice: Double? = null,
    @SerialName("purchase_price")
    val purchasePrice: Double = 0.0,
    @SerialName("stock_content")
    val stockContent: Int = 0,
    @SerialName("empty_stock")
    val emptyStock: Int = 0,
    @SerialName("minimum_stock")
    val minimumStock: Int = 5,
    @SerialName("is_active")
    val isActive: Boolean = true,
    val barcode: String = "",
    @SerialName("is_synced")
    val isSynced: Boolean = true,
    @SerialName("is_deleted")
    val isDeleted: Boolean = false
) {
    val price: Long get() = sellingPrice.toLong()
    val category: String get() = categoryName
    val stock: Int get() = stockContent
}
