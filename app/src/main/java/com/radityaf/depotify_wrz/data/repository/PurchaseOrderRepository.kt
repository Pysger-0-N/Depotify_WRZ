package com.radityaf.depotify_wrz.data.repository

import com.radityaf.depotify_wrz.data.model.*
import com.radityaf.depotify_wrz.data.remote.ApiService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseOrderRepository @Inject constructor(
    private val apiService: ApiService
) {
    private fun handleNetworkException(e: Exception): Exception {
        return when (e) {
            is ConnectException -> Exception("Gagal terhubung ke IP 192.168.100.179:8000. Pastikan 'php artisan serve --host=0.0.0.0 --port=8000' berjalan di laptop & Firewall tidak memblokir Port 8000.")
            is SocketTimeoutException -> Exception("Koneksi ke server 192.168.100.179:8000 mengalami timeout. Periksa jaringan hotspot.")
            is UnknownHostException -> Exception("IP Server 192.168.100.179 tidak ditemukan pada jaringan saat ini.")
            else -> e
        }
    }

    suspend fun getPurchaseOrders(status: String? = null, page: Int? = null): Result<PurchaseOrderListApiResponse> {
        return try {
            val response = apiService.getPurchaseOrders(status, page)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errStr = response.errorBody()?.string()
                Result.failure(Exception(if (!errStr.isNullOrBlank()) errStr else "Gagal memuat Purchase Orders (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }

    suspend fun getPurchaseOrderDetail(id: Int): Result<PurchaseOrder> {
        return try {
            val response = apiService.getPurchaseOrderDetail(id)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                Result.success(data)
            } else {
                val errStr = response.errorBody()?.string()
                Result.failure(Exception(if (!errStr.isNullOrBlank()) errStr else "Detail PO tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }

    suspend fun createPurchaseOrder(request: CreatePoRequest): Result<PurchaseOrder> {
        return try {
            val response = apiService.createPurchaseOrder(request)
            if (response.isSuccessful) {
                val data = response.body()?.data ?: PurchaseOrder(
                    vendor_id = request.vendor_id,
                    po_date = request.po_date,
                    payment_method = request.payment_method,
                    notes = request.notes
                )
                Result.success(data)
            } else {
                val errStr = response.errorBody()?.string()
                val errorMsg = parseErrorMessage(errStr, response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }

    suspend fun updatePurchaseOrder(id: Int, request: CreatePoRequest): Result<PurchaseOrder> {
        return try {
            val response = apiService.updatePurchaseOrder(id, request)
            if (response.isSuccessful) {
                val data = response.body()?.data ?: PurchaseOrder(
                    id = id,
                    vendor_id = request.vendor_id,
                    po_date = request.po_date,
                    payment_method = request.payment_method,
                    notes = request.notes
                )
                Result.success(data)
            } else {
                val errStr = response.errorBody()?.string()
                val errorMsg = parseErrorMessage(errStr, response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }

    private val jsonSerializer = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun parsePurchaseOrderJson(jsonStr: String): PurchaseOrder? {
        if (jsonStr.isBlank()) return null
        return try {
            val wrapper = jsonSerializer.decodeFromString<PurchaseOrderApiResponse>(jsonStr)
            if (wrapper.data != null && wrapper.data.id > 0) {
                return wrapper.data
            }
            val direct = jsonSerializer.decodeFromString<PurchaseOrder>(jsonStr)
            if (direct.id > 0) {
                return direct
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun parseErrorMessage(errorJson: String?, httpCode: Int): String {
        if (errorJson.isNullOrBlank()) return "Gagal memproses PO (HTTP $httpCode)"
        return try {
            val jsonObj = jsonSerializer.parseToJsonElement(errorJson).jsonObject
            val msg = jsonObj["message"]?.jsonPrimitive?.content
            val errors = jsonObj["errors"]?.jsonObject
            val firstErrorMsg = errors?.values?.firstOrNull()?.jsonArray?.firstOrNull()?.jsonPrimitive?.content
            firstErrorMsg ?: msg ?: errorJson
        } catch (_: Exception) {
            errorJson
        }
    }

    suspend fun updatePoStatus(id: Int, status: String, notes: String? = null): Result<PurchaseOrder> {
        return try {
            val response = apiService.updatePoStatus(id, PoStatusUpdateRequest(status = status, notes = notes))
            if (response.isSuccessful) {
                val jsonStr = response.body()?.string() ?: ""
                val parsedPo = parsePurchaseOrderJson(jsonStr)
                val finalPo = parsedPo ?: PurchaseOrder(id = id, status = status)
                Result.success(finalPo)
            } else {
                val errStr = response.errorBody()?.string()
                val errorMsg = parseErrorMessage(errStr, response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }

    suspend fun deletePurchaseOrder(id: Int): Result<Boolean> {
        return try {
            val response = apiService.deletePurchaseOrder(id)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(true)
            } else {
                val errStr = response.errorBody()?.string()
                Result.failure(Exception(if (!errStr.isNullOrBlank()) errStr else "Gagal menghapus PO"))
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }

    suspend fun createGoodsReceipt(request: CreateGoodsReceiptRequest): Result<GoodsReceipt> {
        return try {
            val response = apiService.createGoodsReceipt(request)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                Result.success(data)
            } else {
                val errStr = response.errorBody()?.string()
                Result.failure(Exception(if (!errStr.isNullOrBlank()) errStr else (response.body()?.message ?: "Gagal menerima barang")))
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }

    suspend fun getGoodsReceipts(purchaseOrderId: Int? = null): Result<List<GoodsReceipt>> {
        return try {
            val response = apiService.getGoodsReceipts(purchaseOrderId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val errStr = response.errorBody()?.string()
                Result.failure(Exception(if (!errStr.isNullOrBlank()) errStr else "Gagal memuat penerimaan barang"))
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }
}
