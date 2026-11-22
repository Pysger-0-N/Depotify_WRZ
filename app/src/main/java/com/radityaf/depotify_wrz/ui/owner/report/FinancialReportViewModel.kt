package com.radityaf.depotify_wrz.ui.owner.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.model.Product
import com.radityaf.depotify_wrz.data.model.PurchaseOrder
import com.radityaf.depotify_wrz.data.model.SaleTransaction
import com.radityaf.depotify_wrz.data.repository.ProductRepository
import com.radityaf.depotify_wrz.data.repository.PurchaseOrderRepository
import com.radityaf.depotify_wrz.data.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FinancialTab {
    SUMMARY, SALES, PURCHASE_ORDERS, PRODUCTS
}

data class ProductPerformanceItem(
    val productName: String,
    val totalQtySold: Int,
    val totalRevenue: Double
)

@HiltViewModel
class FinancialReportViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val poRepository: PurchaseOrderRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _sales = MutableStateFlow<List<SaleTransaction>>(emptyList())
    val sales: StateFlow<List<SaleTransaction>> = _sales.asStateFlow()

    private val _purchaseOrders = MutableStateFlow<List<PurchaseOrder>>(emptyList())
    val purchaseOrders: StateFlow<List<PurchaseOrder>> = _purchaseOrders.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedTab = MutableStateFlow(FinancialTab.SUMMARY)
    val selectedTab: StateFlow<FinancialTab> = _selectedTab.asStateFlow()

    // 5 Financial Metrics
    private val _totalOmset = MutableStateFlow(0.0)
    val totalOmset: StateFlow<Double> = _totalOmset.asStateFlow()

    private val _totalExpense = MutableStateFlow(0.0)
    val totalExpense: StateFlow<Double> = _totalExpense.asStateFlow()

    private val _netProfit = MutableStateFlow(0.0)
    val netProfit: StateFlow<Double> = _netProfit.asStateFlow()

    private val _netLoss = MutableStateFlow(0.0)
    val netLoss: StateFlow<Double> = _netLoss.asStateFlow()

    private val _netBalance = MutableStateFlow(0.0)
    val netBalance: StateFlow<Double> = _netBalance.asStateFlow()

    // Product Performance Analysis
    private val _bestSellingProducts = MutableStateFlow<List<ProductPerformanceItem>>(emptyList())
    val bestSellingProducts: StateFlow<List<ProductPerformanceItem>> = _bestSellingProducts.asStateFlow()

    private val _slowMovingProducts = MutableStateFlow<List<Product>>(emptyList())
    val slowMovingProducts: StateFlow<List<Product>> = _slowMovingProducts.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val salesResult = salesRepository.getSales()
            val poResult = poRepository.getPurchaseOrders()
            val prodResult = productRepository.getProducts()

            if (salesResult.isSuccess && poResult.isSuccess) {
                val sList = salesResult.getOrNull() ?: emptyList()
                val poApiResponse = poResult.getOrNull()
                val pList = poApiResponse?.data ?: emptyList()
                val prodList = prodResult.getOrNull() ?: emptyList()

                _sales.value = sList
                _purchaseOrders.value = pList
                _products.value = prodList

                // 1. Calculate Financial Metrics
                // Pemasukan: sum of completed sales
                val omset = sList.filter { it.status.equals("completed", true) || it.status.equals("selesai", true) || it.status.equals("confirmed", true) }.sumOf { it.total_amount }
                // Pengeluaran: sum of received/completed POs
                val expense = pList.filter { po -> po.status == "received" || po.status == "completed" }.sumOf { it.displayTotal }
                
                val balance = omset - expense
                val profit = if (balance > 0) balance else 0.0
                val loss = if (balance < 0) -balance else 0.0

                _totalOmset.value = omset
                _totalExpense.value = expense
                _netProfit.value = profit
                _netLoss.value = loss
                _netBalance.value = balance

                // 2. Product Performance Analysis
                val productSalesMap = mutableMapOf<String, Pair<Int, Double>>() // Name -> (Qty, Revenue)
                sList.forEach { sale ->
                    sale.items.forEach { item ->
                        val name = item.product_name.ifBlank { "Produk #${item.product_id}" }
                        val current = productSalesMap[name] ?: Pair(0, 0.0)
                        productSalesMap[name] = Pair(current.first + item.qty, current.second + item.subtotal)
                    }
                }

                val bestSelling = productSalesMap.entries
                    .map { ProductPerformanceItem(it.key, it.value.first, it.value.second) }
                    .sortedByDescending { it.totalQtySold }
                _bestSellingProducts.value = bestSelling

                // Slow moving: products with 0 sales or lowest sales
                val soldProductNames = productSalesMap.keys.toSet()
                val slowMoving = prodList.filter { !soldProductNames.contains(it.name) }
                _slowMovingProducts.value = slowMoving

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
}
