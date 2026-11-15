package com.radityaf.depotify_wrz.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

object DoubleAsStringSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DoubleAsString", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Double) {
        encoder.encodeDouble(value)
    }
    override fun deserialize(decoder: Decoder): Double {
        val input = decoder as? JsonDecoder ?: return try { decoder.decodeDouble() } catch(_: Exception) { 0.0 }
        val element = input.decodeJsonElement()
        return when (element) {
            is JsonPrimitive -> {
                element.doubleOrNull ?: element.content.toDoubleOrNull() ?: 0.0
            }
            else -> 0.0
        }
    }
}

object NullableDoubleAsStringSerializer : KSerializer<Double?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NullableDoubleAsString", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Double?) {
        if (value != null) encoder.encodeDouble(value) else encoder.encodeNull()
    }
    override fun deserialize(decoder: Decoder): Double? {
        val input = decoder as? JsonDecoder ?: return try { decoder.decodeDouble() } catch(_: Exception) { null }
        val element = input.decodeJsonElement()
        return when (element) {
            is JsonPrimitive -> {
                if (element.content.equals("null", true) || element.content.isBlank()) null
                else element.doubleOrNull ?: element.content.toDoubleOrNull()
            }
            else -> null
        }
    }
}

@OptIn(InternalSerializationApi::class)
@Serializable
data class Meta(
    val current_page: Int = 1,
    val per_page: Int = 15,
    val total: Int = 0,
    val last_page: Int = 1
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val message: String = "",
    val error_code: String = "",
    val errors: Map<String, List<String>> = emptyMap()
)

@Serializable
data class User(
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val role: String = "owner", // "owner", "staf", "vendor", "customer"
    val phone: String? = null,
    val is_active: Boolean = true,
    val vendor_id: Int? = null,
    val vendor: Vendor? = null
) {
    val effectiveVendorId: Int? get() = vendor_id ?: vendor?.id
}

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val device_name: String? = "Android App"
)

@Serializable
data class LoginData(
    val token: String = "",
    val user: User? = null,
    val token_type: String? = null,
    val expires_at: String? = null,
    val expires_in_seconds: Long? = null
)

@Serializable
data class LoginApiResponse(
    val success: Boolean = true,
    val message: String = "",
    val data: LoginData? = null
)

@Serializable
data class UserApiResponse(
    val success: Boolean = true,
    val message: String = "",
    val data: User? = null
)

@Serializable
data class LoginResponse(
    val token: String = "",
    val user: User? = null
)

@Serializable
data class LogoutResponse(
    val message: String
)

@Serializable
data class Category(
    val id: Int = 0,
    val vendor_id: Int? = null,
    val code: String = "",
    val name: String = "",
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
    val id: Int = 0,
    val vendor_id: Int? = null,
    val source_product_id: Int? = null,
    val category_id: Int = 0,
    val name: String = "",
    val sku: String? = null,
    val variant: String? = null,
    val unit: String = "gallon", // "canister", "gallon", "pcs", "other"
    @Serializable(with = DoubleAsStringSerializer::class)
    val selling_price: Double = 0.0,
    @Serializable(with = NullableDoubleAsStringSerializer::class)
    val deposit_price: Double? = null,
    @Serializable(with = DoubleAsStringSerializer::class)
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
    @Serializable(with = DoubleAsStringSerializer::class)
    val selling_price: Double = 0.0,
    @Serializable(with = NullableDoubleAsStringSerializer::class)
    val deposit_price: Double? = null,
    @Serializable(with = DoubleAsStringSerializer::class)
    val purchase_price: Double = 0.0,
    val stock_content: Int = 0,
    val empty_stock: Int = 0,
    val minimum_stock: Int = 0,
    val is_active: Boolean = true,
    val vendor_id: Int? = null
)

@Serializable
data class Vendor(
    val id: Int = 0,
    val user_id: Int? = null,
    val name: String = "",
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
    val account_email: String? = null,
    val address: String? = null,
    val category_ids: List<Int> = emptyList(),
    val is_active: Boolean = true,
    val password: String? = null,
    val account_password: String? = null,
    val password_confirmation: String? = null,
    val account_password_confirmation: String? = null
)

@Serializable
data class PurchaseOrderItem(
    val id: Int = 0,
    val purchase_order_id: Int = 0,
    val product_id: Int = 0,
    val product_name: String? = null,
    val qty_ordered: Int = 0,
    @Serializable(with = DoubleAsStringSerializer::class)
    val unit_price: Double = 0.0,
    @Serializable(with = DoubleAsStringSerializer::class)
    val agreed_price: Double = 0.0,
    @Serializable(with = DoubleAsStringSerializer::class)
    val subtotal: Double = 0.0,
    @Serializable(with = DoubleAsStringSerializer::class)
    val total_price: Double = 0.0,
    val product: Product? = null
) {
    val displayUnitPrice: Double get() = if (unit_price > 0) unit_price else agreed_price
}

@Serializable
data class PurchaseOrder(
    val id: Int = 0,
    val po_number: String? = "",
    val vendor_id: Int? = 0,
    val po_date: String? = "",
    val payment_method: String? = "cash",
    val expected_delivery_date: String? = null,
    val shipping_address: String? = null,
    val shipping_method: String? = null,
    val status: String? = "pending", // draft, ordered, confirmed, shipped, received, rejected, cancelled
    @Serializable(with = DoubleAsStringSerializer::class)
    val subtotal: Double = 0.0,
    @Serializable(with = DoubleAsStringSerializer::class)
    val subtotal_price: Double = 0.0,
    @Serializable(with = DoubleAsStringSerializer::class)
    val tax_amount: Double = 0.0,
    @Serializable(with = DoubleAsStringSerializer::class)
    val tax: Double = 0.0,
    @Serializable(with = DoubleAsStringSerializer::class)
    val total_amount: Double = 0.0,
    @Serializable(with = DoubleAsStringSerializer::class)
    val total_price: Double = 0.0,
    val notes: String? = null,
    val vendor: Vendor? = null,
    val items: List<PurchaseOrderItem> = emptyList(),
    val goods_receipts: List<GoodsReceipt> = emptyList()
) {
    val displayTotal: Double get() = if (total_amount > 0) total_amount else total_price
    val displayStatus: String get() = status ?: "pending"
    val displayPoNumber: String get() = po_number ?: "PO-$id"
}

@Serializable
data class CreatePoItemRequest(
    val product_id: Int,
    val qty_ordered: Int,
    @Serializable(with = DoubleAsStringSerializer::class)
    val unit_price: Double
)

@Serializable
data class CreatePoRequest(
    val vendor_id: Int,
    val po_date: String,
    val payment_method: String = "cash",
    val expected_delivery_date: String? = null,
    val shipping_address: String? = null,
    val shipping_method: String? = null,
    @Serializable(with = DoubleAsStringSerializer::class)
    val tax_amount: Double = 0.0,
    val notes: String? = null,
    val items: List<CreatePoItemRequest>
)

@Serializable
data class PoStatusUpdateRequest(
    val status: String,
    val notes: String? = null
)

@Serializable
data class GoodsReceiptItem(
    val id: Int = 0,
    val goods_receipt_id: Int = 0,
    val purchase_order_item_id: Int? = null,
    val product_id: Int = 0,
    val qty_diterima: Int = 0,
    val qty_ditolak: Int = 0,
    val catatan_qc: String? = null,
    val product: Product? = null
)

@Serializable
data class GoodsReceipt(
    val id: Int = 0,
    val receipt_number: String = "",
    val purchase_order_id: Int = 0,
    val document_type: String = "do", // "do", "invoice", "do_and_invoice"
    val document_number: String? = null,
    val received_date: String? = null,
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
    val id: Int = 0,
    val product_id: Int = 0,
    val stock_type: String = "fill", // "fill" or "empty"
    val movement_type: String = "in", // "in", "out", "adjustment"
    val qty: Int = 0,
    val ending_balance: Int = 0,
    val reference_type: String? = null,
    val reference_id: Int? = null,
    val description: String? = null,
    val created_by: Int = 0,
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
    val id: Int = 0,
    val product_id: Int = 0,
    val message: String = "",
    val is_read: Boolean = false,
    val created_at: String? = null,
    val product: Product? = null
)

@Serializable
data class CustomerData(
    val id: Int = 0,
    val name: String = "",
    val phone: String? = null,
    val address: String? = null
)

@Serializable
data class SaleItem(
    val id: Int = 0,
    val product_id: Int = 0,
    val product_name: String = "",
    val transaction_type: String? = null, // "top_up", "new_deposit", "buy_disconnect"
    val qty: Int = 0,
    @Serializable(with = DoubleAsStringSerializer::class)
    val unit_price: Double = 0.0,
    @Serializable(with = DoubleAsStringSerializer::class)
    val subtotal: Double = 0.0,
    val empty_container_accepted: Int = 0
)

@Serializable
data class CreateSaleItemRequest(
    val product_id: Int,
    val transaction_type: String = "top_up", // "top_up", "new_deposit", "buy_disconnect"
    val qty: Int,
    val empty_container_accepted: Int = 0
)

@Serializable
data class CreateSaleRequest(
    val customer_name: String? = null,
    val customer_phone: String? = null,
    val customer_address: String? = null,
    val customer_id: Int? = null,
    val channel: String = "walk_in", // "walk_in", "whatsapp", "app"
    val payment_method: String = "cash", // "cash", "transfer", "other"
    val items: List<CreateSaleItemRequest>
)

@Serializable
data class SaleTransaction(
    val id: Int = 0,
    val transaction_code: String = "",
    val invoice_number: String = "",
    val customer_name: String = "",
    val customer_phone: String? = null,
    val customer_address: String? = null,
    val customer: CustomerData? = null,
    val status: String = "pending", // "pending", "confirmed", "selesai"
    @Serializable(with = DoubleAsStringSerializer::class)
    val total_amount: Double = 0.0,
    val payment_method: String? = null,
    val items: List<SaleItem> = emptyList(),
    val created_at: String? = null
) {
    val displayInvoice: String get() = invoice_number.ifBlank { transaction_code.ifBlank { "TRX-$id" } }
    val displayCustomerName: String get() = customer_name.ifBlank { customer?.name ?: "Customer" }
    val displayCustomerPhone: String? get() = customer_phone ?: customer?.phone
    val displayCustomerAddress: String? get() = customer_address ?: customer?.address
}

@Serializable
data class PaginatedData<T>(
    val current_page: Int = 1,
    val data: List<T> = emptyList(),
    val total: Int = 0,
    val last_page: Int = 1
)

@Serializable
data class SyncPayload(
    val products: List<com.radityaf.depotify_wrz.data.local.entity.ProductEntity> = emptyList(),
    val categories: List<com.radityaf.depotify_wrz.data.local.entity.CategoryEntity> = emptyList(),
    val vendors: List<com.radityaf.depotify_wrz.data.local.entity.VendorEntity> = emptyList(),
    val stock_movements: List<com.radityaf.depotify_wrz.data.local.entity.StockMovementEntity> = emptyList()
)

// Standard Wrappers
@Serializable
data class ProductApiResponse(val success: Boolean = true, val message: String = "", val data: Product? = null)

@Serializable
data class ProductListApiResponse(
    val success: Boolean = true,
    val message: String = "",
    val data: List<Product> = emptyList(),
    val current_page: Int = 1,
    val total: Int = 0,
    val last_page: Int = 1,
    val meta: Meta? = null
)

@Serializable
data class CategoryApiResponse(
    val success: Boolean = true,
    val message: String = "",
    val data: Category? = null,
    val category: Category? = null
) {
    val effectiveCategory: Category? get() = data ?: category
}

@Serializable
data class CategoryListApiResponse(
    val success: Boolean = true,
    val message: String = "",
    val data: List<Category> = emptyList(),
    val current_page: Int = 1,
    val total: Int = 0,
    val last_page: Int = 1,
    val meta: Meta? = null
)

@Serializable
data class VendorApiResponse(
    val success: Boolean = true,
    val message: String = "",
    val data: Vendor? = null,
    val vendor: Vendor? = null
) {
    val effectiveVendor: Vendor? get() = data ?: vendor
}

@Serializable
data class VendorListApiResponse(
    val success: Boolean = true,
    val message: String = "",
    val data: List<Vendor> = emptyList(),
    val current_page: Int = 1,
    val total: Int = 0,
    val last_page: Int = 1,
    val meta: Meta? = null
)

@Serializable
data class PurchaseOrderApiResponse(val success: Boolean = true, val message: String = "", val data: PurchaseOrder? = null)

@Serializable
data class PurchaseOrderListApiResponse(
    val success: Boolean = true,
    val message: String = "",
    val data: List<PurchaseOrder> = emptyList(),
    val current_page: Int = 1,
    val total: Int = 0,
    val last_page: Int = 1,
    val meta: Meta? = null
)

@Serializable
data class GoodsReceiptApiResponse(val success: Boolean = true, val message: String = "", val data: GoodsReceipt? = null)

@Serializable
data class GoodsReceiptListApiResponse(
    val success: Boolean = true,
    val message: String = "",
    val data: List<GoodsReceipt> = emptyList(),
    val current_page: Int = 1,
    val total: Int = 0,
    val last_page: Int = 1,
    val meta: Meta? = null
)

@Serializable
data class StockMovementApiResponse(val success: Boolean = true, val message: String = "", val data: StockMovement? = null)

@Serializable
data class StockMovementListApiResponse(
    val success: Boolean = true,
    val message: String = "",
    val data: List<StockMovement> = emptyList(),
    val current_page: Int = 1,
    val total: Int = 0,
    val last_page: Int = 1,
    val meta: Meta? = null
)

@Serializable
data class SimpleApiResponse(val success: Boolean = true, val message: String = "")

@Serializable
data class NotificationListApiResponse(
    val success: Boolean = true,
    val message: String = "",
    val data: List<StockNotification> = emptyList(),
    val current_page: Int = 1,
    val total: Int = 0,
    val last_page: Int = 1,
    val meta: Meta? = null
)

@Serializable
data class SaleListApiResponse(
    val success: Boolean = true,
    val message: String = "",
    val data: PaginatedData<SaleTransaction>? = null
) {
    val items: List<SaleTransaction> get() = data?.data ?: emptyList()
}

@Serializable
data class SaleApiResponse(val success: Boolean = true, val message: String = "", val data: SaleTransaction? = null)
