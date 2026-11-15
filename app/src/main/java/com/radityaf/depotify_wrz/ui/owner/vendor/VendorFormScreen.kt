package com.radityaf.depotify_wrz.ui.owner.vendor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.owner.sales.ErrorBanner
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun VendorFormScreen(
    modifier: Modifier = Modifier,
    vendorId: Int? = null,
    viewModel: VendorViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val phoneClean = phone.filter(Char::isDigit)
    val isPhoneValid = phoneClean.isBlank() || (
        phoneClean.length in 8..15 && (phoneClean.startsWith("0") || phoneClean.startsWith("62"))
    )

    val isPasswordValid = vendorId != null || (password.length >= 8 && password == passwordConfirmation)
    val isEmailValid = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isFormValid = name.isNotBlank() && isEmailValid && isPhoneValid && isPasswordValid

    LaunchedEffect(vendorId) {
        if (vendorId != null) {
            viewModel.loadVendor(vendorId)
        } else {
            viewModel.clearSelectedVendor()
        }
    }

    LaunchedEffect(state.selectedVendor) {
        state.selectedVendor?.let { vendor ->
            name = vendor.name
            phone = vendor.phone ?: ""
            address = vendor.address ?: ""
            email = vendor.email ?: ""
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { DepotifyTopBar(if (vendorId == null) "Tambah Supplier" else "Ubah Supplier", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader(
                "Master Data",
                if (vendorId == null) "Tambah Supplier" else "Edit Supplier #$vendorId",
                "Data supplier dan akun login dikelola bersamaan oleh owner."
            )

            if (!isPhoneValid) {
                ErrorBanner(
                    message = "Nomor telepon/HP supplier harus terdiri dari 8 hingga 15 digit dan diawali angka '0' atau '62'!",
                    onDismiss = {}
                )
            }

            if (password.isNotEmpty() && password.length < 8) {
                ErrorBanner(
                    message = "Kata sandi awal minimal 8 karakter!",
                    onDismiss = {}
                )
            }

            if (password.isNotEmpty() && passwordConfirmation.isNotEmpty() && password != passwordConfirmation) {
                ErrorBanner(
                    message = "Konfirmasi kata sandi tidak sama!",
                    onDismiss = {}
                )
            }

            state.errorMessage?.let { err ->
                ErrorBanner(message = err, onDismiss = {})
            }

            SectionCard(label = "Form", title = "Informasi Supplier") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DepotifyTextField(
                        label = "Nama supplier *",
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Contoh: PT Sumber Gas"
                    )
                    DepotifyTextField(
                        label = "Nomor telepon",
                        value = phone,
                        onValueChange = { phone = it.filter(Char::isDigit) },
                        placeholder = "Nomor telepon supplier",
                        keyboardType = KeyboardType.Phone
                    )
                    DepotifyTextField(
                        label = "Alamat",
                        value = address,
                        onValueChange = { address = it },
                        placeholder = "Alamat supplier"
                    )
                }
            }

            SectionCard(label = "Akun", title = "Akun Login Supplier") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Gunakan email ini untuk login ke Depotify.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    DepotifyTextField(
                        label = "Email login *",
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "supplier@email.com",
                        keyboardType = KeyboardType.Email
                    )
                    if (vendorId == null) {
                        DepotifyTextField(
                            label = "Kata sandi awal *",
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Minimal 8 karakter",
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardType = KeyboardType.Password
                        )
                        DepotifyTextField(
                            label = "Konfirmasi kata sandi *",
                            value = passwordConfirmation,
                            onValueChange = { passwordConfirmation = it },
                            placeholder = "Ulangi kata sandi",
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardType = KeyboardType.Password
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clayButton(cornerRadius = 24.dp)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Batal", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                }

                DepotifyButton(
                    text = if (vendorId == null) "Simpan Supplier & Akun" else "Simpan Perubahan",
                    onClick = {
                        viewModel.saveVendor(
                            id = vendorId,
                            name = name,
                            phone = phone,
                            email = email,
                            address = address,
                            password = password,
                            passwordConfirmation = passwordConfirmation,
                            onSuccess = { onBack() }
                        )
                    },
                    enabled = isFormValid && !state.isLoading,
                    modifier = Modifier.weight(1.5f)
                )
            }

            if (vendorId != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clayInset(cornerRadius = 24.dp, backgroundColor = DepotifyColors.WarningSoft)
                        .clickable { showDeleteDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Hapus Vendor", color = DepotifyColors.Danger, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            }
        }
    }

    if (showDeleteDialog && vendorId != null) {
        DepotifyConfirmDialog(
            title = "Hapus Supplier",
            message = "Hapus supplier '${name.ifBlank { "ini" }}'?",
            confirmText = "Hapus",
            onConfirm = {
                viewModel.delete(vendorId)
                showDeleteDialog = false
                onBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
