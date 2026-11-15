package com.radityaf.depotify_wrz.ui.owner.po

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.local.PurchaseOrderStore
import com.radityaf.depotify_wrz.data.local.dao.ProductDao
import com.radityaf.depotify_wrz.data.local.dao.StockMovementDao
import com.radityaf.depotify_wrz.data.local.dao.VendorDao
import com.radityaf.depotify_wrz.data.local.entity.ProductEntity
import com.radityaf.depotify_wrz.data.local.entity.StockMovementEntity
import com.radityaf.depotify_wrz.data.local.entity.VendorEntity
import com.radityaf.depotify_wrz.data.model.CreatePoItemRequest
import com.radityaf.depotify_wrz.data.model.CreatePoRequest
import com.radityaf.depotify_wrz.data.repository.PurchaseOrderRepository
import com.radityaf.depotify_wrz.data.repository.StockRepository
import com.radityaf.depotify_wrz.data.repository.SyncRepository
import com.radityaf.depotify_wrz.ui.components.formatRupiah
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class PoItem(val productId: Int = 0, val productName: String, val qty: Int, val price: Long)

data class PurchaseOrder(
    val id: Int,
    val vendorName: String,
    val status: String, // "PENDING", "DIKONFIRMASI", "DITOLAK", "DIKIRIM", "DITERIMA"
    val paymentMethod: String,
    val items: List<PoItem>
) {
    val total: Long get() = items.sumOf { it.qty * it.price }
}

fun com.radityaf.depotify_wrz.data.model.PurchaseOrder.toUiModel(): PurchaseOrder {
    return PurchaseOrder(
        id = this.id,
        vendorName = this.vendor?.name ?: "Vendor #${this.vendor_id ?: 0}",
        status = when (this.displayStatus.lowercase()) {
            "draft", "ordered", "pending" -> "PENDING"
            "confirmed" -> "DIKONFIRMASI"
            "shipped" -> "DIKIRIM"
            "received" -> "DITERIMA"
            "rejected", "cancelled" -> "DITOLAK"
            else -> this.displayStatus.uppercase()
        },
        paymentMethod = this.payment_method ?: "cash",
        items = this.items.map { item ->
            PoItem(
                productId = item.product_id,
                productName = item.product?.name ?: item.product_name ?: "Produk #${item.product_id}",
                qty = item.qty_ordered,
                price = item.displayUnitPrice.toLong()
            )
        }
    )
}

data class PoUiState(
    val items: List<PurchaseOrder> = emptyList(),
    val vendors: List<VendorEntity> = emptyList(),
    val products: List<ProductEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class PurchaseOrderViewModel @Inject constructor(
    private val vendorDao: VendorDao,
    private val productDao: ProductDao,
    private val stockMovementDao: StockMovementDao,
    private val purchaseOrderStore: PurchaseOrderStore,
    private val purchaseOrderRepository: PurchaseOrderRepository,
    private val stockRepository: StockRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PoUiState())
    val uiState: StateFlow<PoUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            purchaseOrderStore.poList.collect { list ->
                _uiState.update { it.copy(items = list) }
            }
        }
        viewModelScope.launch {
            vendorDao.getAllVendors().collect { vendors ->
                _uiState.update { it.copy(vendors = vendors) }
            }
        }
        viewModelScope.launch {
            productDao.getAllProducts().collect { products ->
                _uiState.update { it.copy(products = products) }
            }
        }
        fetchServerPurchaseOrders()
    }

    fun refresh() {
        viewModelScope.launch {
            syncRepository.syncServerToRoom()
            fetchServerPurchaseOrders()
        }
    }

    fun fetchServerPurchaseOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = purchaseOrderRepository.getPurchaseOrders()
            result.fold(
                onSuccess = { apiResponse ->
                    val uiPos = apiResponse.data.map { it.toUiModel() }
                    purchaseOrderStore.setPos(uiPos)
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
            )
        }
    }

    fun getById(id: Int): PurchaseOrder? = _uiState.value.items.find { it.id == id }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun create(
        vendorId: Int,
        productId: Int,
        vendorName: String,
        productName: String,
        qty: Int,
        price: Long,
        paymentMethod: String = "cash",
        shippingAddress: String? = null,
        notes: String? = null,
        onComplete: () -> Unit = {}
    ) {
        if (_uiState.value.isSubmitting) return
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val poDateStr = dateFormat.format(Date())

                val request = CreatePoRequest(
                    vendor_id = vendorId,
                    po_date = poDateStr,
                    payment_method = paymentMethod,
                    shipping_address = shippingAddress?.takeIf { it.isNotBlank() },
                    notes = notes?.takeIf { it.isNotBlank() },
                    items = listOf(
                        CreatePoItemRequest(
                            product_id = productId,
                            qty_ordered = qty,
                            unit_price = price.toDouble()
                        )
                    )
                )

                val result = purchaseOrderRepository.createPurchaseOrder(request)
                result.fold(
                    onSuccess = { apiPo ->
                        val uiPo = apiPo.toUiModel()
                        purchaseOrderStore.addPo(uiPo)
                        fetchServerPurchaseOrders()
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                successMessage = "PO #${uiPo.id} berhasil disimpan ke database server!"
                            )
                        }
                        onComplete()
                    },
                    onFailure = { error ->
                        val errorMsg = error.message?.takeIf { it.isNotBlank() } ?: "Tidak dapat terhubung ke server database"
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                errorMessage = "Gagal menyimpan PO ke server: $errorMsg"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = e.message ?: "Gagal membuat Purchase Order"
                    )
                }
            }
        }
    }

    fun create(vendorName: String, productName: String, qty: Int, price: Long, onComplete: () -> Unit = {}) {
        val vendor = _uiState.value.vendors.find { it.name.equals(vendorName, ignoreCase = true) }
        val product = _uiState.value.products.find { it.name.equals(productName, ignoreCase = true) }
        val vendorId = vendor?.id ?: 1
        val productId = product?.id ?: 1
        create(vendorId, productId, vendorName, productName, qty, price, "cash", null, null, onComplete)
    }

    fun formatWhatsAppUrl(po: PurchaseOrder): String? {
        val vendor = _uiState.value.vendors.find { it.name.equals(po.vendorName, ignoreCase = true) }
        val rawPhone = vendor?.phone?.filter { it.isDigit() }?.takeIf { it.isNotBlank() }
        val phone = when {
            rawPhone == null -> null
            rawPhone.startsWith("0") -> "62" + rawPhone.substring(1)
            rawPhone.startsWith("62") -> rawPhone
            else -> "62" + rawPhone
        }

        val sb = StringBuilder()
        sb.append("Halo *${po.vendorName}*,\n\n")
        sb.append("Berikut rincian Purchase Order (PO) dari Depotify:\n")
        sb.append("• *No. PO:* #PO-${po.id}\n")
        sb.append("• *Status:* ${po.status}\n\n")
        sb.append("*Detail Pesanan:*\n")
        po.items.forEach { item ->
            sb.append("- ${item.productName} (${item.qty}x @ ${formatRupiah(item.price)})\n")
        }
        sb.append("\n*Total Pembelian:* ${formatRupiah(po.total)}\n\n")
        sb.append("Mohon diproses. Terima kasih!")

        val encodedText = URLEncoder.encode(sb.toString(), "UTF-8")
        return if (phone != null) {
            "https://api.whatsapp.com/send?phone=$phone&text=$encodedText"
        } else {
            "https://api.whatsapp.com/send?text=$encodedText"
        }
    }

    fun sendPoToWhatsAppAndMarkShipped(po: PurchaseOrder, context: Context) {
        val waUrl = formatWhatsAppUrl(po)
        if (waUrl != null) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
            context.startActivity(intent)

            if (po.status == "PENDING" || po.status == "DIKONFIRMASI") {
                viewModelScope.launch {
                    purchaseOrderStore.updateStatus(po.id, "DIKIRIM")
                    purchaseOrderRepository.updatePoStatus(po.id, "shipped", "Dikirim via WhatsApp")
                    fetchServerPurchaseOrders()
                }
            }
        }
    }

    fun directReceiveGoods(id: Int, note: String? = null, onComplete: () -> Unit = {}) {
        receiveGoods(id, note ?: "Bypass Admin - Konfirmasi Diterima Langsung", onComplete)
    }

    fun receiveGoods(id: Int, note: String? = null, onComplete: () -> Unit = {}) {
        val targetPo = _uiState.value.items.find { it.id == id } ?: return
        if (targetPo.status == "DITERIMA") {
            onComplete()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 1. Update local store status immediately to DITERIMA
                purchaseOrderStore.updateStatus(id, "DITERIMA")

                // 2. Update product stock and record stock movement locally (Room) first for robust offline-first behavior
                val allProducts = productDao.getAllProductsSync()
                targetPo.items.forEach { item ->
                    val product = allProducts.find { it.id == item.productId }
                        ?: allProducts.find { it.name.equals(item.productName, ignoreCase = true) }
                    if (product != null) {
                        val newStock = product.stockContent + item.qty
                        productDao.updateProduct(product.copy(stockContent = newStock))
                        stockMovementDao.insertStockMovement(
                            StockMovementEntity(
                                productId = product.id,
                                productName = product.name,
                                stockType = "fill",
                                movementType = "in",
                                qty = item.qty,
                                endingBalance = newStock,
                                referenceType = "purchase_order",
                                referenceId = targetPo.id,
                                description = "Penerimaan PO #${targetPo.id}${if (!note.isNullOrBlank()) ": $note" else ""}"
                            )
                        )
                    }
                }

                // 3. Try updating server status and syncing if online, but catch errors so local success persists
                try {
                    if (targetPo.status != "DIKIRIM" && targetPo.status != "DITERIMA") {
                        purchaseOrderRepository.updatePoStatus(id, "shipped", "Bypass Admin - Diteruskan ke pengiriman")
                    }
                    purchaseOrderRepository.updatePoStatus(id, "received", note)

                    if (syncRepository.isConnected()) {
                        targetPo.items.forEach { item ->
                            val product = allProducts.find { it.id == item.productId }
                                ?: allProducts.find { it.name.equals(item.productName, ignoreCase = true) }
                            if (product != null) {
                                stockRepository.stockMasuk(
                                    productId = product.id,
                                    stockType = "fill",
                                    qty = item.qty,
                                    description = "Penerimaan PO #${targetPo.id}${if (!note.isNullOrBlank()) ": $note" else ""}"
                                )
                            }
                        }
                        syncRepository.syncRoomToServer()
                    }
                } catch (_: Exception) {
                    // Ignore network/server errors so local store & local stock updates remain intact
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Barang PO #${targetPo.id} berhasil diterima & stok produk telah diperbarui!"
                    )
                }
                onComplete()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Gagal mengonfirmasi penerimaan barang PO"
                    )
                }
            }
        }
    }
}
