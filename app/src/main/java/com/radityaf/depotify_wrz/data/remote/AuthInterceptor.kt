package com.radityaf.depotify_wrz.data.remote

import com.radityaf.depotify_wrz.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath
        val isLoginRequest = path.contains("/auth/login")

        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")

        if (!isLoginRequest) {
            val token = runBlocking {
                tokenManager.tokenFlow.first()
            }
            if (!token.isNullOrEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401 && !isLoginRequest) {
            runBlocking {
                tokenManager.clearToken()
            }
        }

        return response
    }
}
