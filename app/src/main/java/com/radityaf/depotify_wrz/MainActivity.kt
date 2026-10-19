package com.radityaf.depotify_wrz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.radityaf.depotify_wrz.ui.auth.AuthState
import com.radityaf.depotify_wrz.ui.auth.AuthViewModel
import com.radityaf.depotify_wrz.ui.auth.LoginScreen
import com.radityaf.depotify_wrz.ui.main.MainScreen
import com.radityaf.depotify_wrz.ui.unsupported.UnsupportedScreen
import com.radityaf.depotify_wrz.ui.vendor.VendorMainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val authState by authViewModel.authState.collectAsState()

            when (val state = authState) {
                is AuthState.LoggedIn -> {
                    if (state.user.role == "vendor") {
                        VendorMainScreen(
                            user = state.user,
                            onLogout = { authViewModel.logout() }
                        )
                    } else {
                        MainScreen(
                            user = state.user,
                            onLogout = { authViewModel.logout() }
                        )
                    }
                }

                is AuthState.VendorUnlinked ->
                    UnsupportedScreen(message = state.message, onLogout = { authViewModel.logout() })

                is AuthState.UnsupportedAccount ->
                    UnsupportedScreen(message = state.message, onLogout = { authViewModel.logout() })

                // Idle, Loading, Error -> tetap di halaman login
                else -> LoginScreen(viewModel = authViewModel)
            }
        }
    }
}

