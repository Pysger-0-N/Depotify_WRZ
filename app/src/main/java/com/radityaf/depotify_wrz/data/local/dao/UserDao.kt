package com.radityaf.depotify_wrz.data.local.dao

import androidx.room.*
import com.radityaf.depotify_wrz.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users ORDER BY lastLoginTimestamp DESC LIMIT 1")
    suspend fun getLoggedInUser(): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("DELETE FROM users")
    suspend fun clearUser()
}
