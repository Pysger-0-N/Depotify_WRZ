package com.radityaf.depotify_wrz.ui.owner.category

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.owner.sales.ErrorBanner
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun CategoryFormScreen(
    modifier: Modifier = Modifier,
    categoryId: Int? = null,
    viewModel: CategoryViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val cleanCode = code.trim().uppercase()
    val isCodeValid = cleanCode.isBlank() || (cleanCode.length in 2..10)

    LaunchedEffect(categoryId) {
        if (categoryId != null) {
            viewModel.loadCategory(categoryId)
        } else {
            viewModel.clearSelectedCategory()
        }
    }

    LaunchedEffect(state.selectedCategory) {
        state.selectedCategory?.let { category ->
            name = category.name
            code = category.code
            description = category.description ?: ""
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { DepotifyTopBar(if (categoryId == null) "Tambah Kategori" else "Ubah Kategori", onBack) }
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
                if (categoryId == null) "Form Kategori Baru" else "Edit Kategori #$categoryId",
                "Kelompokkan jenis produk depot Anda."
            )

            if (!isCodeValid) {
                ErrorBanner(
                    message = "Kode kategori harus terdiri dari 2 hingga 10 karakter!",
                    onDismiss = {}
                )
            }

            SectionCard(label = "Form", title = "Data Kategori") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DepotifyTextField(
                        label = "Nama Kategori",
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Contoh: Air Mineral"
                    )
                    DepotifyTextField(
                        label = "Kode Kategori",
                        value = code,
                        onValueChange = { code = it.take(10).uppercase() },
                        placeholder = "Maks. 10 karakter (otomatis jika kosong)"
                    )
                    DepotifyTextField(
                        label = "Deskripsi",
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Deskripsi singkat kategori"
                    )
                }
            }

            DepotifyButton(
                text = if (categoryId == null) "Tambah Kategori" else "Simpan Perubahan",
                onClick = {
                    viewModel.saveCategory(categoryId, name, cleanCode, description)
                    onBack()
                },
                enabled = name.isNotBlank() && isCodeValid,
                modifier = Modifier.fillMaxWidth()
            )

            if (categoryId != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clayInset(cornerRadius = 24.dp, backgroundColor = DepotifyColors.WarningSoft)
                        .clickable { showDeleteDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Hapus Kategori", color = DepotifyColors.Danger, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            }
        }
    }

    if (showDeleteDialog && categoryId != null) {
        DepotifyConfirmDialog(
            title = "Hapus Kategori",
            message = "Hapus kategori '${name.ifBlank { "ini" }}'?",
            confirmText = "Hapus",
            onConfirm = {
                viewModel.delete(categoryId)
                showDeleteDialog = false
                onBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
