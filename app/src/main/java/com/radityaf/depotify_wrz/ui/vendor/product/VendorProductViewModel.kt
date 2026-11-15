package com.radityaf.depotify_wrz.ui.vendor.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.local.dao.CategoryDao
import com.radityaf.depotify_wrz.data.local.dao.ProductDao
import com.radityaf.depotify_wrz.data.local.entity.CategoryEntity
import com.radityaf.depotify_wrz.data.local.entity.ProductEntity
import com.radityaf.depotify_wrz.data.model.CreateProductRequest
import com.radityaf.depotify_wrz.data.repository.ProductRepository
import com.radityaf.depotify_wrz.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VendorProductUiState(
    val products: List<ProductEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val query: String = ""
) {
    val filteredProducts: List<ProductEntity>
        get() = if (query.isBlank()) products
        else products.filter { it.name.contains(query, ignoreCase = true) || it.sku?.contains(query, ignoreCase = true) == true }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class VendorProductViewModel @Inject constructor(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val productRepository: ProductRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _vendorId = MutableStateFlow<Int?>(null)
    private val _query = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _isSubmitting = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _successMessage = MutableStateFlow<String?>(null)

    private val productsFlow: Flow<List<ProductEntity>> = _vendorId.flatMapLatest { vId ->
        if (vId != null && vId > 0) {
            productDao.getProductsByVendor(vId)
        } else {
            productDao.getAllProducts()
        }
    }

    val uiState: StateFlow<VendorProductUiState> = combine(
        productsFlow,
        categoryDao.getAllCategories(),
        combine(_query, _isLoading, _isSubmitting, _errorMessage, _successMessage) { q, l, s, err, succ ->
            TupleState(q, l, s, err, succ)
        }
    ) { products, categories, tuple ->
        VendorProductUiState(
            products = products,
            categories = categories,
            query = tuple.query,
            isLoading = tuple.isLoading,
            isSubmitting = tuple.isSubmitting,
            errorMessage = tuple.errorMessage,
            successMessage = tuple.successMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VendorProductUiState()
    )

    private data class TupleState(
        val query: String,
        val isLoading: Boolean,
        val isSubmitting: Boolean,
        val errorMessage: String?,
        val successMessage: String?
    )

    fun setVendorId(id: Int?) {
        _vendorId.value = id
    }

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            syncRepository.syncServerToRoom()
            _isLoading.value = false
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (syncRepository.isConnected() && product.id > 0) {
                    productRepository.deleteProduct(product.id)
                }
                productDao.deleteProduct(product)
                _successMessage.value = "Produk '${product.name}' berhasil dihapus."
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Gagal menghapus produk"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getProductById(id: Int): ProductEntity? {
        return productDao.getProductById(id)
    }

    fun saveProduct(
        vendorId: Int?,
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
            _isSubmitting.value = true
            _errorMessage.value = null
            _successMessage.value = null
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
                    is_active = isActive,
                    vendor_id = vendorId
                )

                if (productId == null || productId == 0) {
                    val apiResult = productRepository.createProduct(req)
                    if (apiResult.isSuccess) {
                        val newProd = apiResult.getOrNull()
                        val entity = ProductEntity(
                            id = newProd?.id ?: 0,
                            vendorId = newProd?.vendor_id ?: vendorId,
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
                            vendorId = vendorId,
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
                        vendorId = vendorId ?: existing?.vendorId,
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

                _isSubmitting.value = false
                _successMessage.value = "Produk berhasil disimpan!"
                onSuccess()
            } catch (e: Exception) {
                _isSubmitting.value = false
                _errorMessage.value = e.message ?: "Gagal menyimpan produk"
            }
        }
    }
}
