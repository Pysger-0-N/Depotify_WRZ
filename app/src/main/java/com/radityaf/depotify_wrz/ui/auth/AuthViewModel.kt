package com.radityaf.depotify_wrz.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        // Toggle switch: true = Hardcode Mock Mode, false = Real API Mode
        var USE_HARDCODE_AUTH = true
    }

    val tokenFlow = authRepository.tokenFlow

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        if (USE_HARDCODE_AUTH) {
            _authState.value = AuthState.Idle
            return
        }
        viewModelScope.launch {
            authRepository.tokenFlow.collect { token ->
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
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Email dan password wajib diisi")
            return
        }

        if (USE_HARDCODE_AUTH) {
            viewModelScope.launch {
                _authState.value = AuthState.Loading
                kotlinx.coroutines.delay(400) // Simulate network delay
                when {
                    email.trim().lowercase() == "owner@example.com" && pass == "12345" -> {
                        val mockUser = User(
                            id = 1,
                            name = "Owner",
                            email = "owner@example.com",
                            role = "owner",
                            vendor_id = null
                        )
                        validateUserRole(mockUser)
                    }
                    email.trim().lowercase() == "vendor@example.com" && pass == "12345" -> {
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
                        _authState.value = AuthState.Error("Email atau password salah (Hardcore Mode)")
                    }
                }
            }
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.login(email, pass)
            result.fold(
                onSuccess = { data ->
                    validateUserRole(data.user)
                },
                onFailure = { err ->
                    _authState.value = AuthState.Error(err.localizedMessage ?: "Login gagal")
                }
            )
        }
    }

    private fun validateUserRole(user: User) {
        when {
            user.role == "owner" || user.role == "staf" -> {
                _authState.value = AuthState.LoggedIn(user)
            }
            user.role == "vendor" -> {
                if (user.vendor_id != null) {
                    _authState.value = AuthState.LoggedIn(user)
                } else {
                    _authState.value = AuthState.VendorUnlinked("Akun vendor belum terhubung, hubungi admin")
                }
            }
            else -> {
                _authState.value = AuthState.UnsupportedAccount("Akun ini belum didukung di aplikasi mobile")
            }
        }
    }

    fun logout() {
        if (USE_HARDCODE_AUTH) {
            _authState.value = AuthState.Idle
            return
        }
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Idle
        }
    }
}
