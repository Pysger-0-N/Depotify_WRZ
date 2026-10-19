package com.radityaf.depotify_wrz.ui.owner.po

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoFormScreen(
    modifier: Modifier = Modifier,
    viewModel: PurchaseOrderViewModel = hiltViewModel(),
    onSaved: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var selectedVendor by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    var vendorExpanded by remember { mutableStateOf(false) }
    var productExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
        topBar = { DepotifyTopBar("Buat PO", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PageHeader("Pembelian", "Purchase Order Baru", "Isi data pesanan ke vendor dari database lokal.")

            SectionCard(label = "Form", title = "Data Pembelian") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Vendor Dropdown
                    ExposedDropdownMenuBox(
                        expanded = vendorExpanded,
                        onExpandedChange = { vendorExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedVendor.ifBlank { "Pilih Vendor" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Vendor") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vendorExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DepotifyColors.Green,
                                unfocusedBorderColor = DepotifyColors.Border
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = vendorExpanded,
                            onDismissRequest = { vendorExpanded = false }
                        ) {
                            if (state.vendors.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Belum ada vendor di Room DB") },
                                    onClick = { vendorExpanded = false }
                                )
                            }
                            state.vendors.forEach { vendor ->
                                DropdownMenuItem(
                                    text = { Text(vendor.name) },
                                    onClick = {
                                        selectedVendor = vendor.name
                                        vendorExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Product Dropdown
                    ExposedDropdownMenuBox(
                        expanded = productExpanded,
                        onExpandedChange = { productExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedProduct.ifBlank { "Pilih Produk" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Produk") },
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
                                    text = { Text(prod.name) },
                                    onClick = {
                                        selectedProduct = prod.name
                                        price = prod.price.toString()
                                        productExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    DepotifyTextField("Jumlah (Qty)", qty, { qty = it.filter(Char::isDigit) }, placeholder = "0", keyboardType = KeyboardType.Number)
                    DepotifyTextField("Harga Satuan", price, { price = it.filter(Char::isDigit) }, placeholder = "0", keyboardType = KeyboardType.Number)
                }
            }

            DepotifyButton(
                text = "Simpan & Kembali",
                onClick = {
                    viewModel.create(selectedVendor, selectedProduct, qty.toIntOrNull() ?: 0, price.toLongOrNull() ?: 0L)
                    onSaved()
                },
                enabled = selectedVendor.isNotBlank() && selectedProduct.isNotBlank() && qty.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
