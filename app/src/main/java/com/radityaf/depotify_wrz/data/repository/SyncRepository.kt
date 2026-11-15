package com.radityaf.depotify_wrz.data.repository

import com.radityaf.depotify_wrz.data.local.AppDatabase
import com.radityaf.depotify_wrz.data.local.entity.CategoryEntity
import com.radityaf.depotify_wrz.data.local.entity.ProductEntity
import com.radityaf.depotify_wrz.data.local.entity.VendorEntity
import com.radityaf.depotify_wrz.data.model.CreateCategoryRequest
import com.radityaf.depotify_wrz.data.model.CreateProductRequest
import com.radityaf.depotify_wrz.data.model.CreateVendorRequest
import com.radityaf.depotify_wrz.data.model.StockTransactionRequest
import com.radityaf.depotify_wrz.data.remote.ApiService
import com.radityaf.depotify_wrz.utils.NetworkObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val apiService: ApiService,
    private val database: AppDatabase,
    private val networkObserver: NetworkObserver
) {
    val isOnlineFlow: Flow<Boolean> = networkObserver.isOnlineFlow

    fun isConnected(): Boolean = networkObserver.isCurrentlyConnected()

    fun startAutoSyncListener(scope: CoroutineScope) {
        networkObserver.isOnlineFlow
            .onEach { online ->
                if (online) {
                    syncRoomToServer()
                    syncServerToRoom()
                }
            }
            .launchIn(scope)
    }

    suspend fun syncFullTwoWay(): Result<Boolean> {
        if (!networkObserver.isCurrentlyConnected()) {
            return Result.failure(Exception("Perangkat offline. Tidak dapat sinkronisasi dengan server database."))
        }
        val pushResult = syncRoomToServer()
        if (pushResult.isFailure) {
            return pushResult
        }
        return syncServerToRoom()
    }

    suspend fun syncRoomToServer(): Result<Boolean> {
        if (!networkObserver.isCurrentlyConnected()) {
            return Result.failure(Exception("Perangkat offline. Data tersimpan di Room DB lokal."))
        }
        return try {
            // 0. Process Pending Deletes from offline delete actions
            val pendingDeletes = database.pendingDeleteDao().getAllPendingDeletes()
            pendingDeletes.forEach { pd ->
                try {
                    val res = when (pd.entityType.lowercase()) {
                        "category" -> apiService.deleteCategory(pd.serverId)
                        "vendor" -> apiService.deleteVendor(pd.serverId)
                        "product" -> apiService.deleteProduct(pd.serverId)
                        else -> null
                    }
                    if (res == null || res.isSuccessful || res.code() == 404) {
                        database.pendingDeleteDao().deletePendingDelete(pd)
                    }
                } catch (_: Exception) {}
            }

            // Fetch server lookup maps to map codes/SKUs accurately
            val serverCategoriesRes = try { apiService.getCategories() } catch (_: Exception) { null }
            val serverCategories = serverCategoriesRes?.body()?.data ?: emptyList()
            val categoryCodeMap = serverCategories.associateBy({ it.code }, { it.id })

            val serverVendorsRes = try { apiService.getVendors() } catch (_: Exception) { null }
            val serverVendors = serverVendorsRes?.body()?.data ?: emptyList()
            val vendorNameMap = serverVendors.associateBy({ it.name.lowercase() }, { it.id })

            val serverProductsRes = try { apiService.getProducts() } catch (_: Exception) { null }
            val serverProducts = serverProductsRes?.body()?.data ?: emptyList()
            val productSkuMap = serverProducts.associateBy({ it.sku ?: "" }, { it.id })

            // 1. Sync unsynced categories
            val unsyncedCategories = database.categoryDao().getUnsyncedCategories()
            unsyncedCategories.forEach { cat ->
                try {
                    val req = CreateCategoryRequest(
                        code = cat.code,
                        name = cat.name,
                        description = cat.description
                    )
                    val existingServerId = categoryCodeMap[cat.code]
                    if (existingServerId != null && existingServerId > 0) {
                        val res = apiService.updateCategory(existingServerId, req)
                        if (res.isSuccessful) {
                            if (cat.id != existingServerId) {
                                database.categoryDao().deleteCategory(cat)
                            }
                            database.categoryDao().insertCategory(
                                cat.copy(id = existingServerId, isSynced = true)
                            )
                        }
                    } else if (cat.id > 0) {
                        val res = apiService.updateCategory(cat.id, req)
                        if (res.isSuccessful) {
                            database.categoryDao().insertCategory(cat.copy(isSynced = true))
                        }
                    } else {
                        val res = apiService.createCategory(req)
                        if (res.isSuccessful) {
                            res.body()?.effectiveCategory?.let { newCat ->
                                database.categoryDao().deleteCategory(cat)
                                database.categoryDao().insertCategory(
                                    cat.copy(id = newCat.id, isSynced = true)
                                )
                            }
                        }
                    }
                } catch (_: Exception) {}
            }

            // 2. Sync unsynced vendors
            val unsyncedVendors = database.vendorDao().getUnsyncedVendors()
            unsyncedVendors.forEach { ven ->
                try {
                    val vendorEmail = ven.email?.ifBlank { null }
                        ?: "${ven.name.lowercase().replace("\\s+".toRegex(), "")}@depotify.com"
                    val req = CreateVendorRequest(
                        name = ven.name,
                        contact_person = ven.name,
                        phone = ven.phone,
                        email = vendorEmail,
                        account_email = vendorEmail,
                        address = ven.address
                    )
                    val existingVendorId = vendorNameMap[ven.name.lowercase()]
                    if (existingVendorId != null && existingVendorId > 0) {
                        val res = apiService.updateVendor(existingVendorId, req)
                        if (res.isSuccessful) {
                            if (ven.id != existingVendorId) {
                                database.vendorDao().deleteVendor(ven)
                            }
                            database.vendorDao().insertVendor(
                                ven.copy(id = existingVendorId, isSynced = true)
                            )
                        }
                    } else if (ven.id > 0) {
                        val res = apiService.updateVendor(ven.id, req)
                        if (res.isSuccessful) {
                            database.vendorDao().insertVendor(ven.copy(isSynced = true))
                        }
                    } else {
                        val res = apiService.createVendor(req)
                        if (res.isSuccessful) {
                            res.body()?.effectiveVendor?.let { newVen ->
                                database.vendorDao().deleteVendor(ven)
                                database.vendorDao().insertVendor(
                                    ven.copy(id = newVen.id, isSynced = true)
                                )
                            }
                        }
                    }
                } catch (_: Exception) {}
            }

            // 3. Sync unsynced products
            val unsyncedProducts = database.productDao().getUnsyncedProducts()
            unsyncedProducts.forEach { prod ->
                try {
                    val req = CreateProductRequest(
                        category_id = prod.categoryId,
                        name = prod.name,
                        sku = prod.sku,
                        variant = prod.variant,
                        unit = prod.unit,
                        selling_price = prod.sellingPrice,
                        deposit_price = prod.depositPrice,
                        purchase_price = prod.purchasePrice,
                        minimum_stock = prod.minimumStock,
                        is_active = prod.isActive
                    )
                    val existingProductId = if (!prod.sku.isNullOrBlank()) productSkuMap[prod.sku] else null
                    if (existingProductId != null && existingProductId > 0) {
                        val res = apiService.updateProduct(existingProductId, req)
                        if (res.isSuccessful) {
                            if (prod.id != existingProductId) {
                                database.productDao().deleteProduct(prod)
                            }
                            database.productDao().insertProduct(
                                prod.copy(id = existingProductId, isSynced = true)
                            )
                        }
                    } else if (prod.id > 0) {
                        val res = apiService.updateProduct(prod.id, req)
                        if (res.isSuccessful) {
                            database.productDao().insertProduct(prod.copy(isSynced = true))
                        }
                    } else {
                        val res = apiService.createProduct(req)
                        if (res.isSuccessful) {
                            res.body()?.data?.let { newProd ->
                                database.productDao().deleteProduct(prod)
                                database.productDao().insertProduct(
                                    prod.copy(id = newProd.id, isSynced = true)
                                )
                            }
                        }
                    }
                } catch (_: Exception) {}
            }

            // 4. Sync unsynced stock movements
            val unsyncedMovements = database.stockMovementDao().getUnsyncedMovements()
            unsyncedMovements.forEach { m ->
                try {
                    val req = StockTransactionRequest(
                        product_id = m.productId,
                        stock_type = m.stockType,
                        movement_type = m.movementType,
                        qty = m.qty,
                        description = m.description
                    )
                    val res = when (m.movementType.lowercase()) {
                        "in", "masuk" -> apiService.stockMasuk(req)
                        "out", "keluar" -> apiService.stockKeluar(req)
                        "adjustment", "penyesuaian" -> apiService.stockPenyesuaian(req)
                        else -> apiService.stockMasuk(req)
                    }
                    if (res.isSuccessful) {
                        database.stockMovementDao().insertStockMovement(m.copy(isSynced = true))
                    }
                } catch (_: Exception) {}
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncServerToRoom(): Result<Boolean> {
        if (!networkObserver.isCurrentlyConnected()) {
            return Result.failure(Exception("Perangkat offline. Tidak dapat menarik data dari server."))
        }
        return try {
            // 1. Fetch & Sync Categories
            val catResponse = apiService.getCategories()
            if (catResponse.isSuccessful && catResponse.body() != null) {
                catResponse.body()!!.data.forEach { cat ->
                    database.categoryDao().insertCategory(
                        CategoryEntity(
                            id = cat.id,
                            vendorId = cat.vendor_id,
                            code = cat.code,
                            name = cat.name,
                            description = cat.description,
                            isSynced = true,
                            isDeleted = false
                        )
                    )
                }
            }

            // 2. Fetch & Sync Vendors
            val venResponse = apiService.getVendors()
            if (venResponse.isSuccessful && venResponse.body() != null) {
                venResponse.body()!!.data.forEach { ven ->
                    database.vendorDao().insertVendor(
                        VendorEntity(
                            id = ven.id,
                            userId = ven.user_id,
                            name = ven.name,
                            phone = ven.phone,
                            email = ven.email,
                            address = ven.address,
                            isActive = ven.is_active,
                            isSynced = true,
                            isDeleted = false
                        )
                    )
                }
            }

            // 3. Fetch & Sync Products
            val prodResponse = apiService.getProducts()
            if (prodResponse.isSuccessful && prodResponse.body() != null) {
                prodResponse.body()!!.data.forEach { prod ->
                    database.productDao().insertProduct(
                        ProductEntity(
                            id = prod.id,
                            vendorId = prod.vendor_id,
                            name = prod.name,
                            sku = prod.sku ?: "PRD${prod.id}",
                            categoryId = prod.category_id,
                            categoryName = prod.category?.name ?: "Umum",
                            variant = prod.variant,
                            unit = prod.unit,
                            sellingPrice = prod.selling_price,
                            depositPrice = prod.deposit_price,
                            purchasePrice = prod.purchase_price,
                            minimumStock = prod.minimum_stock,
                            isActive = prod.is_active,
                            stockContent = prod.stock_content,
                            emptyStock = prod.empty_stock,
                            isSynced = true,
                            isDeleted = false
                        )
                    )
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
