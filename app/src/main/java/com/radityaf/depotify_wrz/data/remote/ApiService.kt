package com.radityaf.depotify_wrz.data.remote

import com.radityaf.depotify_wrz.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginApiResponse>

    @GET("v1/auth/me")
    suspend fun getMe(): Response<UserApiResponse>

    @POST("v1/auth/logout")
    suspend fun logout(): Response<SimpleApiResponse>

    // Products
    @GET("v1/products")
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("stok_menipis") stokMenipis: Boolean? = null,
        @Query("low_stock") lowStock: Int? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int? = null
    ): Response<ProductListApiResponse>

    @GET("v1/products/{id}")
    suspend fun getProductDetail(@Path("id") id: Int): Response<ProductApiResponse>

    @POST("v1/products")
    suspend fun createProduct(@Body request: CreateProductRequest): Response<ProductApiResponse>

    @PUT("v1/products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body request: CreateProductRequest): Response<ProductApiResponse>

    @DELETE("v1/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<SimpleApiResponse>

    @GET("v1/barcode/{code}")
    suspend fun getProductByBarcode(@Path("code") code: String): Response<ProductApiResponse>

    // Categories
    @GET("v1/categories")
    suspend fun getCategories(): Response<CategoryListApiResponse>

    @POST("v1/categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): Response<CategoryApiResponse>

    @PUT("v1/categories/{id}")
    suspend fun updateCategory(@Path("id") id: Int, @Body request: CreateCategoryRequest): Response<CategoryApiResponse>

    @DELETE("v1/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Int): Response<SimpleApiResponse>

    // Vendors / Suppliers
    @GET("v1/vendors")
    suspend fun getVendors(@Query("page") page: Int? = null): Response<VendorListApiResponse>

    @GET("v1/vendors/{id}")
    suspend fun getVendorDetail(@Path("id") id: Int): Response<VendorApiResponse>

    @POST("v1/vendors")
    suspend fun createVendor(@Body request: CreateVendorRequest): Response<VendorApiResponse>

    @PUT("v1/vendors/{id}")
    suspend fun updateVendor(@Path("id") id: Int, @Body request: CreateVendorRequest): Response<VendorApiResponse>

    @DELETE("v1/vendors/{id}")
    suspend fun deleteVendor(@Path("id") id: Int): Response<SimpleApiResponse>

    // Purchase Orders
    @GET("v1/purchase-orders")
    suspend fun getPurchaseOrders(
        @Query("status") status: String? = null,
        @Query("page") page: Int? = null
    ): Response<PurchaseOrderListApiResponse>

    @GET("v1/purchase-orders/{id}")
    suspend fun getPurchaseOrderDetail(@Path("id") id: Int): Response<PurchaseOrderApiResponse>

    @POST("v1/purchase-orders")
    suspend fun createPurchaseOrder(@Body request: CreatePoRequest): Response<PurchaseOrderApiResponse>

    @PUT("v1/purchase-orders/{id}")
    suspend fun updatePurchaseOrder(@Path("id") id: Int, @Body request: CreatePoRequest): Response<PurchaseOrderApiResponse>

    @PUT("v1/purchase-orders/{id}")
    suspend fun updatePoStatus(@Path("id") id: Int, @Body request: PoStatusUpdateRequest): Response<okhttp3.ResponseBody>

    @DELETE("v1/purchase-orders/{id}")
    suspend fun deletePurchaseOrder(@Path("id") id: Int): Response<SimpleApiResponse>

    // Goods Receipts
    @POST("v1/goods-receipts")
    suspend fun createGoodsReceipt(@Body request: CreateGoodsReceiptRequest): Response<GoodsReceiptApiResponse>

    @GET("v1/goods-receipts")
    suspend fun getGoodsReceipts(
        @Query("purchase_order_id") purchaseOrderId: Int? = null,
        @Query("page") page: Int? = null
    ): Response<GoodsReceiptListApiResponse>

    @GET("v1/goods-receipts/{id}")
    suspend fun getGoodsReceiptDetail(@Path("id") id: Int): Response<GoodsReceiptApiResponse>

    // Stock Transactions & Movements
    @POST("v1/stock-transactions/masuk")
    suspend fun stockMasuk(@Body request: StockTransactionRequest): Response<StockMovementApiResponse>

    @POST("v1/stock-transactions/keluar")
    suspend fun stockKeluar(@Body request: StockTransactionRequest): Response<StockMovementApiResponse>

    @POST("v1/stock-transactions/penyesuaian")
    suspend fun stockPenyesuaian(@Body request: StockTransactionRequest): Response<StockMovementApiResponse>

    @GET("v1/stock-movements")
    suspend fun getStockMovements(
        @Query("product_id") productId: Int? = null,
        @Query("jenis_stok") jenisStok: String? = null,
        @Query("tipe") tipe: String? = null,
        @Query("page") page: Int? = null
    ): Response<StockMovementListApiResponse>

    // Stock Notifications
    @GET("v1/stock-notifications")
    suspend fun getStockNotifications(
        @Query("is_read") isRead: Int? = null
    ): Response<NotificationListApiResponse>

    @PATCH("v1/stock-notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: Int): Response<SimpleApiResponse>

    @DELETE("v1/stock-notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: Int): Response<SimpleApiResponse>

    // Sales Transactions (Penjualan)
    @GET("v1/sales")
    suspend fun getSales(
        @Query("status") status: String? = null,
        @Query("page") page: Int? = null
    ): Response<SaleListApiResponse>

    @POST("v1/sales")
    suspend fun createSale(@Body request: CreateSaleRequest): Response<SaleApiResponse>

    @PUT("v1/sales/{id}")
    suspend fun updateSale(@Path("id") id: Int, @Body request: SaleStatusUpdateRequest): Response<SaleApiResponse>

    // Sync Room to Server Database
    @POST("v1/sync/room-to-server")
    suspend fun syncRoomToServer(@Body payload: String): Response<SimpleApiResponse>
}
