package com.radityaf.depotify_wrz.data.repository

import com.radityaf.depotify_wrz.data.local.dao.ProductDao
import com.radityaf.depotify_wrz.data.local.dao.StockMovementDao
import com.radityaf.depotify_wrz.data.local.entity.StockMovementEntity
import com.radityaf.depotify_wrz.data.model.StockMovement
import com.radityaf.depotify_wrz.data.model.StockTransactionRequest
import com.radityaf.depotify_wrz.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepository @Inject constructor(
    private val apiService: ApiService,
    private val stockMovementDao: StockMovementDao,
    private val productDao: ProductDao
) {
    suspend fun stockMasuk(productId: Int, stockType: String, qty: Int, description: String?): Result<StockMovement> {
        return try {
            val response = apiService.stockMasuk(StockTransactionRequest(productId, stockType, "in", qty, description))
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                cacheMovement(data)
                Result.success(data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.body()?.message ?: "Gagal memproses stok masuk ke server"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun stockKeluar(productId: Int, stockType: String, qty: Int, description: String?): Result<StockMovement> {
        return try {
            val response = apiService.stockKeluar(StockTransactionRequest(productId, stockType, "out", qty, description))
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                cacheMovement(data)
                Result.success(data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.body()?.message ?: "Stok tidak mencukupi atau gagal memproses di server"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun stockPenyesuaian(productId: Int, stockType: String, qty: Int, description: String?): Result<StockMovement> {
        return try {
            val response = apiService.stockPenyesuaian(StockTransactionRequest(productId, stockType, "adjustment", qty, description))
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                cacheMovement(data)
                Result.success(data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.body()?.message ?: "Gagal menyesuaikan stok di server"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStockMovements(productId: Int? = null): Result<List<StockMovement>> {
        return try {
            val response = apiService.getStockMovements(productId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    body.data.forEach { cacheMovement(it) }
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message))
                }
            } else {
                Result.failure(Exception("Gagal memuat histori pergerakan stok dari server"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun cacheMovement(m: StockMovement) {
        val prodName = m.product?.name ?: run {
            val localProd = productDao.getProductById(m.product_id)
            localProd?.name ?: "Produk #${m.product_id}"
        }
        val entity = StockMovementEntity(
            id = m.id,
            productId = m.product_id,
            productName = prodName,
            stockType = m.stock_type,
            movementType = m.movement_type,
            qty = m.qty,
            endingBalance = m.ending_balance,
            referenceType = m.reference_type ?: "manual",
            referenceId = m.reference_id,
            description = m.description,
            createdBy = m.created_by,
            createdAt = m.created_at,
            isSynced = true
        )
        stockMovementDao.insertStockMovement(entity)

        // Sync product ending balance if available
        val localProduct = productDao.getProductById(m.product_id)
        if (localProduct != null) {
            if (m.stock_type.equals("fill", ignoreCase = true)) {
                productDao.updateProduct(localProduct.copy(stockContent = m.ending_balance))
            } else {
                productDao.updateProduct(localProduct.copy(emptyStock = m.ending_balance))
            }
        }
    }
}
