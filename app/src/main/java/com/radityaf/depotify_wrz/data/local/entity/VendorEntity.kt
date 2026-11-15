package com.radityaf.depotify_wrz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
@Entity(tableName = "vendor")
data class VendorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @SerialName("user_id")
    val userId: Int? = null,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("is_synced")
    val isSynced: Boolean = true,
    @SerialName("is_deleted")
    val isDeleted: Boolean = false
)
