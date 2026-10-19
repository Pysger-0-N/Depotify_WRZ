package com.radityaf.depotify_wrz.data.repository

import com.radityaf.depotify_wrz.data.model.*
import com.radityaf.depotify_wrz.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getVendors(): Result<List<Vendor>> {
        return try {
            val response = apiService.getVendors()
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Gagal memuat daftar vendor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVendorDetail(id: Int): Result<Vendor> {
        return try {
            val response = apiService.getVendorDetail(id)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Vendor tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createVendor(request: CreateVendorRequest): Result<Vendor> {
        return try {
            val response = apiService.createVendor(request)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal membuat vendor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVendor(id: Int, request: CreateVendorRequest): Result<Vendor> {
        return try {
            val response = apiService.updateVendor(id, request)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal mengupdate vendor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteVendor(id: Int): Result<Boolean> {
        return try {
            val response = apiService.deleteVendor(id)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Gagal menghapus vendor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
