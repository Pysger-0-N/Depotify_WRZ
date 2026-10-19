package com.radityaf.depotify_wrz.data.repository

import com.radityaf.depotify_wrz.data.model.StockNotification
import com.radityaf.depotify_wrz.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getNotifications(isRead: Int? = null): Result<List<StockNotification>> {
        return try {
            val response = apiService.getStockNotifications(isRead)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Gagal memuat notifikasi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(id: Int): Result<Boolean> {
        return try {
            val response = apiService.markNotificationRead(id)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Gagal menandai notifikasi dibaca"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNotification(id: Int): Result<Boolean> {
        return try {
            val response = apiService.deleteNotification(id)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Gagal menghapus notifikasi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
