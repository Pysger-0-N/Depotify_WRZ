package com.radityaf.depotify_wrz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
@Entity(tableName = "pending_deletes")
data class PendingDeleteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @SerialName("entity_type")
    val entityType: String, // "category", "product", "vendor"
    @SerialName("server_id")
    val serverId: Int
)
