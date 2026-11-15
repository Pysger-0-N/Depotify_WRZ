package com.radityaf.depotify_wrz.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.local.dao.ProductDao
import com.radityaf.depotify_wrz.data.local.entity.ProductEntity
import com.radityaf.depotify_wrz.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductUiState(
    val allItems: List<ProductEntity> = emptyList(),
    val isLoading: Boolean = false,
    val query: String = ""
) {
    val filtered: List<ProductEntity>
        get() = if (query.isBlank()) allItems
        else allItems.filter { it.name.contains(query, true) || it.barcode.contains(query) }
}

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productDao: ProductDao,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<ProductUiState> = combine(
        productDao.getAllProducts(),
        _query,
        _isLoading
    ) { products, query, isLoading ->
        ProductUiState(
            allItems = products,
            isLoading = isLoading,
            query = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProductUiState()
    )

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            syncRepository.syncServerToRoom()
            _isLoading.value = false
        }
    }

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun getById(id: Int): ProductEntity? {
        return uiState.value.allItems.find { it.id == id }
    }

    fun getByBarcode(code: String): ProductEntity? {
        return uiState.value.allItems.find { it.barcode == code }
    }

    fun addProduct(product: ProductEntity) {
        viewModelScope.launch {
            productDao.insertProduct(product)
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            productDao.updateProduct(product)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            productDao.deleteProduct(product)
        }
    }

    fun deleteProductById(id: Int) {
        viewModelScope.launch {
            productDao.deleteProductById(id)
        }
    }
}
