package com.radityaf.depotify_wrz.ui.owner.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    modifier: Modifier = Modifier,
    productId: Int? = null,
    viewModel: ProductFormViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedCategoryName by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var variant by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("canister") }
    var usesContainer by remember { mutableStateOf(false) }
    var sellingPrice by remember { mutableStateOf("0") }
    var depositPrice by remember { mutableStateOf("0") }
    var purchasePrice by remember { mutableStateOf("0") }
    var minStock by remember { mutableStateOf("5") }
    var isActive by remember { mutableStateOf(true) }

    LaunchedEffect(productId) {
        if (productId != null) {
            viewModel.loadProduct(productId)
        }
    }

    LaunchedEffect(state.product) {
        state.product?.let { p ->
            name = p.name
            sku = p.sku ?: ""
            variant = p.variant ?: ""
            unit = p.unit
            selectedCategoryId = p.categoryId
            selectedCategoryName = p.categoryName
            sellingPrice = p.sellingPrice.toString()
            depositPrice = p.depositPrice?.toString() ?: "0"
            purchasePrice = p.purchasePrice.toString()
            minStock = p.minimumStock.toString()
            isActive = p.isActive
            usesContainer = p.depositPrice != null && p.depositPrice > 0.0
        }
    }

    LaunchedEffect(state.categories) {
        if (selectedCategoryId == null && state.categories.isNotEmpty()) {
            val first = state.categories.first()
            selectedCategoryId = first.id
            selectedCategoryName = first.name
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
        topBar = { DepotifyTopBar(if (productId == null) "Tambah Produk (Room)" else "Ubah Produk (Room)", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader("Manajemen Produk", if (productId == null) "Form Produk Baru" else "Edit Data Produk", "Disimpan secara offline di Room Database.")

            if (!state.errorMessage.isNullOrBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = DepotifyColors.Danger.copy(alpha = 0.1f))) {
                    Text(text = state.errorMessage!!, color = DepotifyColors.Danger, modifier = Modifier.padding(12.dp))
                }
            }

            SectionCard(label = "Detail Utama", title = "Informasi Dasar") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategoryName.ifBlank { "Pilih Kategori" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kategori Produk") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DepotifyColors.Green,
                                unfocusedBorderColor = DepotifyColors.Border
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            if (state.categories.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Belum ada kategori di Room DB") },
                                    onClick = { categoryExpanded = false }
                                )
                            }
                            state.categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        selectedCategoryId = cat.id
                                        selectedCategoryName = cat.name
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    DepotifyTextField("Nama Produk *", name, { name = it }, placeholder = "Contoh: LPG 3 Kg")
                    DepotifyTextField("SKU / Kode", sku, { sku = it }, placeholder = "Contoh: LPG-03")
                    DepotifyTextField("Varian", variant, { variant = it }, placeholder = "Contoh: Regular / Refill")
                    
                    Text("Unit / Satuan", fontWeight = FontWeight.Medium, color = DepotifyColors.Navy)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("canister" to "Kanister", "gallon" to "Galon", "pcs" to "Pcs", "other" to "Lainnya").forEach { (key, label) ->
                            OutlinedButton(
                                onClick = { unit = key },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (unit == key) DepotifyColors.GreenSoft else DepotifyColors.Surface,
                                    contentColor = if (unit == key) DepotifyColors.GreenDark else DepotifyColors.Navy
                                )
                            ) { Text(label) }
                        }
                    }
                }
            }

            SectionCard(label = "Kontainer & Harga", title = "Pengaturan Harga & Tabung") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Menggunakan Tabung / Galon (Deposit)?", fontWeight = FontWeight.Medium, fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                        Switch(
                            checked = usesContainer,
                            onCheckedChange = { usesContainer = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = DepotifyColors.Green)
                        )
                    }

                    if (usesContainer) {
                        DepotifyTextField("Harga Deposit", depositPrice, { depositPrice = it }, keyboardType = KeyboardType.Number)
                    }

                    DepotifyTextField("Harga Jual", sellingPrice, { sellingPrice = it }, keyboardType = KeyboardType.Number)
                    DepotifyTextField("Harga Beli / Modal", purchasePrice, { purchasePrice = it }, keyboardType = KeyboardType.Number)
                }
            }

            SectionCard(label = "Stok", title = "Batas Minimum Stok") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DepotifyTextField("Batas Stok Minimum", minStock, { minStock = it }, keyboardType = KeyboardType.Number)
                }
            }

            SectionCard(label = "Status", title = "Status Produk") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Produk Aktif", fontWeight = FontWeight.Medium, fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = DepotifyColors.Green)
                        )
                    }
                }
            }

            DepotifyButton(
                text = if (state.isLoading) "Menyimpan..." else "Simpan ke Room DB",
                onClick = {
                    viewModel.saveProduct(
                        productId = productId,
                        categoryId = selectedCategoryId ?: 1,
                        categoryName = selectedCategoryName,
                        name = name,
                        sku = sku,
                        variant = variant,
                        unit = unit,
                        sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                        depositPrice = if (usesContainer) depositPrice.toDoubleOrNull() else null,
                        purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                        minimumStock = minStock.toIntOrNull() ?: 5,
                        isActive = isActive,
                        onSuccess = onBack
                    )
                },
                enabled = !state.isLoading && name.isNotBlank() && selectedCategoryName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
