package com.radityaf.depotify_wrz.ui.owner.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.local.dao.ProductDao
import com.radityaf.depotify_wrz.data.repository.NotificationRepository
import com.radityaf.depotify_wrz.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockNotificationItem(
    val id: Int,
    val productId: Int,
    val productName: String,
    val currentStock: Int,
    val minStock: Int,
    val unit: String,
    val message: String,
    val isRead: Boolean = false
)

data class NotificationUiState(
    val items: List<StockNotificationItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isOnline: Boolean = true
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val productDao: ProductDao,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        observeNetwork()
        fetchNotifications()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            syncRepository.isOnlineFlow.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
            }
        }
    }

    fun refresh() {
        fetchNotifications()
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = notificationRepository.getNotifications()
            result.fold(
                onSuccess = { apiNotifications ->
                    if (apiNotifications.isNotEmpty()) {
                        val items = apiNotifications.map { n ->
                            val prod = n.product
                            StockNotificationItem(
                                id = n.id,
                                productId = n.product_id,
                                productName = prod?.name ?: "Produk #${n.product_id}",
                                currentStock = prod?.stock_content ?: 0,
                                minStock = prod?.minimum_stock ?: 0,
                                unit = prod?.unit ?: "unit",
                                message = n.message.ifBlank { "Stok '${prod?.name}' menipis!" },
                                isRead = n.is_read
                            )
                        }
                        _uiState.update { it.copy(items = items, isLoading = false) }
                    } else {
                        // Fallback to local calculation if server list is empty
                        calculateLocalNotifications()
                    }
                },
                onFailure = {
                    calculateLocalNotifications()
                }
            )
        }
    }

    private suspend fun calculateLocalNotifications() {
        val products = productDao.getAllProductsSync()
        val lowStockItems = products.filter { it.stockContent <= it.minimumStock }.mapIndexed { index, product ->
            StockNotificationItem(
                id = index + 1,
                productId = product.id,
                productName = product.name,
                currentStock = product.stockContent,
                minStock = product.minimumStock,
                unit = product.unit,
                message = "Stok '${product.name}' menipis! Sisa ${product.stockContent} ${product.unit} (Batas Min: ${product.minimumStock})"
            )
        }
        _uiState.update { it.copy(items = lowStockItems, isLoading = false) }
    }

    fun markAsRead(id: Int) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
            val updated = _uiState.value.items.map {
                if (it.id == id) it.copy(isRead = true) else it
            }
            _uiState.update { it.copy(items = updated) }
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(id)
            val updated = _uiState.value.items.filterNot { it.id == id }
            _uiState.update { it.copy(items = updated) }
        }
    }
}
