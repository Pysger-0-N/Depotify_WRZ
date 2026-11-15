package com.radityaf.depotify_wrz.data.local.dao

import androidx.room.*
import com.radityaf.depotify_wrz.data.local.entity.PendingDeleteEntity

@Dao
interface PendingDeleteDao {
    @Query("SELECT * FROM pending_deletes")
    suspend fun getAllPendingDeletes(): List<PendingDeleteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingDelete(pendingDelete: PendingDeleteEntity)

    @Delete
    suspend fun deletePendingDelete(pendingDelete: PendingDeleteEntity)

    @Query("DELETE FROM pending_deletes WHERE id = :id")
    suspend fun deleteById(id: Int)
}
