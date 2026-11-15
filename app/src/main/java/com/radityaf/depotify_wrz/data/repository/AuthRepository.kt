package com.radityaf.depotify_wrz.data.repository

import com.radityaf.depotify_wrz.data.local.TokenManager
import com.radityaf.depotify_wrz.data.model.LoginRequest
import com.radityaf.depotify_wrz.data.model.LoginResponse
import com.radityaf.depotify_wrz.data.model.User
import com.radityaf.depotify_wrz.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val json: Json
) {
    val tokenFlow: Flow<String?> = tokenManager.tokenFlow

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val loginData = body.data
                if (body.success && loginData != null && loginData.user != null) {
                    val userWithVendorId = loginData.user.let { u ->
                        val vId = u.effectiveVendorId
                        if (u.vendor_id == null && vId != null) u.copy(vendor_id = vId) else u
                    }
                    tokenManager.saveToken(loginData.token)
                    Result.success(LoginResponse(token = loginData.token, user = userWithVendorId))
                } else {
                    Result.failure(Exception(body.message.ifBlank { "Email atau kata sandi salah." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Email atau kata sandi salah (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server backend database. Pastikan server running (${e.message})"))
        }
    }

    suspend fun getMe(): Result<User> {
        return try {
            val response = apiService.getMe()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    val rawUser = body.data
                    val vId = rawUser.effectiveVendorId
                    val userWithVendorId = if (rawUser.vendor_id == null && vId != null) rawUser.copy(vendor_id = vId) else rawUser
                    Result.success(userWithVendorId)
                } else {
                    Result.failure(Exception(body.message.ifBlank { "Sesi telah berakhir" }))
                }
            } else {
                Result.failure(Exception("Sesi telah berakhir atau tidak valid (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try {
            apiService.logout()
        } catch (_: Exception) {}
        tokenManager.clearToken()
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val element = json.parseToJsonElement(errorBody).jsonObject
            element["message"]?.jsonPrimitive?.content
                ?: element["error"]?.jsonPrimitive?.content
        } catch (_: Exception) {
            null
        }
    }
}
