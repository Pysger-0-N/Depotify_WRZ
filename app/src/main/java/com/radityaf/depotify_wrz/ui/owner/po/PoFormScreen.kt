package com.radityaf.depotify_wrz.ui.owner.po

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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.data.local.entity.ProductEntity
import com.radityaf.depotify_wrz.data.local.entity.VendorEntity
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.owner.sales.ErrorBanner
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

    var selectedVendorEntity by remember { mutableStateOf<VendorEntity?>(null) }
    var isAnonymousVendor by remember { mutableStateOf(false) }
    var selectedProductEntity by remember { mutableStateOf<ProductEntity?>(null) }
    var qty by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    val availableProducts = remember(selectedVendorEntity, isAnonymousVendor, state.products) {
        if (isAnonymousVendor) {
            state.products
        } else {
            val vId = selectedVendorEntity?.id
            if (vId == null) {
                emptyList()
            } else {
                state.products.filter { it.vendorId == vId }
            }
        }
    }

    val isProductSelectable = selectedVendorEntity != null || isAnonymousVendor

    val paymentMethods = remember {
        listOf(
            "cash" to "Tunai (Cash)",
            "transfer" to "Transfer Bank",
            "qris" to "QRIS",
            "credit" to "Kredit / Tempo"
        )
    }
    var selectedPaymentMethod by remember { mutableStateOf("cash") }
    var shippingAddress by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var vendorExpanded by remember { mutableStateOf(false) }
    var productExpanded by remember { mutableStateOf(false) }
    var paymentExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { DepotifyTopBar("Buat PO Baru", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader("Pembelian", "Purchase Order Baru", "Isi data pesanan ke vendor dan simpan ke database server.")

            if (!state.errorMessage.isNullOrBlank()) {
                ErrorBanner(message = state.errorMessage!!) { viewModel.clearMessages() }
            }

            SectionCard(label = "Form", title = "Data Pembelian") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Vendor Dropdown
                    ExposedDropdownMenuBox(
                        expanded = vendorExpanded,
                        onExpandedChange = { vendorExpanded = it }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clayInset(cornerRadius = 16.dp, backgroundColor = DepotifyColors.SurfaceInset)
                        ) {
                            TextField(
                                value = when {
                                    isAnonymousVendor -> "Tanpa Vendor (Vendor Anonim / WhatsApp)"
                                    selectedVendorEntity != null -> selectedVendorEntity!!.name
                                    else -> "Pilih Vendor"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Vendor", color = DepotifyColors.Navy, fontWeight = FontWeight.Bold) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vendorExpanded) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = DepotifyColors.Navy,
                                    unfocusedTextColor = DepotifyColors.Navy
                                ),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                        }
                        ExposedDropdownMenu(
                            expanded = vendorExpanded,
                            onDismissRequest = { vendorExpanded = false }
                        ) {
                            // Option for Anonymous/WhatsApp PO (No specific vendor)
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text("Tanpa Vendor (Vendor Anonim / WA)", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                                        Text("Pesan produk bebas tanpa terikat vendor tertentu", fontSize = 11.sp, color = DepotifyColors.TextSecondary)
                                    }
                                },
                                onClick = {
                                    if (!isAnonymousVendor) {
                                        isAnonymousVendor = true
                                        selectedVendorEntity = null
                                        selectedProductEntity = null
                                        price = ""
                                    }
                                    vendorExpanded = false
                                }
                            )

                            HorizontalDivider()

                            if (state.vendors.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Belum ada vendor terdaftar di database") },
                                    onClick = { vendorExpanded = false }
                                )
                            }
                            state.vendors.forEach { vendor ->
                                DropdownMenuItem(
                                    text = { Text(vendor.name) },
                                    onClick = {
                                        if (selectedVendorEntity != vendor || isAnonymousVendor) {
                                            selectedVendorEntity = vendor
                                            isAnonymousVendor = false
                                            selectedProductEntity = null
                                            price = ""
                                        }
                                        vendorExpanded = false
                                    }
                                )
                            }
                        }
                    }



                    // Product Dropdown
                    ExposedDropdownMenuBox(
                        expanded = productExpanded && isProductSelectable && availableProducts.isNotEmpty(),
                        onExpandedChange = {
                            if (isProductSelectable && availableProducts.isNotEmpty()) {
                                productExpanded = it
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clayInset(cornerRadius = 16.dp, backgroundColor = DepotifyColors.SurfaceInset)
                        ) {
                            TextField(
                                value = when {
                                    !isProductSelectable -> "Pilih Vendor Terlebih Dahulu"
                                    availableProducts.isEmpty() -> "Vendor ini belum memiliki produk"
                                    selectedProductEntity != null -> selectedProductEntity!!.name
                                    else -> "Pilih Produk"
                                },
                                onValueChange = {},
                                readOnly = true,
                                enabled = isProductSelectable && availableProducts.isNotEmpty(),
                                label = { Text("Produk", color = DepotifyColors.Navy, fontWeight = FontWeight.Bold) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded && isProductSelectable && availableProducts.isNotEmpty()) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = DepotifyColors.Navy,
                                    unfocusedTextColor = DepotifyColors.Navy,
                                    disabledTextColor = DepotifyColors.TextMuted
                                ),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                        }
                        ExposedDropdownMenu(
                            expanded = productExpanded && isProductSelectable && availableProducts.isNotEmpty(),
                            onDismissRequest = { productExpanded = false }
                        ) {
                            if (availableProducts.isEmpty()) {
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            if (isAnonymousVendor) "Belum ada produk terdaftar di database" 
                                            else "Vendor ini belum memiliki produk yang dijual",
                                            fontSize = 12.sp,
                                            color = DepotifyColors.Danger
                                        ) 
                                    },
                                    onClick = { productExpanded = false }
                                )
                            }
                            availableProducts.forEach { prod ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(prod.name, fontWeight = FontWeight.Bold)
                                            val priceText = if (prod.purchasePrice > 0) formatRupiah(prod.purchasePrice.toLong()) else formatRupiah(prod.price)
                                            Text("Harga Modal: $priceText", fontSize = 11.sp, color = DepotifyColors.TextSecondary)
                                        }
                                    },
                                    onClick = {
                                        selectedProductEntity = prod
                                        val defaultPrice = if (prod.purchasePrice > 0) prod.purchasePrice.toLong() else prod.price
                                        price = defaultPrice.toString()
                                        productExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    DepotifyTextField("Jumlah (Qty)", qty, { qty = it.filter(Char::isDigit) }, placeholder = "0", keyboardType = KeyboardType.Number)
                    DepotifyTextField("Harga Satuan", price, { price = it.filter(Char::isDigit) }, placeholder = "0", keyboardType = KeyboardType.Number)

                    // Payment Method Dropdown
                    ExposedDropdownMenuBox(
                        expanded = paymentExpanded,
                        onExpandedChange = { paymentExpanded = it }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clayInset(cornerRadius = 16.dp, backgroundColor = DepotifyColors.SurfaceInset)
                        ) {
                            TextField(
                                value = paymentMethods.find { it.first == selectedPaymentMethod }?.second ?: "Tunai (Cash)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Metode Pembayaran", color = DepotifyColors.Navy, fontWeight = FontWeight.Bold) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = DepotifyColors.Navy,
                                    unfocusedTextColor = DepotifyColors.Navy
                                ),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                        }
                        ExposedDropdownMenu(
                            expanded = paymentExpanded,
                            onDismissRequest = { paymentExpanded = false }
                        ) {
                            paymentMethods.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selectedPaymentMethod = code
                                        paymentExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    DepotifyTextField("Alamat Pengiriman (Opsional)", shippingAddress, { shippingAddress = it }, placeholder = "Alamat toko / depot")
                    DepotifyTextField("Catatan Pesanan (Opsional)", notes, { notes = it }, placeholder = "Catatan untuk vendor")
                }
            }

            DepotifyButton(
                text = if (state.isSubmitting) "Menyimpan..." else "Simpan & Kembali",
                onClick = {
                    val vendor = selectedVendorEntity
                    val product = selectedProductEntity
                    if (product != null && (vendor != null || isAnonymousVendor)) {
                        val effectiveVendorId = vendor?.id
                            ?: product.vendorId
                            ?: state.vendors.firstOrNull()?.id
                            ?: 1
                        val effectiveVendorName = vendor?.name ?: "Vendor Anonim (WhatsApp)"
                        val effectiveNotes = if (isAnonymousVendor) {
                            "Pesanan via WhatsApp (Vendor Anonim)${if (notes.isNotBlank()) " - $notes" else ""}"
                        } else {
                            notes
                        }

                        viewModel.create(
                            vendorId = effectiveVendorId,
                            productId = product.id,
                            vendorName = effectiveVendorName,
                            productName = product.name,
                            qty = qty.toIntOrNull() ?: 0,
                            price = price.toLongOrNull() ?: 0L,
                            paymentMethod = selectedPaymentMethod,
                            shippingAddress = shippingAddress,
                            notes = effectiveNotes,
                            onComplete = onSaved
                        )
                    }
                },
                enabled = isProductSelectable && 
                        availableProducts.isNotEmpty() && 
                        selectedProductEntity != null && 
                        (qty.toIntOrNull() ?: 0) > 0 && 
                        (price.toLongOrNull() ?: 0L) > 0L && 
                        !state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
