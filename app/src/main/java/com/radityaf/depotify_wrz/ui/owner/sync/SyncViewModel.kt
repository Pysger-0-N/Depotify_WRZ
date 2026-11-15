package com.radityaf.depotify_wrz.ui.owner.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = syncRepository.isOnlineFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), syncRepository.isConnected())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    init {
        syncRepository.startAutoSyncListener(viewModelScope)
    }

    fun syncFullDatabase() {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = null
            val result = syncRepository.syncFullTwoWay()
            _isLoading.value = false
            result.onSuccess {
                _isSuccess.value = true
                _message.value = "Sinkronisasi selesai. Data lokal dan server sudah teratur."
            }.onFailure { e ->
                _isSuccess.value = false
                _message.value = e.message ?: "Gagal sinkronisasi data dengan server."
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
