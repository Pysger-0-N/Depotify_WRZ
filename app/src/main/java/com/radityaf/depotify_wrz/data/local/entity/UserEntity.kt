package com.radityaf.depotify_wrz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.radityaf.depotify_wrz.data.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val phone: String? = null,
    val isActive: Boolean = true,
    val vendorId: Int? = null,
    val token: String? = null,
    val lastLoginTimestamp: Long = System.currentTimeMillis()
) {
    fun toDomainUser(): User {
        return User(
            id = id,
            name = name,
            email = email,
            role = role,
            phone = phone,
            is_active = isActive,
            vendor_id = vendorId
        )
    }
}

fun User.toEntity(token: String? = null): UserEntity {
    val effVendorId = effectiveVendorId
    return UserEntity(
        id = id,
        name = name,
        email = email,
        role = role,
        phone = phone,
        isActive = is_active,
        vendorId = effVendorId,
        token = token,
        lastLoginTimestamp = System.currentTimeMillis()
    )
}
