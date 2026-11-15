package com.radityaf.depotify_wrz.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.BuildConfig
import com.radityaf.depotify_wrz.data.model.User
import com.radityaf.depotify_wrz.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    data class LoggedIn(val user: User) : AuthState
    data class VendorUnlinked(val message: String) : AuthState
    data class UnsupportedAccount(val message: String) : AuthState
    data class Error(val message: String) : AuthState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        // Mode Hardcode hanya diperbolehkan jika aplikasi di-build sebagai DEBUG
        var USE_HARDCODE_AUTH = false
        val isHardcodeAuthAllowed: Boolean get() = BuildConfig.DEBUG && USE_HARDCODE_AUTH
    }

    val tokenFlow = authRepository.tokenFlow

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        if (isHardcodeAuthAllowed) {
            _authState.value = AuthState.Idle
            return
        }
        viewModelScope.launch {
            val token = authRepository.tokenFlow.firstOrNull()
            if (!token.isNullOrEmpty()) {
                _authState.value = AuthState.Loading
                val result = authRepository.getMe()
                result.fold(
                    onSuccess = { user ->
                        validateUserRole(user)
                    },
                    onFailure = {
                        _authState.value = AuthState.Idle
                    }
                )
            } else {
                _authState.value = AuthState.Idle
            }
        }
    }

    fun login(email: String, pass: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Email dan kata sandi wajib diisi")
            return
        }

        if (!trimmedEmail.contains("@") || trimmedEmail.startsWith("@") || trimmedEmail.endsWith("@")) {
            _authState.value = AuthState.Error("Format email tidak valid (contoh: admin@depotify.com)")
            return
        }

        if (isHardcodeAuthAllowed) {
            viewModelScope.launch {
                _authState.value = AuthState.Loading
                kotlinx.coroutines.delay(400)
                when {
                    trimmedEmail.lowercase() == "owner@example.com" && pass == "12345" -> {
                        val mockUser = User(
                            id = 1,
                            name = "Owner",
                            email = "owner@example.com",
                            role = "owner",
                            vendor_id = null
                        )
                        validateUserRole(mockUser)
                    }
                    trimmedEmail.lowercase() == "vendor@example.com" && pass == "12345" -> {
                        val mockUser = User(
                            id = 2,
                            name = "Vendor",
                            email = "vendor@example.com",
                            role = "vendor",
                            vendor_id = 101
                        )
                        validateUserRole(mockUser)
                    }
                    else -> {
                        _authState.value = AuthState.Error("Email atau kata sandi salah")
                    }
                }
            }
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.login(trimmedEmail, pass)
            result.fold(
                onSuccess = { data ->
                    val user = data.user
                    if (user != null) {
                        validateUserRole(user)
                    } else {
                        _authState.value = AuthState.Error("Data pengguna tidak ditemukan")
                    }
                },
                onFailure = { err ->
                    _authState.value = AuthState.Error(err.message ?: "Gagal masuk. Periksa email dan kata sandi.")
                }
            )
        }
    }

    private fun validateUserRole(user: User) {
        val effectiveVendorId = user.effectiveVendorId
        val updatedUser = if (user.role == "vendor" && user.vendor_id == null && effectiveVendorId != null) {
            user.copy(vendor_id = effectiveVendorId)
        } else {
            user
        }

        when {
            updatedUser.role == "owner" || updatedUser.role == "staf" -> {
                _authState.value = AuthState.LoggedIn(updatedUser)
            }
            updatedUser.role == "vendor" -> {
                val finalUser = if (updatedUser.vendor_id == null) updatedUser.copy(vendor_id = updatedUser.id) else updatedUser
                _authState.value = AuthState.LoggedIn(finalUser)
            }
            else -> {
                _authState.value = AuthState.UnsupportedAccount("Peran akun '${updatedUser.role}' belum didukung di aplikasi mobile")
            }
        }
    }

    fun logout() {
        if (isHardcodeAuthAllowed) {
            _authState.value = AuthState.Idle
            return
        }
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Idle
        }
    }
}
