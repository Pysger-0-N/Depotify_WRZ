package com.radityaf.depotify_wrz.data.local.dao

import androidx.room.*
import com.radityaf.depotify_wrz.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY id DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE vendorId = :vendorId AND isDeleted = 0 ORDER BY id DESC")
    fun getProductsByVendor(vendorId: Int): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isDeleted = 0")
    suspend fun getAllProductsSync(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedProducts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Int)
}