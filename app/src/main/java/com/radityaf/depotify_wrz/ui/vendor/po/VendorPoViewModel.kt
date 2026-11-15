package com.radityaf.depotify_wrz.ui.vendor.po

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.local.PurchaseOrderStore
import com.radityaf.depotify_wrz.data.repository.PurchaseOrderRepository
import com.radityaf.depotify_wrz.ui.owner.po.PurchaseOrder
import com.radityaf.depotify_wrz.ui.owner.po.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VendorPoUiState(
    val items: List<PurchaseOrder> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class VendorPoViewModel @Inject constructor(
    private val purchaseOrderStore: PurchaseOrderStore,
    private val purchaseOrderRepository: PurchaseOrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VendorPoUiState())
    val uiState: StateFlow<VendorPoUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            purchaseOrderStore.poList.collect { list ->
                _uiState.update { it.copy(items = list) }
            }
        }
        fetchServerPurchaseOrders()
    }

    fun fetchServerPurchaseOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = purchaseOrderRepository.getPurchaseOrders()
            result.fold(
                onSuccess = { apiResponse ->
                    val uiPos = apiResponse.data.map { it.toUiModel() }
                    purchaseOrderStore.setPos(uiPos)
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Gagal memuat Purchase Orders dari server") }
                }
            )
        }
    }

    fun getById(id: Int): PurchaseOrder? = _uiState.value.items.find { it.id == id }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun refresh() {
        fetchServerPurchaseOrders()
    }

    fun confirmPo(id: Int, notes: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = purchaseOrderRepository.updatePoStatus(id, "confirmed", notes)
            result.fold(
                onSuccess = { apiPo ->
                    val uiPo = apiPo.toUiModel()
                    purchaseOrderStore.updateStatus(id, uiPo.status)
                    fetchServerPurchaseOrders()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "PO #$id berhasil dikonfirmasi (Status: Dikelola)!"
                        )
                    }
                },
                onFailure = { error ->
                    val msg = error.message ?: ""
                    fetchServerPurchaseOrders()
                    if (msg.contains("ke 'confirmed'", ignoreCase = true)) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "PO #$id sudah berstatus Dikelola di server!"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Gagal mengonfirmasi PO #$id: $msg"
                            )
                        }
                    }
                }
            )
        }
    }

    fun rejectPo(id: Int, notes: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = purchaseOrderRepository.updatePoStatus(id, "rejected", notes)
            result.fold(
                onSuccess = { apiPo ->
                    val uiPo = apiPo.toUiModel()
                    purchaseOrderStore.updateStatus(id, uiPo.status)
                    fetchServerPurchaseOrders()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "PO #$id telah ditolak."
                        )
                    }
                },
                onFailure = { error ->
                    val msg = error.message ?: ""
                    fetchServerPurchaseOrders()
                    if (msg.contains("ke 'rejected'", ignoreCase = true)) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "PO #$id sudah berstatus Ditolak di server."
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Gagal menolak PO #$id: $msg"
                            )
                        }
                    }
                }
            )
        }
    }

    fun shipPo(id: Int, notes: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = purchaseOrderRepository.updatePoStatus(id, "shipped", notes)
            result.fold(
                onSuccess = { apiPo ->
                    val uiPo = apiPo.toUiModel()
                    purchaseOrderStore.updateStatus(id, uiPo.status)
                    fetchServerPurchaseOrders()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "PO #$id ditandai Dikirim ke depot Owner!"
                        )
                    }
                },
                onFailure = { error ->
                    val msg = error.message ?: ""
                    fetchServerPurchaseOrders()
                    if (msg.contains("ke 'shipped'", ignoreCase = true)) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "PO #$id sudah berstatus Dikirim di server!"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Gagal mengubah status PO #$id ke Dikirim: $msg"
                            )
                        }
                    }
                }
            )
        }
    }
}
