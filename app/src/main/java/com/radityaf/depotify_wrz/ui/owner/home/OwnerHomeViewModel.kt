package com.radityaf.depotify_wrz.ui.owner.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.local.dao.ProductDao
import com.radityaf.depotify_wrz.data.local.dao.StockMovementDao
import com.radityaf.depotify_wrz.data.local.entity.StockMovementEntity
import com.radityaf.depotify_wrz.data.model.SaleTransaction
import com.radityaf.depotify_wrz.data.repository.SalesRepository
import com.radityaf.depotify_wrz.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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
    val isLoading: Boolean = false,
    val isOnline: Boolean = true
)

@HiltViewModel
class OwnerHomeViewModel @Inject constructor(
    private val productDao: ProductDao,
    private val stockMovementDao: StockMovementDao,
    private val salesRepository: SalesRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _remoteSales = MutableStateFlow<List<SaleTransaction>>(emptyList())

    val isOnline: Flow<Boolean> = syncRepository.isOnlineFlow

    val uiState: StateFlow<OwnerHomeUiState> = combine(
        productDao.getAllProducts(),
        stockMovementDao.getAllStockMovements(),
        _remoteSales,
        _isLoading,
        syncRepository.isOnlineFlow
    ) { products, movements, remoteSales, isLoading, online ->
        val activeProducts = products.filter { it.isActive }
        val lowStockList = products.filter { it.stock <= it.minimumStock }.map {
            LowStockItem(
                name = it.name,
                unit = it.unit,
                stock = it.stock,
                minStock = it.minimumStock
            )
        }

        // Process Sales (Hybrid: Remote Sales if available, otherwise local Stock Movements for sales)
        val (todayCount, todayRevenue, sales7DaysList, topProductsList, recentTxList) =
            if (remoteSales.isNotEmpty()) {
                processRemoteSales(remoteSales)
            } else {
                processLocalMovements(movements)
            }

        OwnerHomeUiState(
            transaksiHariIni = todayCount,
            pendapatanHariIni = todayRevenue,
            produkAktif = activeProducts.size,
            stokMenipis = lowStockList.size,
            lowStock = lowStockList,
            sales7Days = sales7DaysList,
            topProducts = topProductsList,
            recentTransactions = recentTxList,
            isLoading = isLoading,
            isOnline = online
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OwnerHomeUiState()
    )

    init {
        loadRemoteSales()
    }

    fun loadRemoteSales() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = salesRepository.getSales()
            result.onSuccess { list ->
                _remoteSales.value = list
            }
            _isLoading.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            if (syncRepository.isConnected()) {
                syncRepository.syncFullTwoWay()
                loadRemoteSales()
            }
            _isLoading.value = false
        }
    }

    private fun processRemoteSales(sales: List<SaleTransaction>): ProcessedSalesData {
        val todayCal = Calendar.getInstance()
        val todayYear = todayCal.get(Calendar.YEAR)
        val todayDayOfYear = todayCal.get(Calendar.DAY_OF_YEAR)

        var todayCount = 0
        var todayRevenue = 0L

        sales.forEach { sale ->
            val saleDate = parseDateString(sale.created_at)
            if (saleDate != null) {
                val cal = Calendar.getInstance().apply { time = saleDate }
                if (cal.get(Calendar.YEAR) == todayYear && cal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear) {
                    todayCount++
                    todayRevenue += sale.total_amount.toLong()
                }
            }
        }

        // 7 Days Sales
        val sales7Days = calculate7DaysSalesFromRemote(sales)

        // Top Products
        val topProductsMap = mutableMapOf<String, Int>()
        sales.forEach { sale ->
            sale.items.forEach { item ->
                topProductsMap[item.product_name] = (topProductsMap[item.product_name] ?: 0) + item.qty
            }
        }
        val topProducts = topProductsMap.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { TopProduct(name = it.key, sold = it.value) }

        // Recent Transactions
        val recentTx = sales.take(5).map { sale ->
            RecentTransaction(
                code = sale.displayInvoice,
                total = sale.total_amount.toLong(),
                time = formatDisplayTime(sale.created_at)
            )
        }

        return ProcessedSalesData(todayCount, todayRevenue, sales7Days, topProducts, recentTx)
    }

    private fun processLocalMovements(movements: List<StockMovementEntity>): ProcessedSalesData {
        val salesMovements = movements.filter {
            it.movementType == "out" && (it.referenceType == "sales_transaction" || it.description?.contains("Penjualan", ignoreCase = true) == true)
        }

        val todayCal = Calendar.getInstance()
        val todayYear = todayCal.get(Calendar.YEAR)
        val todayDayOfYear = todayCal.get(Calendar.DAY_OF_YEAR)

        var todayCount = 0
        var todayRevenue = 0L

        salesMovements.forEach { m ->
            val date = parseDateString(m.createdAt)
            if (date != null) {
                val cal = Calendar.getInstance().apply { time = date }
                if (cal.get(Calendar.YEAR) == todayYear && cal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear) {
                    todayCount++
                    todayRevenue += m.qty * 10000L // Estimasi jika data lokal
                }
            }
        }

        val sales7Days = calculate7DaysSalesFromLocal(salesMovements)

        val topProductsMap = mutableMapOf<String, Int>()
        salesMovements.forEach { m ->
            topProductsMap[m.productName] = (topProductsMap[m.productName] ?: 0) + m.qty
        }
        val topProducts = topProductsMap.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { TopProduct(name = it.key, sold = it.value) }

        val recentTx = salesMovements.take(5).map { m ->
            RecentTransaction(
                code = m.referenceId?.let { "TRX-$it" } ?: "TRX-LOKAL",
                total = m.qty * 10000L,
                time = formatDisplayTime(m.createdAt)
            )
        }

        return ProcessedSalesData(todayCount, todayRevenue, sales7Days, topProducts, recentTx)
    }

    private fun calculate7DaysSalesFromRemote(sales: List<SaleTransaction>): List<DailySales> {
        val days = mutableListOf<DailySales>()

        // Generate for last 7 days (from 6 days ago to today)
        val dayFormats = SimpleDateFormat("EEE (dd/MM)", Locale("id", "ID"))

        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dayYear = dayCal.get(Calendar.YEAR)
            val dayOfYear = dayCal.get(Calendar.DAY_OF_YEAR)
            val dayLabel = dayFormats.format(dayCal.time)

            var dayTotal = 0L
            sales.forEach { sale ->
                val saleDate = parseDateString(sale.created_at)
                if (saleDate != null) {
                    val sCal = Calendar.getInstance().apply { time = saleDate }
                    if (sCal.get(Calendar.YEAR) == dayYear && sCal.get(Calendar.DAY_OF_YEAR) == dayOfYear) {
                        dayTotal += sale.total_amount.toLong()
                    }
                }
            }
            days.add(DailySales(label = dayLabel, total = dayTotal))
        }

        return days
    }

    private fun calculate7DaysSalesFromLocal(movements: List<StockMovementEntity>): List<DailySales> {
        val days = mutableListOf<DailySales>()
        val dayFormats = SimpleDateFormat("EEE (dd/MM)", Locale("id", "ID"))

        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dayYear = dayCal.get(Calendar.YEAR)
            val dayOfYear = dayCal.get(Calendar.DAY_OF_YEAR)
            val dayLabel = dayFormats.format(dayCal.time)

            var dayTotal = 0L
            movements.forEach { m ->
                val date = parseDateString(m.createdAt)
                if (date != null) {
                    val sCal = Calendar.getInstance().apply { time = date }
                    if (sCal.get(Calendar.YEAR) == dayYear && sCal.get(Calendar.DAY_OF_YEAR) == dayOfYear) {
                        dayTotal += m.qty * 10000L
                    }
                }
            }
            days.add(DailySales(label = dayLabel, total = dayTotal))
        }

        return days
    }

    private fun parseDateString(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
            SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US),
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
        )
        for (format in formats) {
            try {
                return format.parse(dateStr)
            } catch (_: Exception) {}
        }
        return null
    }

    private fun formatDisplayTime(dateStr: String?): String {
        val date = parseDateString(dateStr) ?: return dateStr ?: "-"
        return SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID")).format(date)
    }

    private data class ProcessedSalesData(
        val todayCount: Int,
        val todayRevenue: Long,
        val sales7Days: List<DailySales>,
        val topProducts: List<TopProduct>,
        val recentTx: List<RecentTransaction>
    )
}
