package com.radityaf.depotify_wrz.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.radityaf.depotify_wrz.data.local.dao.*
import com.radityaf.depotify_wrz.data.local.entity.*

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        VendorEntity::class,
        StockMovementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun vendorDao(): VendorDao
    abstract fun stockMovementDao(): StockMovementDao
}
