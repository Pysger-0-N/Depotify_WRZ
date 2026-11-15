package com.radityaf.depotify_wrz.ui.stock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.owner.sales.ErrorBanner
import com.radityaf.depotify_wrz.ui.owner.sales.SuccessBanner
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStockMovementScreen(
    modifier: Modifier = Modifier,
    initialProductId: Int? = null,
    viewModel: StockViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var selectedProductId by remember { mutableStateOf<Int?>(initialProductId) }
    var selectedProductName by remember { mutableStateOf("") }
    var productExpanded by remember { mutableStateOf(false) }

    var qty by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var stockType by remember { mutableStateOf("isi") }
    var movementType by remember { mutableStateOf("masuk") }

    LaunchedEffect(initialProductId, state.products) {
        if (initialProductId != null && selectedProductId == initialProductId) {
            val prod = state.products.find { it.id == initialProductId }
            if (prod != null) {
                selectedProductName = prod.name
            }
        }
    }

    val selectedProduct = state.products.find { it.id == selectedProductId }
    val parsedQty = qty.toIntOrNull() ?: 0
    val currentStock = if (stockType == "isi") (selectedProduct?.stockContent ?: 0) else (selectedProduct?.emptyStock ?: 0)
    val isStockExceeded = movementType == "keluar" && selectedProduct != null && parsedQty > currentStock

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { DepotifyTopBar("Tambah Pergerakan Stok", onBack) }
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
                label = if (state.isOnline) "Online" else "Offline",
                title = "Pergerakan Stok",
                subtitle = "Catat barang masuk, keluar, atau opname stok."
            )

            if (!state.errorMessage.isNullOrBlank()) {
                ErrorBanner(message = state.errorMessage!!, onDismiss = { viewModel.clearMessages() })
            }
            if (!state.successMessage.isNullOrBlank()) {
                SuccessBanner(message = state.successMessage!!, onDismiss = { viewModel.clearMessages() })
            }

            if (isStockExceeded) {
                ErrorBanner(
                    message = "Jumlah barang keluar ($parsedQty) melebihi stok ${if (stockType == "isi") "isi" else "kosong"} ($currentStock).",
                    onDismiss = {}
                )
            }

            SectionCard(label = "Form", title = "Pergerakan Stok") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = productExpanded,
                        onExpandedChange = { productExpanded = it }
                    ) {
                        Box(
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .clayInset(cornerRadius = 16.dp, backgroundColor = DepotifyColors.SurfaceInset)
                        ) {
                            TextField(
                                value = selectedProductName.ifBlank { "Pilih Produk" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Item / Produk", color = DepotifyColors.Navy, fontWeight = FontWeight.Bold) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = DepotifyColors.Navy,
                                    unfocusedTextColor = DepotifyColors.Navy
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        ExposedDropdownMenu(
                            expanded = productExpanded,
                            onDismissRequest = { productExpanded = false }
                        ) {
                            if (state.products.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Belum ada produk di database") },
                                    onClick = { productExpanded = false }
                                )
                            }
                            state.products.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text("${prod.name} (Isi: ${prod.stockContent}, Kosong: ${prod.emptyStock})") },
                                    onClick = {
                                        selectedProductId = prod.id
                                        selectedProductName = prod.name
                                        productExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text("Jenis Stok", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("isi" to "Stok Isi", "kosong" to "Stok Kosong").forEach { (key, label) ->
                            val selected = stockType == key
                            Box(
                                modifier = Modifier
                                    .then(
                                        if (selected) Modifier.clayButton(cornerRadius = 12.dp)
                                        else Modifier.clayInset(cornerRadius = 12.dp)
                                    )
                                    .clickable { stockType = key }
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

                    Text("Tipe Pergerakan", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("masuk" to "Masuk", "keluar" to "Keluar", "penyesuaian" to "Penyesuaian").forEach { (key, label) ->
                            val selected = movementType == key
                            Box(
                                modifier = Modifier
                                    .then(
                                        if (selected) Modifier.clayButton(cornerRadius = 12.dp)
                                        else Modifier.clayInset(cornerRadius = 12.dp)
                                    )
                                    .clickable { movementType = key }
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

                    DepotifyTextField("Jumlah (Qty)", qty, { qty = it.filter(Char::isDigit) }, keyboardType = KeyboardType.Number)
                    DepotifyTextField("Keterangan / Catatan", notes, { notes = it }, placeholder = "Alasan pergerakan stok (opsional)")
                }
            }

            DepotifyButton(
                text = if (state.isSubmitting) "Menyimpan ke Database..." else "Simpan Pergerakan Stok",
                onClick = {
                    val pId = selectedProductId
                    val pName = selectedProductName
                    if (pId != null && pName.isNotBlank() && parsedQty > 0 && !isStockExceeded) {
                        viewModel.addStockMovement(
                            productId = pId,
                            productName = pName,
                            stockType = stockType,
                            movementType = movementType,
                            qty = parsedQty,
                            description = notes,
                            onSuccess = { onBack() }
                        )
                    }
                },
                enabled = selectedProductId != null && parsedQty > 0 && !isStockExceeded && !state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
