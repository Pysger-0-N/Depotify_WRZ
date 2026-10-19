package com.radityaf.depotify_wrz.data.repository

import com.radityaf.depotify_wrz.data.local.AppDatabase
import com.radityaf.depotify_wrz.data.remote.ApiService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val apiService: ApiService,
    private val database: AppDatabase
) {
    suspend fun syncRoomToServer(): Result<Boolean> {
        return try {
            val products = database.productDao().getAllProductsSync()
            val categories = database.categoryDao().getAllCategoriesSync()
            val vendors = database.vendorDao().getAllVendorsSync()
            val stockMovements = database.stockMovementDao().getAllMovementsSync()

            val syncPayload = mapOf(
                "products" to products,
                "categories" to categories,
                "vendors" to vendors,
                "stock_movements" to stockMovements
            )

            val jsonString = Json.encodeToString(syncPayload)
            val response = apiService.syncRoomToServer(jsonString)

            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Gagal sinkronisasi data ke server database"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
