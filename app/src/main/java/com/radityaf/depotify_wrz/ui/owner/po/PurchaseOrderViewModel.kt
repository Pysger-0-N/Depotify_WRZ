package com.radityaf.depotify_wrz.ui.owner.po

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.local.dao.ProductDao
import com.radityaf.depotify_wrz.data.local.dao.VendorDao
import com.radityaf.depotify_wrz.data.local.entity.ProductEntity
import com.radityaf.depotify_wrz.data.local.entity.VendorEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PoItem(val productName: String, val qty: Int, val price: Long)

data class PurchaseOrder(
    val id: Int,
    val vendorName: String,
    val status: String, // "PENDING" / "DITERIMA"
    val items: List<PoItem>
) {
    val total: Long get() = items.sumOf { it.qty * it.price }
}

data class PoUiState(
    val items: List<PurchaseOrder> = emptyList(),
    val vendors: List<VendorEntity> = emptyList(),
    val products: List<ProductEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class PurchaseOrderViewModel @Inject constructor(
    private val vendorDao: VendorDao,
    private val productDao: ProductDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(PoUiState())
    val uiState: StateFlow<PoUiState> = _uiState.asStateFlow()

    init {
        loadDropdownData()
    }

    private fun loadDropdownData() {
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
    }

    fun getById(id: Int): PurchaseOrder? = _uiState.value.items.find { it.id == id }

    fun create(vendorName: String, productName: String, qty: Int, price: Long) {
        _uiState.update {
            val nextId = (it.items.maxOfOrNull { po -> po.id } ?: 0) + 1
            val po = PurchaseOrder(nextId, vendorName, "PENDING", listOf(PoItem(productName, qty, price)))
            it.copy(items = it.items + po)
        }
    }

    fun receiveGoods(id: Int) {
        _uiState.update { s ->
            s.copy(items = s.items.map { if (it.id == id) it.copy(status = "DITERIMA") else it })
        }
    }
}
