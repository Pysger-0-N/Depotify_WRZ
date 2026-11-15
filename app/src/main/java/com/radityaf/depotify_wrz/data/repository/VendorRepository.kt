package com.radityaf.depotify_wrz.data.repository

import com.radityaf.depotify_wrz.data.model.*
import com.radityaf.depotify_wrz.data.remote.ApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getVendors(): Result<List<Vendor>> {
        return try {
            val response = apiService.getVendors()
            if (response.isSuccessful) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVendorDetail(id: Int): Result<Vendor> {
        return try {
            val response = apiService.getVendorDetail(id)
            if (response.isSuccessful) {
                val vendor = response.body()?.effectiveVendor ?: Vendor(id = id)
                Result.success(vendor)
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createVendor(request: CreateVendorRequest): Result<Vendor> {
        return try {
            val response = apiService.createVendor(request)
            if (response.isSuccessful) {
                val vendor = response.body()?.effectiveVendor ?: Vendor(
                    name = request.name,
                    phone = request.phone,
                    email = request.email ?: request.account_email,
                    address = request.address
                )
                Result.success(vendor)
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVendor(id: Int, request: CreateVendorRequest): Result<Vendor> {
        return try {
            val response = apiService.updateVendor(id, request)
            if (response.isSuccessful) {
                val vendor = response.body()?.effectiveVendor ?: Vendor(
                    id = id,
                    name = request.name,
                    phone = request.phone,
                    email = request.email ?: request.account_email,
                    address = request.address
                )
                Result.success(vendor)
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteVendor(id: Int): Result<Boolean> {
        return try {
            val response = apiService.deleteVendor(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun <T> parseErrorMessage(response: Response<T>): String {
        return try {
            val errorStr = response.errorBody()?.string()
            if (!errorStr.isNullOrBlank()) {
                if (errorStr.contains("\"errors\":")) {
                    val arrayRegex = "\"([a-zA-Z0-9_-]+)\"\\s*:\\s*\\[\\s*\"([^\"]+)\"\\s*]".toRegex()
                    val matches = arrayRegex.findAll(errorStr).map { it.groupValues[2] }.toList()
                    if (matches.isNotEmpty()) {
                        return matches.joinToString("\n")
                    }
                }
                if (errorStr.contains("\"message\":")) {
                    val regex = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                    val match = regex.find(errorStr)
                    if (match != null) return match.groupValues[1]
                }
                errorStr
            } else {
                "Gagal terhubung ke server database (HTTP ${response.code()})"
            }
        } catch (_: Exception) {
            "Gagal terhubung ke server database (HTTP ${response.code()})"
        }
    }
}
