package com.radityaf.depotify_wrz.ui.owner.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.model.SaleTransaction
import com.radityaf.depotify_wrz.data.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val salesRepository: SalesRepository
) : ViewModel() {

    private val _sales = MutableStateFlow<List<SaleTransaction>>(emptyList())
    val sales: StateFlow<List<SaleTransaction>> = _sales.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadSales()
    }

    fun loadSales(status: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = salesRepository.getSales(status)
            _isLoading.value = false
            result.onSuccess { list ->
                _sales.value = list
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Gagal memuat penjualan"
            }
        }
    }

    fun confirmSale(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = salesRepository.confirmSale(id)
            _isLoading.value = false
            result.onSuccess {
                _successMessage.value = "Transaksi berhasil dikonfirmasi (tatap muka)"
                loadSales()
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Gagal konfirmasi transaksi"
            }
        }
    }

    fun completeSale(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = salesRepository.completeSale(id)
            _isLoading.value = false
            result.onSuccess {
                _successMessage.value = "Transaksi selesai dan tercatat ke database"
                loadSales()
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Gagal menyelesaikan transaksi"
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
