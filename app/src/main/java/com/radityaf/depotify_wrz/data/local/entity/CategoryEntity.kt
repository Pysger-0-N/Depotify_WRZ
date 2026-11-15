package com.radityaf.depotify_wrz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @SerialName("vendor_id")
    val vendorId: Int? = null,
    val code: String,
    val name: String,
    val description: String? = null,
    @SerialName("is_synced")
    val isSynced: Boolean = true,
    @SerialName("is_deleted")
    val isDeleted: Boolean = false
)
