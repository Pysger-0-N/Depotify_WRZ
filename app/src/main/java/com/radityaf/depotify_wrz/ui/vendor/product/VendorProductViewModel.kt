package com.radityaf.depotify_wrz.ui.vendor.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.model.Category
import com.radityaf.depotify_wrz.data.model.CreateProductRequest
import com.radityaf.depotify_wrz.data.model.Product
import com.radityaf.depotify_wrz.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VendorProductUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val query: String = ""
) {
    val filteredProducts: List<Product>
        get() = if (query.isBlank()) products
        else products.filter { it.name.contains(query, ignoreCase = true) || it.sku?.contains(query, ignoreCase = true) == true }
}

@HiltViewModel
class VendorProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _vendorId = MutableStateFlow<Int?>(null)
    private val _uiState = MutableStateFlow(VendorProductUiState())
    val uiState: StateFlow<VendorProductUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun setVendorId(id: Int?) {
        if (_vendorId.value != id) {
            _vendorId.value = id
            loadData()
        }
    }

    fun onQueryChange(q: String) {
        _uiState.update { it.copy(query = q) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun refresh() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val categoriesResult = productRepository.getCategories()
            val categories = categoriesResult.getOrNull() ?: emptyList()

            val productsResult = productRepository.getProducts()
            productsResult.fold(
                onSuccess = { allProducts ->
                    val vId = _vendorId.value
                    val filtered = if (vId != null && vId > 0) {
                        allProducts.filter { it.vendor_id == vId }
                    } else {
                        allProducts
                    }
                    _uiState.update {
                        it.copy(
                            products = filtered,
                            categories = categories,
                            isLoading = false
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            categories = categories,
                            errorMessage = err.message ?: "Gagal memuat produk dari server"
                        )
                    }
                }
            )
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = productRepository.deleteProduct(product.id)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(successMessage = "Produk '${product.name}' berhasil dihapus.") }
                    loadData()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.message ?: "Gagal menghapus produk"
                        )
                    }
                }
            )
        }
    }

    suspend fun getProductById(id: Int): Product? {
        val existing = _uiState.value.products.find { it.id == id }
        if (existing != null) return existing
        val result = productRepository.getProductDetail(id)
        return result.getOrNull()
    }

    fun saveProduct(
        vendorId: Int?,
        productId: Int?,
        categoryId: Int,
        name: String,
        sku: String?,
        variant: String?,
        unit: String,
        sellingPrice: Double,
        depositPrice: Double?,
        purchasePrice: Double,
        stockContent: Int,
        minimumStock: Int,
        isActive: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
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
                    stock_content = stockContent,
                    empty_stock = 0,
                    minimum_stock = minimumStock,
                    is_active = isActive,
                    vendor_id = vendorId
                )

                val result = if (productId == null || productId == 0) {
                    productRepository.createProduct(req)
                } else {
                    productRepository.updateProduct(productId, req)
                }

                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isSubmitting = false, successMessage = "Produk berhasil disimpan.") }
                        loadData()
                        onSuccess()
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                errorMessage = err.message ?: "Gagal menyimpan produk"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = e.message ?: "Gagal menyimpan produk"
                    )
                }
            }
        }
    }
}
