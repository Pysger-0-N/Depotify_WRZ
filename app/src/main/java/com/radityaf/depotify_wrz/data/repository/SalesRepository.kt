package com.radityaf.depotify_wrz.data.repository

import com.radityaf.depotify_wrz.data.model.CreateSaleRequest
import com.radityaf.depotify_wrz.data.model.SaleTransaction
import com.radityaf.depotify_wrz.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getSales(status: String? = null): Result<List<SaleTransaction>> {
        return try {
            val response = apiService.getSales(status)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.items)
            } else {
                Result.failure(Exception("Gagal memuat data penjualan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSale(request: CreateSaleRequest): Result<SaleTransaction> {
        return try {
            val response = apiService.createSale(request)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                Result.success(data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.body()?.message ?: "Gagal membuat transaksi kasir"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmSale(id: Int): Result<SaleTransaction> {
        return try {
            val response = apiService.confirmSale(id)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                Result.success(data)
            } else {
                Result.failure(Exception("Gagal mengkonfirmasi penjualan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeSale(id: Int): Result<SaleTransaction> {
        return try {
            val response = apiService.completeSale(id)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                Result.success(data)
            } else {
                Result.failure(Exception("Gagal menyelesaikan penjualan ke database"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
