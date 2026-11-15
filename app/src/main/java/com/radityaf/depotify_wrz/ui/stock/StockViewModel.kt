package com.radityaf.depotify_wrz.ui.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.local.dao.ProductDao
import com.radityaf.depotify_wrz.data.local.dao.StockMovementDao
import com.radityaf.depotify_wrz.data.local.entity.ProductEntity
import com.radityaf.depotify_wrz.data.local.entity.StockMovementEntity
import com.radityaf.depotify_wrz.data.model.StockMovement
import com.radityaf.depotify_wrz.data.repository.StockRepository
import com.radityaf.depotify_wrz.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class StockMovementUi(
    val id: Int,
    val productId: Int,
    val productName: String,
    val type: String, // "MASUK", "KELUAR", "PENYESUAIAN"
    val stockType: String, // "ISI", "KOSONG"
    val qty: Int,
    val endingBalance: Int,
    val referenceType: String, // "manual", "sales_transaction", "purchase_order", "goods_receipt"
    val referenceId: Int?,
    val description: String?,
    val date: String
)

data class StockUiState(
    val items: List<StockMovementUi> = emptyList(),
    val products: List<ProductEntity> = emptyList(),
    val searchQuery: String = "",
    val movementTypeFilter: String = "SEMUA", // "SEMUA", "MASUK", "KELUAR", "PENYESUAIAN"
    val stockTypeFilter: String = "SEMUA", // "SEMUA", "ISI", "KOSONG"
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isOnline: Boolean = true
) {
    val filteredItems: List<StockMovementUi>
        get() = items.filter { m ->
            val matchesMovement = when (movementTypeFilter.uppercase()) {
                "SEMUA" -> true
                else -> m.type.equals(movementTypeFilter, ignoreCase = true)
            }
            val matchesStock = when (stockTypeFilter.uppercase()) {
                "SEMUA" -> true
                else -> m.stockType.equals(stockTypeFilter, ignoreCase = true)
            }
            val matchesQuery = if (searchQuery.isBlank()) true else {
                m.productName.contains(searchQuery, ignoreCase = true) ||
                (m.description?.contains(searchQuery, ignoreCase = true) == true) ||
                m.type.contains(searchQuery, ignoreCase = true)
            }
            matchesMovement && matchesStock && matchesQuery
        }

    val totalMasukCount: Int
        get() = filteredItems.filter { it.type == "MASUK" }.sumOf { it.qty }

    val totalKeluarCount: Int
        get() = filteredItems.filter { it.type == "KELUAR" }.sumOf { it.qty }
}

@HiltViewModel
class StockViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    private val stockMovementDao: StockMovementDao,
    private val productDao: ProductDao,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    init {
        observeNetwork()
        observeLocalData()
        ensureProductsLoaded()
        fetchServerMovements()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            syncRepository.isOnlineFlow.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
            }
        }
    }

    private fun ensureProductsLoaded() {
        viewModelScope.launch {
            val localProducts = productDao.getAllProductsSync()
            if (localProducts.isEmpty() && syncRepository.isConnected()) {
                syncRepository.syncServerToRoom()
            }
        }
    }

    private fun observeLocalData() {
        viewModelScope.launch {
            productDao.getAllProducts().collect { products ->
                _uiState.update { it.copy(products = products) }
            }
        }

        viewModelScope.launch {
            stockMovementDao.getAllStockMovements().collect { movements ->
                val mapped = movements.map { m ->
                    val typeLabel = when (m.movementType.lowercase()) {
                        "in", "masuk" -> "MASUK"
                        "out", "keluar" -> "KELUAR"
                        "adjustment", "penyesuaian" -> "PENYESUAIAN"
                        else -> m.movementType.uppercase()
                    }
                    val stockTypeLabel = when (m.stockType.lowercase()) {
                        "fill", "isi" -> "ISI"
                        "empty", "kosong" -> "KOSONG"
                        else -> m.stockType.uppercase()
                    }
                    StockMovementUi(
                        id = m.id,
                        productId = m.productId,
                        productName = m.productName,
                        type = typeLabel,
                        stockType = stockTypeLabel,
                        qty = m.qty,
                        endingBalance = m.endingBalance,
                        referenceType = m.referenceType,
                        referenceId = m.referenceId,
                        description = m.description,
                        date = m.createdAt ?: LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
                    )
                }
                _uiState.update { it.copy(items = mapped) }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setMovementTypeFilter(type: String) {
        _uiState.update { it.copy(movementTypeFilter = type) }
    }

    fun setStockTypeFilter(type: String) {
        _uiState.update { it.copy(stockTypeFilter = type) }
    }

    fun refresh() {
        viewModelScope.launch {
            if (syncRepository.isConnected()) {
                syncRepository.syncServerToRoom()
            }
            fetchServerMovements()
        }
    }

    fun fetchServerMovements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = stockRepository.getStockMovements()
            result.fold(
                onSuccess = {
                    // stockRepository.getStockMovements() already cached movements to Room DB.
                    // Room DB Flow will update uiState.items reactively.
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { _ ->
                    // Do not show error message if local Room DB has movements; fallback silently to local cache
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun addStockMovement(
        productId: Int,
        productName: String,
        stockType: String,
        movementType: String,
        qty: Int,
        description: String?,
        onSuccess: () -> Unit
    ) {
        if (_uiState.value.isSubmitting) return
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val normalizedStockType = when (stockType.lowercase()) {
                "isi", "fill" -> "fill"
                "kosong", "empty" -> "empty"
                else -> "fill"
            }
            val normalizedMovementType = when (movementType.lowercase()) {
                "masuk", "in" -> "in"
                "keluar", "out" -> "out"
                "penyesuaian", "adjustment" -> "adjustment"
                else -> "in"
            }

            // Attempt server transaction first
            val result = when (normalizedMovementType) {
                "in" -> stockRepository.stockMasuk(productId, normalizedStockType, qty, description)
                "out" -> stockRepository.stockKeluar(productId, normalizedStockType, qty, description)
                "adjustment" -> stockRepository.stockPenyesuaian(productId, normalizedStockType, qty, description)
                else -> stockRepository.stockMasuk(productId, normalizedStockType, qty, description)
            }

            result.fold(
                onSuccess = { apiMovement ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = "Pergerakan stok berhasil dicatat."
                        )
                    }
                    fetchServerMovements()
                    onSuccess()
                },
                onFailure = { error ->
                    // Room Database Backup Fallback if server is unreachable
                    val dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
                    val product = productDao.getProductById(productId)
                    val currentStock = if (normalizedStockType == "fill") product?.stockContent ?: 0 else product?.emptyStock ?: 0

                    val endingBalance = when (normalizedMovementType) {
                        "in" -> currentStock + qty
                        "out" -> maxOf(0, currentStock - qty)
                        "adjustment" -> qty
                        else -> currentStock + qty
                    }

                    val movement = StockMovementEntity(
                        productId = productId,
                        productName = productName,
                        stockType = normalizedStockType,
                        movementType = normalizedMovementType,
                        qty = qty,
                        endingBalance = endingBalance,
                        referenceType = "manual",
                        referenceId = null,
                        description = description ?: "Pergerakan manual (offline fallback)",
                        createdBy = 1,
                        createdAt = dateStr,
                        isSynced = false
                    )
                    stockMovementDao.insertStockMovement(movement)

                    if (product != null) {
                        if (normalizedStockType == "fill") {
                            productDao.updateProduct(product.copy(stockContent = endingBalance))
                        } else {
                            productDao.updateProduct(product.copy(emptyStock = endingBalance))
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = "Tersimpan ke Room DB (Backup): ${error.message ?: "Server tidak terjangkau"}"
                        )
                    }
                    onSuccess()
                }
            )
        }
    }
}
