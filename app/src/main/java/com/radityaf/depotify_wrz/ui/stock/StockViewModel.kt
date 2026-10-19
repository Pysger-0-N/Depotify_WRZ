package com.radityaf.depotify_wrz.ui.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.local.dao.ProductDao
import com.radityaf.depotify_wrz.data.local.dao.StockMovementDao
import com.radityaf.depotify_wrz.data.local.entity.ProductEntity
import com.radityaf.depotify_wrz.data.local.entity.StockMovementEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class StockMovementUi(
    val id: Int,
    val productName: String,
    val type: String, // "MASUK", "KELUAR", "PENYESUAIAN"
    val qty: Int,
    val date: String
)

data class StockUiState(
    val items: List<StockMovementUi> = emptyList(),
    val products: List<ProductEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class StockViewModel @Inject constructor(
    private val stockMovementDao: StockMovementDao,
    private val productDao: ProductDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            productDao.getAllProducts().collect { products ->
                _uiState.update { it.copy(products = products) }
            }
        }
        viewModelScope.launch {
            stockMovementDao.getAllStockMovements().collect { movements ->
                val mapped = movements.map { m ->
                    StockMovementUi(
                        id = m.id,
                        productName = m.productName,
                        type = m.movementType.uppercase(),
                        qty = m.qty,
                        date = m.createdAt ?: LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
                    )
                }
                _uiState.update { it.copy(items = mapped) }
            }
        }
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
        viewModelScope.launch {
            val dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
            val movement = StockMovementEntity(
                productId = productId,
                productName = productName,
                stockType = stockType,
                movementType = movementType,
                qty = qty,
                description = description,
                createdAt = dateStr
            )
            stockMovementDao.insertStockMovement(movement)

            val product = productDao.getProductById(productId)
            if (product != null) {
                val newStock = when (movementType.lowercase()) {
                    "masuk" -> product.stock + qty
                    "keluar" -> maxOf(0, product.stock - qty)
                    "penyesuaian" -> qty
                    else -> product.stock + qty
                }
                productDao.updateProduct(product.copy(stock = newStock))
            }

            onSuccess()
        }
    }
}
