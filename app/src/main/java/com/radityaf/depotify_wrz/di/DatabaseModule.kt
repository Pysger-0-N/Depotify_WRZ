package com.radityaf.depotify_wrz.di

import android.content.Context
import androidx.room.Room
import com.radityaf.depotify_wrz.data.local.AppDatabase
import com.radityaf.depotify_wrz.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()

    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    @Singleton
    fun provideVendorDao(database: AppDatabase): VendorDao = database.vendorDao()

    @Provides
    @Singleton
    fun provideStockMovementDao(database: AppDatabase): StockMovementDao = database.stockMovementDao()

    @Provides
    @Singleton
    fun providePendingDeleteDao(database: AppDatabase): PendingDeleteDao = database.pendingDeleteDao()

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()
}
