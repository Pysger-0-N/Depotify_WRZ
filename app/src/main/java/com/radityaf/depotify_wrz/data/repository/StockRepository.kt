package com.radityaf.depotify_wrz.data.repository

import com.radityaf.depotify_wrz.data.model.StockMovement
import com.radityaf.depotify_wrz.data.model.StockTransactionRequest
import com.radityaf.depotify_wrz.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun stockMasuk(productId: Int, stockType: String, qty: Int, description: String?): Result<StockMovement> {
        return try {
            val response = apiService.stockMasuk(StockTransactionRequest(productId, stockType, "in", qty, description))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) Result.success(body.data)
                else Result.failure(Exception(body.message))
            } else {
                Result.failure(Exception("Gagal memproses stok masuk"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun stockKeluar(productId: Int, stockType: String, qty: Int, description: String?): Result<StockMovement> {
        return try {
            val response = apiService.stockKeluar(StockTransactionRequest(productId, stockType, "out", qty, description))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) Result.success(body.data)
                else Result.failure(Exception(body.message))
            } else {
                Result.failure(Exception("Stok tidak mencukupi atau gagal memproses"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun stockPenyesuaian(productId: Int, stockType: String, qty: Int, description: String?): Result<StockMovement> {
        return try {
            val response = apiService.stockPenyesuaian(StockTransactionRequest(productId, stockType, "adjustment", qty, description))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) Result.success(body.data)
                else Result.failure(Exception(body.message))
            } else {
                Result.failure(Exception("Gagal menyesuaikan stok"))
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
                if (body.success) Result.success(body.data)
                else Result.failure(Exception(body.message))
            } else {
                Result.failure(Exception("Gagal memuat histori pergerakan stok"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
