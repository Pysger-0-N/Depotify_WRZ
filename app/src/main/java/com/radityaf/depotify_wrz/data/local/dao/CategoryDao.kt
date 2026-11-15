package com.radityaf.depotify_wrz.data.local.dao

import androidx.room.*
import com.radityaf.depotify_wrz.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isDeleted = 0 ORDER BY id DESC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isDeleted = 0")
    suspend fun getAllCategoriesSync(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Int)
}
