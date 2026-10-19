package com.radityaf.depotify_wrz.ui.stock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStockMovementScreen(
    modifier: Modifier = Modifier,
    viewModel: StockViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var selectedProductId by remember { mutableStateOf<Int?>(null) }
    var selectedProductName by remember { mutableStateOf("") }
    var productExpanded by remember { mutableStateOf(false) }

    var qty by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var stockType by remember { mutableStateOf("isi") } // "isi" or "kosong"
    var movementType by remember { mutableStateOf("masuk") } // "masuk", "keluar", "penyesuaian"

    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
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
            PageHeader("Inventaris", "Pergerakan Stok Manual", "Catat barang masuk, keluar, atau opname stok secara offline.")

            SectionCard(label = "Form", title = "Input Pergerakan Stok") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Product Dropdown
                    ExposedDropdownMenuBox(
                        expanded = productExpanded,
                        onExpandedChange = { productExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedProductName.ifBlank { "Pilih Produk" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Item / Produk") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DepotifyColors.Green,
                                unfocusedBorderColor = DepotifyColors.Border
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = productExpanded,
                            onDismissRequest = { productExpanded = false }
                        ) {
                            if (state.products.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Belum ada produk di Room DB") },
                                    onClick = { productExpanded = false }
                                )
                            }
                            state.products.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text("${prod.name} (Stok: ${prod.stock})") },
                                    onClick = {
                                        selectedProductId = prod.id
                                        selectedProductName = prod.name
                                        productExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text("Jenis Stok", fontWeight = FontWeight.Medium, color = DepotifyColors.Navy)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("isi" to "Stok Isi", "kosong" to "Stok Kosong").forEach { (key, label) ->
                            OutlinedButton(
                                onClick = { stockType = key },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (stockType == key) DepotifyColors.GreenSoft else DepotifyColors.Surface,
                                    contentColor = if (stockType == key) DepotifyColors.GreenDark else DepotifyColors.Navy
                                )
                            ) { Text(label) }
                        }
                    }

                    Text("Tipe Pergerakan", fontWeight = FontWeight.Medium, color = DepotifyColors.Navy)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("masuk" to "Masuk", "keluar" to "Keluar", "penyesuaian" to "Penyesuaian").forEach { (key, label) ->
                            OutlinedButton(
                                onClick = { movementType = key },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (movementType == key) DepotifyColors.GreenSoft else DepotifyColors.Surface,
                                    contentColor = if (movementType == key) DepotifyColors.GreenDark else DepotifyColors.Navy
                                )
                            ) { Text(label) }
                        }
                    }

                    DepotifyTextField("Jumlah (Qty)", qty, { qty = it.filter(Char::isDigit) }, keyboardType = KeyboardType.Number)
                    DepotifyTextField("Keterangan / Catatan", notes, { notes = it }, placeholder = "Alasan pergerakan stok")
                }
            }

            DepotifyButton(
                text = "Simpan Pergerakan Stok",
                onClick = {
                    val pId = selectedProductId
                    val pName = selectedProductName
                    val q = qty.toIntOrNull() ?: 0
                    if (pId != null && pName.isNotBlank() && q > 0) {
                        viewModel.addStockMovement(
                            productId = pId,
                            productName = pName,
                            stockType = stockType,
                            movementType = movementType,
                            qty = q,
                            description = notes,
                            onSuccess = { onBack() }
                        )
                    }
                },
                enabled = selectedProductId != null && qty.toIntOrNull() ?: 0 > 0,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
