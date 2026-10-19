package com.radityaf.depotify_wrz.data.repository

import com.radityaf.depotify_wrz.data.model.*
import com.radityaf.depotify_wrz.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseOrderRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getPurchaseOrders(status: String? = null, page: Int? = null): Result<PurchaseOrderListApiResponse> {
        return try {
            val response = apiService.getPurchaseOrders(status, page)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal memuat Purchase Orders (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPurchaseOrderDetail(id: Int): Result<PurchaseOrder> {
        return try {
            val response = apiService.getPurchaseOrderDetail(id)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Detail PO tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPurchaseOrder(request: CreatePoRequest): Result<PurchaseOrder> {
        return try {
            val response = apiService.createPurchaseOrder(request)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal membuat PO"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePurchaseOrder(id: Int, request: CreatePoRequest): Result<PurchaseOrder> {
        return try {
            val response = apiService.updatePurchaseOrder(id, request)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal memperbarui PO"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePoStatus(id: Int, status: String): Result<PurchaseOrder> {
        return try {
            val response = apiService.updatePoStatus(id, PoStatusUpdateRequest(status))
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal mengubah status PO"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePurchaseOrder(id: Int): Result<Boolean> {
        return try {
            val response = apiService.deletePurchaseOrder(id)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Gagal menghapus PO"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createGoodsReceipt(request: CreateGoodsReceiptRequest): Result<GoodsReceipt> {
        return try {
            val response = apiService.createGoodsReceipt(request)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal menerima barang"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGoodsReceipts(purchaseOrderId: Int? = null): Result<List<GoodsReceipt>> {
        return try {
            val response = apiService.getGoodsReceipts(purchaseOrderId)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Gagal memuat penerimaan barang"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
