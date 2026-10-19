package com.radityaf.depotify_wrz.data.repository

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
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Gagal memuat data penjualan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmSale(id: Int): Result<SaleTransaction> {
        return try {
            val response = apiService.confirmSale(id)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
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
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Gagal menyelesaikan penjualan ke database"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
