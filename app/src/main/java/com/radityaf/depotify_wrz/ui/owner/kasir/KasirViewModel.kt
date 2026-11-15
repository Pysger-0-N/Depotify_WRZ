package com.radityaf.depotify_wrz.ui.owner.kasir

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.local.dao.ProductDao
import com.radityaf.depotify_wrz.data.local.dao.StockMovementDao
import com.radityaf.depotify_wrz.data.local.entity.ProductEntity
import com.radityaf.depotify_wrz.data.local.entity.StockMovementEntity
import com.radityaf.depotify_wrz.data.model.*
import com.radityaf.depotify_wrz.data.repository.ProductRepository
import com.radityaf.depotify_wrz.data.repository.SalesRepository
import com.radityaf.depotify_wrz.data.repository.StockRepository
import com.radityaf.depotify_wrz.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class PosCartItem(
    val product: Product,
    var transactionType: String = "top_up", // "top_up", "new_deposit", "buy_disconnect"
    var qty: Int = 1,
    var emptyContainerAccepted: Int = 1
) {
    val unitPrice: Double get() = product.selling_price
    val depositPrice: Double get() = if (transactionType == "new_deposit") (product.deposit_price ?: 0.0) else 0.0
    val itemSubtotal: Double get() = (unitPrice + depositPrice) * qty
}

private fun ProductEntity.toDomainModel(): Product {
    return Product(
        id = this.id,
        vendor_id = this.vendorId,
        category_id = this.categoryId,
        name = this.name,
        sku = this.sku,
        variant = this.variant,
        unit = this.unit,
        selling_price = this.sellingPrice,
        deposit_price = this.depositPrice,
        purchase_price = this.purchasePrice,
        stock_content = this.stockContent,
        empty_stock = this.emptyStock,
        minimum_stock = this.minimumStock,
        is_active = this.isActive,
        category = Category(id = this.categoryId, name = this.categoryName)
    )
}

@HiltViewModel
class KasirViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val salesRepository: SalesRepository,
    private val stockRepository: StockRepository,
    private val syncRepository: SyncRepository,
    private val productDao: ProductDao,
    private val stockMovementDao: StockMovementDao
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _cartItems = MutableStateFlow<List<PosCartItem>>(emptyList())
    val cartItems: StateFlow<List<PosCartItem>> = _cartItems.asStateFlow()

    // Customer detail (Unified, single customer name & optional phone)
    private val _customerName = MutableStateFlow("Pelanggan Umum")
    val customerName: StateFlow<String> = _customerName.asStateFlow()

    private val _customerPhone = MutableStateFlow("")
    val customerPhone: StateFlow<String> = _customerPhone.asStateFlow()

    private val _customerAddress = MutableStateFlow("")
    val customerAddress: StateFlow<String> = _customerAddress.asStateFlow()

    // Payment detail - standardized enum: "cash", "transfer", "other"
    private val _paymentMethod = MutableStateFlow("cash")
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _channel = MutableStateFlow("walk_in") // "walk_in", "whatsapp", "app"
    val channel: StateFlow<String> = _channel.asStateFlow()

    private val _cashReceivedText = MutableStateFlow("")
    val cashReceivedText: StateFlow<String> = _cashReceivedText.asStateFlow()

    // Loading & Feedback
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _completedTransaction = MutableStateFlow<SaleTransaction?>(null)
    val completedTransaction: StateFlow<SaleTransaction?> = _completedTransaction.asStateFlow()

    // Reactive calculated fields for Jetpack Compose UI
    val subtotalProducts: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.unitPrice * it.qty }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val subtotalDeposits: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.depositPrice * it.qty }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val totalAmount: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { (it.unitPrice + it.depositPrice) * it.qty }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val cashReceived: StateFlow<Double> = _cashReceivedText.map { text ->
        text.toDoubleOrNull() ?: 0.0
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val changeAmount: StateFlow<Double> = combine(_cashReceivedText, _cartItems) { text, items ->
        val rec = text.toDoubleOrNull() ?: 0.0
        val tot = items.sumOf { (it.unitPrice + it.depositPrice) * it.qty }
        (rec - tot).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val isCashEnough: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()

    private var rawEntities: List<ProductEntity> = emptyList()

    init {
        loadCategories()
        observeProducts()
    }

    fun loadCategories() {
        viewModelScope.launch {
            productRepository.getCategories().onSuccess {
                _categories.value = it
            }
        }
    }

    private fun observeProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            productDao.getAllProducts().collect { entities ->
                rawEntities = entities
                applyFilter()
            }
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            try {
                productRepository.getProducts().onSuccess { apiProducts ->
                    apiProducts.forEach { prod ->
                        productDao.insertProduct(
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
                                emptyStock = prod.empty_stock
                            )
                        )
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun setCategory(category: Category?) {
        _selectedCategory.value = category
        applyFilter()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilter()
    }

    private fun applyFilter() {
        val domainList = rawEntities.map { it.toDomainModel() }
        // Deduplicate products to prevent duplicate cards when hybrid sync creates entries
        val uniqueList = domainList.distinctBy { prod ->
            if (prod.id > 0) {
                "id_${prod.id}"
            } else {
                val cleanSku = prod.sku?.trim()?.takeIf { it.isNotBlank() }
                if (cleanSku != null) "sku_$cleanSku"
                else "v_${prod.vendor_id}_c_${prod.category_id}_name_${prod.name.trim().lowercase()}_var_${prod.variant?.trim()?.lowercase() ?: ""}"
            }
        }
        val activeList = uniqueList.filter { it.is_active }
        val selectedCat = _selectedCategory.value
        val catFiltered = if (selectedCat != null) {
            activeList.filter { p ->
                p.category_id == selectedCat.id ||
                p.category?.id == selectedCat.id ||
                p.category?.name?.equals(selectedCat.name, ignoreCase = true) == true
            }
        } else {
            activeList
        }
        val query = _searchQuery.value.trim()
        val searchFiltered = if (query.isNotBlank()) {
            catFiltered.filter { p ->
                p.name.contains(query, ignoreCase = true) ||
                p.sku?.contains(query, ignoreCase = true) == true
            }
        } else {
            catFiltered
        }
        _products.value = searchFiltered
        _isLoading.value = false
    }

    fun addProductToCart(product: Product) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val item = currentList[index]
            val newQty = item.qty + 1
            if (newQty > product.stock_content) {
                _errorMessage.value = "Stok ${product.name} tidak mencukupi (Tersedia: ${product.stock_content})"
                return
            }
            val newEmpty = if (item.transactionType == "top_up") newQty else 0
            currentList[index] = item.copy(qty = newQty, emptyContainerAccepted = newEmpty)
        } else {
            if (product.stock_content <= 0) {
                _errorMessage.value = "Stok ${product.name} habis (Stok: 0)"
                return
            }
            val defaultType = if (product.deposit_price != null && product.deposit_price > 0) "top_up" else "buy_disconnect"
            val defaultEmpty = if (defaultType == "top_up") 1 else 0
            currentList.add(PosCartItem(product = product, transactionType = defaultType, qty = 1, emptyContainerAccepted = defaultEmpty))
        }
        _errorMessage.value = null
        _cartItems.value = currentList
    }

    fun updateCartItemQuantity(productId: Int, newQty: Int) {
        if (newQty <= 0) {
            removeCartItem(productId)
            return
        }
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = currentList[index]
            if (newQty > item.product.stock_content) {
                _errorMessage.value = "Stok ${item.product.name} tidak mencukupi (Tersedia: ${item.product.stock_content})"
                return
            }
            val newEmpty = if (item.transactionType == "top_up") newQty else 0
            currentList[index] = item.copy(qty = newQty, emptyContainerAccepted = newEmpty)
            _errorMessage.value = null
            _cartItems.value = currentList
        }
    }

    fun updateCartItemTransactionType(productId: Int, type: String) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = currentList[index]
            val emptyAccepted = if (type == "top_up") item.qty else 0
            currentList[index] = item.copy(transactionType = type, emptyContainerAccepted = emptyAccepted)
            _cartItems.value = currentList
        }
    }

    fun removeCartItem(productId: Int) {
        val currentList = _cartItems.value.toMutableList()
        currentList.removeAll { it.product.id == productId }
        _cartItems.value = currentList
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _cashReceivedText.value = ""
    }

    fun setCustomerName(name: String) {
        _customerName.value = name
    }

    fun setCustomerPhone(phone: String) {
        _customerPhone.value = phone
    }

    fun setCustomerAddress(address: String) {
        _customerAddress.value = address
    }

    fun setPaymentMethod(method: String) {
        val normalized = when (method.lowercase().trim()) {
            "tunai", "cash" -> "cash"
            "transfer" -> "transfer"
            else -> "other"
        }
        _paymentMethod.value = normalized
    }

    fun setChannel(ch: String) {
        _channel.value = ch
    }

    fun setCashReceivedText(text: String) {
        _cashReceivedText.value = text.filter { it.isDigit() }
    }

    fun setQuickCashAmount(amount: Double) {
        _cashReceivedText.value = amount.toLong().toString()
    }

    fun processSale() {
        if (_cartItems.value.isEmpty()) {
            _errorMessage.value = "Keranjang kasir masih kosong"
            return
        }

        // Validate stock sufficiency for all cart items before submitting
        val insufficientItem = _cartItems.value.find { it.qty > it.product.stock_content }
        if (insufficientItem != null) {
            _errorMessage.value = "Stok ${insufficientItem.product.name} tidak mencukupi (Tersedia: ${insufficientItem.product.stock_content}, diminta: ${insufficientItem.qty})"
            return
        }

        val currentTotal = totalAmount.value
        val selectedMethod = when (_paymentMethod.value.lowercase().trim()) {
            "tunai", "cash" -> "cash"
            "transfer" -> "transfer"
            else -> "other"
        }

        if (selectedMethod == "cash" && cashReceived.value > 0 && cashReceived.value < currentTotal) {
            _errorMessage.value = "Uang pembayaran kurang dari total bayar"
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null

            val reqItems = _cartItems.value.map { item ->
                CreateSaleItemRequest(
                    product_id = item.product.id,
                    transaction_type = item.transactionType,
                    qty = item.qty,
                    empty_container_accepted = item.emptyContainerAccepted
                )
            }

            val request = CreateSaleRequest(
                customer_name = _customerName.value.ifBlank { "Pelanggan Umum" },
                customer_phone = _customerPhone.value.ifBlank { null },
                customer_address = _customerAddress.value.ifBlank { null },
                channel = _channel.value,
                payment_method = selectedMethod,
                items = reqItems
            )

            val result = salesRepository.createSale(request)
            _isSubmitting.value = false

            val sale = result.getOrElse {
                // Offline-first fallback: generate a guaranteed unique negative ID to prevent primary key collisions
                val randomOffset = (1000..9999).random()
                val uniqueLocalId = -kotlin.math.abs((System.currentTimeMillis() % 1_000_000_000).toInt() + randomOffset)
                val timestampSuffix = System.currentTimeMillis().toString().takeLast(6)

                SaleTransaction(
                    id = uniqueLocalId,
                    transaction_code = "TRX-LOKAL-$timestampSuffix-$randomOffset",
                    invoice_number = "INV-LOKAL-$timestampSuffix",
                    customer_name = _customerName.value.ifBlank { "Pelanggan Umum" },
                    customer_phone = _customerPhone.value.ifBlank { null },
                    customer_address = _customerAddress.value.ifBlank { null },
                    status = "selesai",
                    payment_method = selectedMethod,
                    total_amount = currentTotal,
                    items = _cartItems.value.map { item ->
                        SaleItem(
                            product_id = item.product.id,
                            product_name = item.product.name,
                            transaction_type = item.transactionType,
                            qty = item.qty,
                            unit_price = item.unitPrice,
                            subtotal = item.itemSubtotal,
                            empty_container_accepted = item.emptyContainerAccepted
                        )
                    },
                    created_at = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US).format(Date())
                )
            }

            _completedTransaction.value = sale

            // Record stock movements and update local product stock in Room & Server
            val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US)
            val dateStr = dateFormat.format(Date())

            _cartItems.value.forEach { item ->
                val product = productDao.getProductById(item.product.id)
                if (product != null) {
                    if (syncRepository.isConnected()) {
                        stockRepository.stockKeluar(
                            productId = product.id,
                            stockType = "fill",
                            qty = item.qty,
                            description = "Penjualan (${item.transactionType}) - ${sale.transaction_code}"
                        )

                        if (item.emptyContainerAccepted > 0) {
                            stockRepository.stockMasuk(
                                productId = product.id,
                                stockType = "empty",
                                qty = item.emptyContainerAccepted,
                                description = "Wadah kosong diterima dari customer - ${sale.transaction_code}"
                            )
                        }
                    } else {
                        val newFillStock = maxOf(0, product.stockContent - item.qty)
                        productDao.updateProduct(product.copy(stockContent = newFillStock))

                        stockMovementDao.insertStockMovement(
                            StockMovementEntity(
                                productId = product.id,
                                productName = product.name,
                                stockType = "fill",
                                movementType = "out",
                                qty = item.qty,
                                endingBalance = newFillStock,
                                referenceType = "sales_transaction",
                                referenceId = sale.id,
                                description = "Penjualan (${item.transactionType}) - ${sale.transaction_code}",
                                createdAt = dateStr,
                                isSynced = false
                            )
                        )

                        // If empty container accepted
                        if (item.emptyContainerAccepted > 0) {
                            val newEmptyStock = product.emptyStock + item.emptyContainerAccepted
                            productDao.updateProduct(product.copy(emptyStock = newEmptyStock))

                            stockMovementDao.insertStockMovement(
                                StockMovementEntity(
                                    productId = product.id,
                                    productName = product.name,
                                    stockType = "empty",
                                    movementType = "in",
                                    qty = item.emptyContainerAccepted,
                                    endingBalance = newEmptyStock,
                                    referenceType = "sales_transaction",
                                    referenceId = sale.id,
                                    description = "Wadah kosong diterima dari customer - ${sale.transaction_code}",
                                    createdAt = dateStr,
                                    isSynced = false
                                )
                            )
                        }
                    }
                }
            }

            clearCart()
            loadProducts()
        }
    }

    fun dismissCompletedTransaction() {
        _completedTransaction.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
