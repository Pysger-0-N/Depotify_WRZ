package com.radityaf.depotify_wrz.data.local.dao

import androidx.room.*
import com.radityaf.depotify_wrz.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements ORDER BY id DESC")
    fun getAllStockMovements(): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements")
    suspend fun getAllMovementsSync(): List<StockMovementEntity>

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY id DESC")
    fun getStockMovementsByProduct(productId: Int): Flow<List<StockMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovement(movement: StockMovementEntity)

    @Delete
    suspend fun deleteStockMovement(movement: StockMovementEntity)
}
