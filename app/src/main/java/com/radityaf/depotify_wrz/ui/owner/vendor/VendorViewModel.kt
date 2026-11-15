package com.radityaf.depotify_wrz.ui.owner.vendor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.local.dao.PendingDeleteDao
import com.radityaf.depotify_wrz.data.local.dao.VendorDao
import com.radityaf.depotify_wrz.data.local.entity.PendingDeleteEntity
import com.radityaf.depotify_wrz.data.local.entity.VendorEntity
import com.radityaf.depotify_wrz.data.model.CreateVendorRequest
import com.radityaf.depotify_wrz.data.repository.VendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

data class VendorUiState(
    val items: List<VendorEntity> = emptyList(),
    val isLoading: Boolean = false,
    val selectedVendor: VendorEntity? = null,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class VendorViewModel @Inject constructor(
    private val vendorDao: VendorDao,
    private val pendingDeleteDao: PendingDeleteDao,
    private val vendorRepository: VendorRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _selectedVendor = MutableStateFlow<VendorEntity?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isSuccess = MutableStateFlow(false)

    val uiState: StateFlow<VendorUiState> = combine(
        vendorDao.getAllVendors(),
        _isLoading,
        _selectedVendor,
        _errorMessage,
        _isSuccess
    ) { items, isLoading, selectedVendor, errorMessage, isSuccess ->
        VendorUiState(
            items = items,
            isLoading = isLoading,
            selectedVendor = selectedVendor,
            errorMessage = errorMessage,
            isSuccess = isSuccess
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VendorUiState()
    )

    init {
        refreshFromApi()
    }

    fun refreshFromApi() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = vendorRepository.getVendors()
            if (result.isSuccess) {
                val vendors = result.getOrNull() ?: emptyList()
                vendors.forEach { ven ->
                    vendorDao.insertVendor(
                        VendorEntity(
                            id = ven.id,
                            userId = ven.user_id,
                            name = ven.name,
                            phone = ven.phone,
                            email = ven.email,
                            address = ven.address,
                            isActive = ven.is_active,
                            isSynced = true,
                            isDeleted = false
                        )
                    )
                }
            }
            _isLoading.value = false
        }
    }

    fun loadVendor(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedVendor.value = vendorDao.getVendorById(id)
            _isLoading.value = false
        }
    }

    fun clearSelectedVendor() {
        _selectedVendor.value = null
        _errorMessage.value = null
        _isSuccess.value = false
    }

    fun saveVendor(
        id: Int?,
        name: String,
        phone: String,
        email: String,
        address: String,
        password: String? = null,
        passwordConfirmation: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        if (name.isBlank() || email.isBlank()) {
            _errorMessage.value = "Nama dan Email login wajib diisi!"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isSuccess.value = false
            try {
                val request = CreateVendorRequest(
                    name = name.trim(),
                    contact_person = name.trim(),
                    phone = phone.trim().ifBlank { null },
                    email = email.trim(),
                    account_email = email.trim(),
                    address = address.trim().ifBlank { null },
                    password = password?.ifBlank { null },
                    account_password = password?.ifBlank { null },
                    password_confirmation = passwordConfirmation?.ifBlank { null },
                    account_password_confirmation = passwordConfirmation?.ifBlank { null },
                    is_active = true
                )

                if (id != null && id > 0) {
                    val result = vendorRepository.updateVendor(id, request)
                    if (result.isSuccess) {
                        val vendor = result.getOrNull()
                        val entity = VendorEntity(
                            id = id,
                            userId = vendor?.user_id,
                            name = name.trim(),
                            phone = phone.trim().ifBlank { null },
                            email = email.trim(),
                            address = address.trim().ifBlank { null },
                            isActive = true,
                            isSynced = true
                        )
                        vendorDao.updateVendor(entity)
                        _isSuccess.value = true
                        onSuccess()
                    } else {
                        val exc = result.exceptionOrNull()
                        if (exc is UnknownHostException || exc is ConnectException) {
                            val existing = vendorDao.getVendorById(id)
                            val entity = VendorEntity(
                                id = id,
                                userId = existing?.userId,
                                name = name.trim(),
                                phone = phone.trim().ifBlank { null },
                                email = email.trim(),
                                address = address.trim().ifBlank { null },
                                isActive = true,
                                isSynced = false
                            )
                            vendorDao.updateVendor(entity)
                            _isSuccess.value = true
                            onSuccess()
                        } else {
                            _errorMessage.value = exc?.message ?: "Gagal memperbarui vendor di server database."
                        }
                    }
                } else {
                    val result = vendorRepository.createVendor(request)
                    if (result.isSuccess) {
                        val vendor = result.getOrNull()
                        val entity = VendorEntity(
                            id = vendor?.id ?: 0,
                            userId = vendor?.user_id,
                            name = name.trim(),
                            phone = phone.trim().ifBlank { null },
                            email = email.trim(),
                            address = address.trim().ifBlank { null },
                            isActive = true,
                            isSynced = true
                        )
                        if (entity.id > 0) {
                            vendorDao.insertVendor(entity)
                        } else {
                            vendorDao.insertVendor(
                                VendorEntity(
                                    name = name.trim(),
                                    phone = phone.trim().ifBlank { null },
                                    email = email.trim(),
                                    address = address.trim().ifBlank { null },
                                    isActive = true,
                                    isSynced = false
                                )
                            )
                        }
                        _isSuccess.value = true
                        onSuccess()
                    } else {
                        val exc = result.exceptionOrNull()
                        if (exc is UnknownHostException || exc is ConnectException) {
                            vendorDao.insertVendor(
                                VendorEntity(
                                    name = name.trim(),
                                    phone = phone.trim().ifBlank { null },
                                    email = email.trim(),
                                    address = address.trim().ifBlank { null },
                                    isActive = true,
                                    isSynced = false
                                )
                            )
                            _isSuccess.value = true
                            onSuccess()
                        } else {
                            _errorMessage.value = exc?.message ?: "Gagal membuat vendor di server database."
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is UnknownHostException || e is ConnectException) {
                    try {
                        val entity = VendorEntity(
                            id = id ?: 0,
                            name = name.trim(),
                            phone = phone.trim().ifBlank { null },
                            email = email.trim(),
                            address = address.trim().ifBlank { null },
                            isActive = true,
                            isSynced = false
                        )
                        if (id != null && id > 0) {
                            vendorDao.updateVendor(entity)
                        } else {
                            vendorDao.insertVendor(entity)
                        }
                        _isSuccess.value = true
                        onSuccess()
                    } catch (ex: Exception) {
                        _errorMessage.value = ex.message ?: e.message ?: "Gagal menyimpan vendor"
                    }
                } else {
                    _errorMessage.value = e.message ?: "Gagal menyimpan vendor di server"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            val res = try {
                vendorRepository.deleteVendor(id)
            } catch (e: Exception) {
                Result.failure(e)
            }
            if (res.isFailure) {
                if (id > 0) {
                    pendingDeleteDao.insertPendingDelete(
                        PendingDeleteEntity(entityType = "vendor", serverId = id)
                    )
                }
            }
            vendorDao.deleteVendorById(id)
        }
    }
}
