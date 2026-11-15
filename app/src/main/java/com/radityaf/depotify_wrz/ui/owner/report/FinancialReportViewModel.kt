package com.radityaf.depotify_wrz.ui.owner.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.model.PurchaseOrder
import com.radityaf.depotify_wrz.data.model.SaleTransaction
import com.radityaf.depotify_wrz.data.repository.PurchaseOrderRepository
import com.radityaf.depotify_wrz.data.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FinancialTab {
    SUMMARY, SALES, PURCHASE_ORDERS
}

@HiltViewModel
class FinancialReportViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val poRepository: PurchaseOrderRepository
) : ViewModel() {

    private val _sales = MutableStateFlow<List<SaleTransaction>>(emptyList())
    val sales: StateFlow<List<SaleTransaction>> = _sales.asStateFlow()

    private val _purchaseOrders = MutableStateFlow<List<PurchaseOrder>>(emptyList())
    val purchaseOrders: StateFlow<List<PurchaseOrder>> = _purchaseOrders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedTab = MutableStateFlow(FinancialTab.SUMMARY)
    val selectedTab: StateFlow<FinancialTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    // Calculated metrics
    private val _totalOmset = MutableStateFlow(0.0)
    val totalOmset: StateFlow<Double> = _totalOmset.asStateFlow()

    private val _totalExpense = MutableStateFlow(0.0)
    val totalExpense: StateFlow<Double> = _totalExpense.asStateFlow()

    private val _estimatedProfit = MutableStateFlow(0.0)
    val estimatedProfit: StateFlow<Double> = _estimatedProfit.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val salesResult = salesRepository.getSales()
            val poResult = poRepository.getPurchaseOrders()

            if (salesResult.isSuccess && poResult.isSuccess) {
                val sList = salesResult.getOrNull() ?: emptyList()
                val poApiResponse = poResult.getOrNull()
                val pList = poApiResponse?.data ?: emptyList()

                _sales.value = sList
                _purchaseOrders.value = pList

                // Calculate financial summary
                val omset = sList.sumOf { it.total_amount }
                val expense = pList.filter { po -> po.status == "received" || po.status == "completed" }.sumOf { it.displayTotal }
                
                _totalOmset.value = omset
                _totalExpense.value = expense
                _estimatedProfit.value = omset - expense
                _isLoading.value = false
            } else {
                val err = salesResult.exceptionOrNull()?.localizedMessage ?: poResult.exceptionOrNull()?.localizedMessage ?: "Gagal memuat laporan keuangan"
                _error.value = err
                _isLoading.value = false
            }
        }
    }

    fun setTab(tab: FinancialTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: String?) {
        _statusFilter.value = if (status == "all") null else status
    }
}
