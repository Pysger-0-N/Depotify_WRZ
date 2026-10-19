package com.radityaf.depotify_wrz.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Meta(
    val current_page: Int = 1,
    val per_page: Int = 15,
    val total: Int = 0,
    val last_page: Int = 1
)

@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val message: String = "",
    val error_code: String = "",
    val errors: Map<String, List<String>> = emptyMap()
)

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String, // "owner", "staf", "vendor", "customer"
    val vendor_id: Int? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val device_name: String? = "Android App"
)

@Serializable
data class LoginResponse(
    val token: String,
    val user: User
)

@Serializable
data class LogoutResponse(
    val message: String
)

@Serializable
data class Category(
    val id: Int,
    val code: String,
    val name: String,
    val description: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class CreateCategoryRequest(
    val code: String,
    val name: String,
    val description: String? = null
)

@Serializable
data class Product(
    val id: Int,
    val category_id: Int,
    val name: String,
    val sku: String? = null,
    val variant: String? = null,
    val unit: String, // "canister", "gallon", "pcs", "other"
    val selling_price: Double = 0.0,
    val deposit_price: Double? = null,
    val purchase_price: Double = 0.0,
    val stock_content: Int = 0,
    val empty_stock: Int = 0,
    val minimum_stock: Int = 0,
    val is_active: Boolean = true,
    val created_at: String? = null,
    val updated_at: String? = null,
    val category: Category? = null
)

@Serializable
data class CreateProductRequest(
    val category_id: Int,
    val name: String,
    val sku: String? = null,
    val variant: String? = null,
    val unit: String,
    val selling_price: Double = 0.0,
    val deposit_price: Double? = null,
    val purchase_price: Double = 0.0,
    val stock_content: Int = 0,
    val empty_stock: Int = 0,
    val minimum_stock: Int = 0,
    val is_active: Boolean = true
)

@Serializable
data class Vendor(
    val id: Int,
    val name: String,
    val contact_person: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val is_active: Boolean = true,
    val categories: List<Category> = emptyList()
)

@Serializable
data class CreateVendorRequest(
    val name: String,
    val contact_person: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val category_ids: List<Int> = emptyList(),
    val is_active: Boolean = true
)

@Serializable
data class PurchaseOrderItem(
    val id: Int = 0,
    val purchase_order_id: Int = 0,
    val product_id: Int,
    val qty_ordered: Int,
    val unit_price: Double,
    val subtotal: Double,
    val product: Product? = null
)

@Serializable
data class PurchaseOrder(
    val id: Int,
    val po_number: String,
    val vendor_id: Int,
    val po_date: String,
    val expected_delivery_date: String? = null,
    val status: String, // draft, ordered, confirmed, shipped, received, rejected, cancelled
    val subtotal: Double = 0.0,
    val tax_amount: Double = 0.0,
    val total_amount: Double = 0.0,
    val notes: String? = null,
    val vendor: Vendor? = null,
    val items: List<PurchaseOrderItem> = emptyList(),
    val goods_receipts: List<GoodsReceipt> = emptyList()
)

@Serializable
data class CreatePoItemRequest(
    val product_id: Int,
    val qty_ordered: Int,
    val unit_price: Double
)

@Serializable
data class CreatePoRequest(
    val vendor_id: Int,
    val po_date: String,
    val expected_delivery_date: String? = null,
    val tax_amount: Double = 0.0,
    val notes: String? = null,
    val items: List<CreatePoItemRequest>
)

@Serializable
data class PoStatusUpdateRequest(
    val status: String
)

@Serializable
data class GoodsReceiptItem(
    val id: Int = 0,
    val goods_receipt_id: Int = 0,
    val purchase_order_item_id: Int? = null,
    val product_id: Int,
    val qty_diterima: Int,
    val qty_ditolak: Int = 0,
    val catatan_qc: String? = null,
    val product: Product? = null
)

@Serializable
data class GoodsReceipt(
    val id: Int,
    val receipt_number: String,
    val purchase_order_id: Int,
    val document_type: String, // "do", "invoice", "do_and_invoice"
    val document_number: String,
    val received_date: String,
    val notes: String? = null,
    val items: List<GoodsReceiptItem> = emptyList()
)

@Serializable
data class CreateGoodsReceiptItemRequest(
    val product_id: Int,
    val purchase_order_item_id: Int? = null,
    val qty_diterima: Int,
    val qty_ditolak: Int = 0,
    val catatan_qc: String? = null
)

@Serializable
data class CreateGoodsReceiptRequest(
    val purchase_order_id: Int,
    val document_type: String,
    val document_number: String,
    val received_date: String,
    val notes: String? = null,
    val items: List<CreateGoodsReceiptItemRequest>
)

@Serializable
data class StockMovement(
    val id: Int,
    val product_id: Int,
    val stock_type: String, // "fill" or "empty"
    val movement_type: String, // "in", "out", "adjustment"
    val qty: Int,
    val ending_balance: Int,
    val reference_type: String? = null,
    val reference_id: Int? = null,
    val description: String? = null,
    val created_by: Int,
    val created_at: String? = null,
    val product: Product? = null
)

@Serializable
data class StockTransactionRequest(
    val product_id: Int,
    val stock_type: String, // "fill" or "empty"
    val movement_type: String? = null, // "in", "out", "adjustment"
    val qty: Int,
    val description: String? = null
)

@Serializable
data class StockNotification(
    val id: Int,
    val product_id: Int,
    val message: String,
    val is_read: Boolean = false,
    val created_at: String? = null,
    val product: Product? = null
)

@Serializable
data class SaleItem(
    val id: Int = 0,
    val product_id: Int,
    val product_name: String,
    val qty: Int,
    val unit_price: Double,
    val subtotal: Double
)

@Serializable
data class SaleTransaction(
    val id: Int,
    val invoice_number: String,
    val customer_name: String,
    val customer_phone: String? = null,
    val customer_address: String? = null,
    val status: String, // "pending", "confirmed", "selesai"
    val total_amount: Double,
    val payment_method: String? = null,
    val items: List<SaleItem> = emptyList(),
    val created_at: String? = null
)

// Standard Wrappers
@Serializable
data class ProductApiResponse(val success: Boolean, val message: String, val data: Product)
@Serializable
data class ProductListApiResponse(val success: Boolean, val message: String, val data: List<Product>, val meta: Meta? = null)
@Serializable
data class CategoryApiResponse(val success: Boolean, val message: String, val data: Category)
@Serializable
data class CategoryListApiResponse(val success: Boolean, val message: String, val data: List<Category>, val meta: Meta? = null)
@Serializable
data class VendorApiResponse(val success: Boolean, val message: String, val data: Vendor)
@Serializable
data class VendorListApiResponse(val success: Boolean, val message: String, val data: List<Vendor>, val meta: Meta? = null)
@Serializable
data class PurchaseOrderApiResponse(val success: Boolean, val message: String, val data: PurchaseOrder)
@Serializable
data class PurchaseOrderListApiResponse(val success: Boolean, val message: String, val data: List<PurchaseOrder>, val meta: Meta? = null)
@Serializable
data class GoodsReceiptApiResponse(val success: Boolean, val message: String, val data: GoodsReceipt)
@Serializable
data class GoodsReceiptListApiResponse(val success: Boolean, val message: String, val data: List<GoodsReceipt>, val meta: Meta? = null)
@Serializable
data class StockMovementApiResponse(val success: Boolean, val message: String, val data: StockMovement)
@Serializable
data class StockMovementListApiResponse(val success: Boolean, val message: String, val data: List<StockMovement>, val meta: Meta? = null)
@Serializable
data class NotificationListApiResponse(val success: Boolean, val message: String, val data: List<StockNotification>, val meta: Meta? = null)
@Serializable
data class SaleListApiResponse(val success: Boolean, val message: String, val data: List<SaleTransaction>, val meta: Meta? = null)
@Serializable
data class SaleApiResponse(val success: Boolean, val message: String, val data: SaleTransaction)
@Serializable
data class SimpleApiResponse(val success: Boolean, val message: String)
