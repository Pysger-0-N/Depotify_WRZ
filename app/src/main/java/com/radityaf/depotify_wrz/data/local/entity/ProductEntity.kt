package com.radityaf.depotify_wrz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val sku: String? = null,
    val categoryId: Int = 1,
    val categoryName: String = "",
    val variant: String? = null,
    val unit: String = "canister", // "canister", "gallon", "pcs", "other"
    val sellingPrice: Double = 0.0,
    val depositPrice: Double? = null,
    val purchasePrice: Double = 0.0,
    val minimumStock: Int = 5,
    val isActive: Boolean = true,
    val stock: Int = 0,
    val barcode: String = ""
) {
    val price: Long get() = sellingPrice.toLong()
    val category: String get() = categoryName
}
