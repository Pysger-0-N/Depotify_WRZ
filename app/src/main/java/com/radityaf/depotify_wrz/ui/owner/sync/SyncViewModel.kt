package com.radityaf.depotify_wrz.ui.owner.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    fun syncData() {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = null
            val result = syncRepository.syncRoomToServer()
            _isLoading.value = false
            result.onSuccess {
                _isSuccess.value = true
                _message.value = "Berhasil mengirim data Room Database ke Server Database!"
            }.onFailure { e ->
                _isSuccess.value = false
                _message.value = e.message ?: "Gagal sinkronisasi data ke server"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
