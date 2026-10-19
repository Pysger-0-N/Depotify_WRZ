package com.radityaf.depotify_wrz.ui.owner.vendor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.local.dao.VendorDao
import com.radityaf.depotify_wrz.data.local.entity.VendorEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VendorUiState(
    val items: List<VendorEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class VendorViewModel @Inject constructor(
    private val vendorDao: VendorDao
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<VendorUiState> = combine(
        vendorDao.getAllVendors(),
        _isLoading
    ) { items, isLoading ->
        VendorUiState(items = items, isLoading = isLoading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VendorUiState()
    )

    fun add(name: String, phone: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val entity = VendorEntity(name = name.trim(), phone = phone.trim())
            vendorDao.insertVendor(entity)
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            vendorDao.deleteVendorById(id)
        }
    }
}
