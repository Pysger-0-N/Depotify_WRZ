package com.radityaf.depotify_wrz.ui.vendor.product

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.data.model.User
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.owner.sales.ErrorBanner
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorProductFormScreen(
    user: User,
    modifier: Modifier = Modifier,
    productId: Int? = null,
    viewModel: VendorProductViewModel = hiltViewModel(),
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
    var stockContent by remember { mutableStateOf("0") }
    var minStock by remember { mutableStateOf("5") }
    var isActive by remember { mutableStateOf(true) }

    val sellingPriceVal = sellingPrice.toDoubleOrNull() ?: 0.0
    val purchasePriceVal = purchasePrice.toDoubleOrNull() ?: 0.0

    LaunchedEffect(productId) {
        if (productId != null && productId > 0) {
            val p = viewModel.getProductById(productId)
            p?.let { prod ->
                name = prod.name
                sku = prod.sku ?: ""
                variant = prod.variant ?: ""
                unit = prod.unit
                selectedCategoryId = prod.category_id
                selectedCategoryName = prod.category?.name ?: state.categories.find { it.id == prod.category_id }?.name ?: ""
                sellingPrice = prod.selling_price.toLong().toString()
                depositPrice = prod.deposit_price?.toLong()?.toString() ?: "0"
                purchasePrice = prod.purchase_price.toLong().toString()
                stockContent = prod.stock_content.toString()
                minStock = prod.minimum_stock.toString()
                isActive = prod.is_active
                usesContainer = prod.deposit_price != null && prod.deposit_price > 0.0
            }
        }
    }

    LaunchedEffect(state.categories, selectedCategoryId) {
        if (selectedCategoryId == null && state.categories.isNotEmpty()) {
            val first = state.categories.first()
            selectedCategoryId = first.id
            selectedCategoryName = first.name
        } else if (selectedCategoryId != null && selectedCategoryName.isBlank()) {
            val found = state.categories.find { it.id == selectedCategoryId }
            if (found != null) {
                selectedCategoryName = found.name
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { DepotifyTopBar(if (productId == null) "Tambah Produk" else "Ubah Produk", onBack) }
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
                "Katalog Produk",
                if (productId == null) "Form Produk Baru" else "Edit Data Produk",
                "Kelola informasi produk untuk vendor ${user.name}."
            )

            if (!state.errorMessage.isNullOrBlank()) {
                ErrorBanner(message = state.errorMessage!!) { viewModel.clearMessages() }
            }

            SectionCard(label = "Detail Utama", title = "Informasi Dasar Produk") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        Box(
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .clayInset(cornerRadius = 16.dp, backgroundColor = DepotifyColors.SurfaceInset)
                        ) {
                            TextField(
                                value = selectedCategoryName.ifBlank { "Pilih Kategori" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Kategori Produk", color = DepotifyColors.Navy, fontWeight = FontWeight.Bold) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
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
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            if (state.categories.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Belum ada kategori") },
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

                    DepotifyTextField("Nama Produk *", name, { name = it }, placeholder = "Contoh: Aqua Galon 19L")
                    DepotifyTextField("Kode SKU", sku, { sku = it }, placeholder = "Contoh: SUPP-AQUA-19L")
                    DepotifyTextField("Varian", variant, { variant = it }, placeholder = "Contoh: 19 Liter")

                    Text("Satuan Unit", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("canister" to "Kanister", "gallon" to "Galon", "pcs" to "Pcs", "other" to "Lainnya").forEach { (key, label) ->
                            val selected = unit == key
                            Box(
                                modifier = Modifier
                                    .then(
                                        if (selected) Modifier.clayButton(cornerRadius = 12.dp)
                                        else Modifier.clayInset(cornerRadius = 12.dp)
                                    )
                                    .clickable { unit = key }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    label,
                                    color = if (selected) Color.White else DepotifyColors.Navy,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            SectionCard(label = "Pengaturan Harga", title = "Harga Jual dan Modal") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Menggunakan Tabung Deposit", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                        Switch(
                            checked = usesContainer,
                            onCheckedChange = { usesContainer = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = DepotifyColors.Green)
                        )
                    }

                    if (usesContainer) {
                        DepotifyTextField("Harga Deposit Tabung", depositPrice, { depositPrice = it.filter(Char::isDigit) }, keyboardType = KeyboardType.Number)
                    }

                    DepotifyTextField("Harga Modal / Pasokan *", purchasePrice, { purchasePrice = it.filter(Char::isDigit) }, keyboardType = KeyboardType.Number)
                    DepotifyTextField("Harga Jual Eceran", sellingPrice, { sellingPrice = it.filter(Char::isDigit) }, keyboardType = KeyboardType.Number)
                }
            }

            SectionCard(label = "Stok dan Status", title = "Pengaturan Stok Awal") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DepotifyTextField("Stok Awal Tersedia", stockContent, { stockContent = it.filter(Char::isDigit) }, keyboardType = KeyboardType.Number)
                    DepotifyTextField("Batas Stok Minimum", minStock, { minStock = it.filter(Char::isDigit) }, keyboardType = KeyboardType.Number)

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status Produk Aktif", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = DepotifyColors.Green)
                        )
                    }
                }
            }

            DepotifyButton(
                text = if (state.isSubmitting) "Menyimpan..." else "Simpan produk",
                onClick = {
                    viewModel.saveProduct(
                        vendorId = user.effectiveVendorId,
                        productId = productId,
                        categoryId = selectedCategoryId ?: 1,
                        name = name,
                        sku = sku,
                        variant = variant,
                        unit = unit,
                        sellingPrice = sellingPriceVal,
                        depositPrice = if (usesContainer) depositPrice.toDoubleOrNull() else null,
                        purchasePrice = purchasePriceVal,
                        stockContent = stockContent.toIntOrNull() ?: 0,
                        minimumStock = minStock.toIntOrNull() ?: 5,
                        isActive = isActive,
                        onSuccess = onBack
                    )
                },
                enabled = !state.isSubmitting && name.isNotBlank() && purchasePriceVal >= 0,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
