package com.radityaf.depotify_wrz.data.local.dao

import androidx.room.*
import com.radityaf.depotify_wrz.data.local.entity.VendorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VendorDao {
    @Query("SELECT * FROM vendor ORDER BY id DESC")
    fun getAllVendors(): Flow<List<VendorEntity>>

    @Query("SELECT * FROM vendor")
    suspend fun getAllVendorsSync(): List<VendorEntity>

    @Query("SELECT * FROM vendor WHERE id = :id")
    suspend fun getVendorById(id: Int): VendorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendor(vendor: VendorEntity)

    @Update
    suspend fun updateVendor(vendor: VendorEntity)

    @Delete
    suspend fun deleteVendor(vendor: VendorEntity)

    @Query("DELETE FROM vendor WHERE id = :id")
    suspend fun deleteVendorById(id: Int)
}
