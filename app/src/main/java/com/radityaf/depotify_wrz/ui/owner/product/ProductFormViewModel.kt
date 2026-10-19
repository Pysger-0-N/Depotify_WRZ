package com.radityaf.depotify_wrz.ui.owner.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.local.dao.CategoryDao
import com.radityaf.depotify_wrz.data.local.dao.ProductDao
import com.radityaf.depotify_wrz.data.local.entity.CategoryEntity
import com.radityaf.depotify_wrz.data.local.entity.ProductEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductFormUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val product: ProductEntity? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class ProductFormViewModel @Inject constructor(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductFormUiState())
    val uiState: StateFlow<ProductFormUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryDao.getAllCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
    }

    fun loadProduct(productId: Int) {
        viewModelScope.launch {
            val p = productDao.getProductById(productId)
            _uiState.update { it.copy(product = p) }
        }
    }

    fun saveProduct(
        productId: Int?,
        categoryId: Int,
        categoryName: String,
        name: String,
        sku: String?,
        variant: String?,
        unit: String,
        sellingPrice: Double,
        depositPrice: Double?,
        purchasePrice: Double,
        minimumStock: Int,
        isActive: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                if (productId == null) {
                    val entity = ProductEntity(
                        name = name,
                        sku = sku,
                        categoryId = categoryId,
                        categoryName = categoryName,
                        variant = variant,
                        unit = unit,
                        sellingPrice = sellingPrice,
                        depositPrice = depositPrice,
                        purchasePrice = purchasePrice,
                        minimumStock = minimumStock,
                        isActive = isActive,
                        stock = 0
                    )
                    productDao.insertProduct(entity)
                } else {
                    val existing = productDao.getProductById(productId)
                    if (existing != null) {
                        val updated = existing.copy(
                            name = name,
                            sku = sku,
                            categoryId = categoryId,
                            categoryName = categoryName,
                            variant = variant,
                            unit = unit,
                            sellingPrice = sellingPrice,
                            depositPrice = depositPrice,
                            purchasePrice = purchasePrice,
                            minimumStock = minimumStock,
                            isActive = isActive
                        )
                        productDao.updateProduct(updated)
                    }
                }
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Gagal menyimpan produk ke Room DB") }
            }
        }
    }
}
