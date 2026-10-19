package com.radityaf.depotify_wrz.ui.owner.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.local.dao.ProductDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LowStockItem(val name: String, val unit: String, val stock: Int, val minStock: Int)
data class DailySales(val label: String, val total: Long)
data class TopProduct(val name: String, val sold: Int)
data class RecentTransaction(val code: String, val total: Long, val time: String)

data class OwnerHomeUiState(
    val transaksiHariIni: Int = 0,
    val pendapatanHariIni: Long = 0,
    val produkAktif: Int = 0,
    val stokMenipis: Int = 0,
    val lowStock: List<LowStockItem> = emptyList(),
    val sales7Days: List<DailySales> = emptyList(),
    val topProducts: List<TopProduct> = emptyList(),
    val recentTransactions: List<RecentTransaction> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class OwnerHomeViewModel @Inject constructor(
    private val productDao: ProductDao
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<OwnerHomeUiState> = combine(
        productDao.getAllProducts(),
        _isLoading
    ) { products, isLoading ->
        val activeProducts = products.filter { it.isActive }
        val lowStockList = products.filter { it.stock <= it.minimumStock }.map {
            LowStockItem(
                name = it.name,
                unit = it.unit,
                stock = it.stock,
                minStock = it.minimumStock
            )
        }
        OwnerHomeUiState(
            produkAktif = activeProducts.size,
            stokMenipis = lowStockList.size,
            lowStock = lowStockList,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OwnerHomeUiState()
    )

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(400)
            _isLoading.value = false
        }
    }
}
