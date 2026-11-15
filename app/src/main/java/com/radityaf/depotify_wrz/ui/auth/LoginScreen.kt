package com.radityaf.depotify_wrz.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radityaf.depotify_wrz.ui.components.ClayBackground
import com.radityaf.depotify_wrz.ui.components.SectionLabel
import com.radityaf.depotify_wrz.ui.components.clayButton
import com.radityaf.depotify_wrz.ui.components.clayCard
import com.radityaf.depotify_wrz.ui.components.clayInset
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun LoginScreen(
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsState()

    ClayBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Branding Clay Card Header (White Surface)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clayCard(cornerRadius = 28.dp, color = DepotifyColors.Surface)
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Inflated Mint Badge Logo Icon
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clayButton(cornerRadius = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "D",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = "Depotify",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DepotifyColors.Navy
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            SectionLabel("SISTEM PENGELOLAAN DEPOT")
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Kelola galon dan gas depot lebih praktis.",
                        fontSize = 13.sp,
                        color = DepotifyColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Form Section Floating Clay Card (White Surface)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clayCard(cornerRadius = 28.dp, color = DepotifyColors.Surface)
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Selamat Datang",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DepotifyColors.Navy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Masuk ke akun untuk mengelola depot.",
                        fontSize = 12.sp,
                        color = DepotifyColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Email Field (Clay Inset Input)
                    Text(
                        text = "Email",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DepotifyColors.Navy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clayInset(cornerRadius = 16.dp, backgroundColor = DepotifyColors.SurfaceInset)
                    ) {
                        TextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("Masukkan email", color = DepotifyColors.TextMuted, fontSize = 13.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = DepotifyColors.Navy,
                                unfocusedTextColor = DepotifyColors.Navy
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field (Clay Inset Input)
                    Text(
                        text = "Kata Sandi",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DepotifyColors.Navy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clayInset(cornerRadius = 16.dp, backgroundColor = DepotifyColors.SurfaceInset)
                    ) {
                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("Masukkan kata sandi", color = DepotifyColors.TextMuted, fontSize = 13.sp) },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Text(
                                        text = if (passwordVisible) "Sembunyi" else "Lihat",
                                        color = DepotifyColors.GreenDark,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = DepotifyColors.Navy,
                                unfocusedTextColor = DepotifyColors.Navy
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Clay Mint Button
                    val isLoading = authState is AuthState.Loading
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clayButton(cornerRadius = 26.dp, enabled = !isLoading)
                            .clickable(enabled = !isLoading) { viewModel.login(email, password) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Masuk",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    if (authState is AuthState.Error) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = DepotifyColors.Danger,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
