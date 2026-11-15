package com.radityaf.depotify_wrz.ui.owner.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.local.dao.CategoryDao
import com.radityaf.depotify_wrz.data.local.dao.ProductDao
import com.radityaf.depotify_wrz.data.local.entity.CategoryEntity
import com.radityaf.depotify_wrz.data.local.entity.ProductEntity
import com.radityaf.depotify_wrz.data.model.CreateProductRequest
import com.radityaf.depotify_wrz.data.repository.ProductRepository
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
    private val categoryDao: CategoryDao,
    private val productRepository: ProductRepository
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

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
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
                val req = CreateProductRequest(
                    category_id = categoryId,
                    name = name.trim(),
                    sku = sku?.trim()?.ifBlank { null },
                    variant = variant?.trim()?.ifBlank { null },
                    unit = unit,
                    selling_price = sellingPrice,
                    deposit_price = depositPrice,
                    purchase_price = purchasePrice,
                    minimum_stock = minimumStock,
                    is_active = isActive
                )

                if (productId == null || productId == 0) {
                    val apiResult = productRepository.createProduct(req)
                    if (apiResult.isSuccess) {
                        val newProd = apiResult.getOrNull()
                        val entity = ProductEntity(
                            id = newProd?.id ?: 0,
                            vendorId = newProd?.vendor_id,
                            name = newProd?.name ?: name.trim(),
                            sku = newProd?.sku ?: sku,
                            categoryId = categoryId,
                            categoryName = categoryName,
                            variant = variant,
                            unit = unit,
                            sellingPrice = sellingPrice,
                            depositPrice = depositPrice,
                            purchasePrice = purchasePrice,
                            minimumStock = minimumStock,
                            isActive = isActive,
                            stockContent = newProd?.stock_content ?: 0,
                            emptyStock = newProd?.empty_stock ?: 0,
                            isSynced = true
                        )
                        productDao.insertProduct(entity)
                    } else {
                        val entity = ProductEntity(
                            id = 0,
                            name = name.trim(),
                            sku = sku?.trim()?.ifBlank { null },
                            categoryId = categoryId,
                            categoryName = categoryName,
                            variant = variant,
                            unit = unit,
                            sellingPrice = sellingPrice,
                            depositPrice = depositPrice,
                            purchasePrice = purchasePrice,
                            minimumStock = minimumStock,
                            isActive = isActive,
                            stockContent = 0,
                            emptyStock = 0,
                            isSynced = false
                        )
                        productDao.insertProduct(entity)
                    }
                } else {
                    val existing = productDao.getProductById(productId)
                    val apiResult = productRepository.updateProduct(productId, req)
                    val isSynced = apiResult.isSuccess
                    val updated = (existing ?: ProductEntity(id = productId, name = name)).copy(
                        name = name.trim(),
                        sku = sku?.trim()?.ifBlank { null },
                        categoryId = categoryId,
                        categoryName = categoryName,
                        variant = variant,
                        unit = unit,
                        sellingPrice = sellingPrice,
                        depositPrice = depositPrice,
                        purchasePrice = purchasePrice,
                        minimumStock = minimumStock,
                        isActive = isActive,
                        isSynced = isSynced
                    )
                    productDao.updateProduct(updated)
                }
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Gagal menyimpan data produk") }
            }
        }
    }
}
