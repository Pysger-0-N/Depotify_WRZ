package com.radityaf.depotify_wrz.data.repository

import com.radityaf.depotify_wrz.data.local.TokenManager
import com.radityaf.depotify_wrz.data.local.dao.UserDao
import com.radityaf.depotify_wrz.data.local.entity.toEntity
import com.radityaf.depotify_wrz.data.model.LoginRequest
import com.radityaf.depotify_wrz.data.model.LoginResponse
import com.radityaf.depotify_wrz.data.model.User
import com.radityaf.depotify_wrz.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val userDao: UserDao,
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
                    userDao.insertUser(userWithVendorId.toEntity(loginData.token))
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
            // Fallback Login Offline dari Room
            val localUser = userDao.getUserByEmail(email)
            val currentToken = tokenManager.tokenFlow.firstOrNull() ?: localUser?.token
            if (localUser != null) {
                if (!currentToken.isNullOrEmpty()) {
                    tokenManager.saveToken(currentToken)
                }
                Result.success(LoginResponse(token = currentToken ?: "offline_token", user = localUser.toDomainUser()))
            } else {
                Result.failure(Exception("Server tidak dapat dijangkau dan akun belum pernah tersimpan di perangkat lokal (${e.message})"))
            }
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
                    val currentToken = tokenManager.tokenFlow.firstOrNull()
                    userDao.insertUser(userWithVendorId.toEntity(currentToken))
                    Result.success(userWithVendorId)
                } else {
                    Result.failure(Exception(body.message.ifBlank { "Sesi telah berakhir" }))
                }
            } else {
                Result.failure(Exception("Sesi telah berakhir atau tidak valid (${response.code()})"))
            }
        } catch (e: Exception) {
            // Fallback Offline dari Room jika terputus dari jaringan
            val cachedUser = userDao.getLoggedInUser()
            if (cachedUser != null) {
                Result.success(cachedUser.toDomainUser())
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun logout() {
        try {
            apiService.logout()
        } catch (_: Exception) {}
        userDao.clearUser()
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
