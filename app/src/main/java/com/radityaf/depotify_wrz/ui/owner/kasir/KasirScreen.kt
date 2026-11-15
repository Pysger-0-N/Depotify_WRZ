package com.radityaf.depotify_wrz.ui.owner.kasir

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.data.model.Category
import com.radityaf.depotify_wrz.data.model.Product
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.owner.sales.ErrorBanner
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors
import com.radityaf.depotify_wrz.utils.FormatUtils.toRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasirScreen(
    modifier: Modifier = Modifier,
    viewModel: KasirViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    val customerName by viewModel.customerName.collectAsState()
    val customerPhone by viewModel.customerPhone.collectAsState()

    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val cashReceivedText by viewModel.cashReceivedText.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val completedTransaction by viewModel.completedTransaction.collectAsState()

    val subtotalProducts by viewModel.subtotalProducts.collectAsState()
    val subtotalDeposits by viewModel.subtotalDeposits.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val cashReceived by viewModel.cashReceived.collectAsState()
    val changeAmount by viewModel.changeAmount.collectAsState()
    val isCashEnough by viewModel.isCashEnough.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Katalog, 1: Keranjang & Bayar
    var showClearCartDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            DepotifyTopBar("Kasir", onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PageHeader("Kasir", "Point of Sale", "Catat transaksi penjualan.")

            // Mode Selector Tabs (Katalog vs Keranjang)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (selectedTab == 0) Modifier.clayButton(cornerRadius = 14.dp)
                            else Modifier.clayInset(cornerRadius = 14.dp)
                        )
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Katalog Produk",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) Color.White else DepotifyColors.Navy
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (selectedTab == 1) Modifier.clayButton(cornerRadius = 14.dp)
                            else Modifier.clayInset(cornerRadius = 14.dp)
                        )
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Keranjang (${cartItems.sumOf { it.qty }})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) Color.White else DepotifyColors.Navy
                        )
                        if (cartItems.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clayInset(cornerRadius = 10.dp, backgroundColor = if (selectedTab == 1) Color.White else DepotifyColors.Green)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    totalAmount.toRupiah(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (selectedTab == 1) DepotifyColors.GreenDark else Color.White
                                )
                            }
                        }
                    }
                }
            }

            if (!errorMessage.isNullOrEmpty()) {
                ErrorBanner(message = errorMessage!!) { viewModel.clearErrorMessage() }
            }

            if (selectedTab == 0) {
                // TAB 0: KATALOG PRODUK
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search Bar
                    DepotifyTextField(
                        label = "Cari Produk",
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = "Masukkan nama produk atau SKU..."
                    )

                    // Filter Kategori Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            val isSelected = selectedCategory == null
                            Box(
                                modifier = Modifier
                                    .then(
                                        if (isSelected) Modifier.clayButton(cornerRadius = 12.dp)
                                        else Modifier.clayInset(cornerRadius = 12.dp)
                                    )
                                    .clickable { viewModel.setCategory(null) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Semua",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else DepotifyColors.Navy
                                )
                            }
                        }

                        items(categories) { cat ->
                            val isSelected = selectedCategory?.id == cat.id
                            Box(
                                modifier = Modifier
                                    .then(
                                        if (isSelected) Modifier.clayButton(cornerRadius = 12.dp)
                                        else Modifier.clayInset(cornerRadius = 12.dp)
                                    )
                                    .clickable { viewModel.setCategory(cat) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    cat.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else DepotifyColors.Navy
                                )
                            }
                        }
                    }

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = DepotifyColors.Green)
                        }
                    } else if (products.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState("Produk tidak ditemukan.")
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(products) { product ->
                                val inCartQty = cartItems.find { it.product.id == product.id }?.qty ?: 0
                                ProductPosCard(
                                    product = product,
                                    inCartQty = inCartQty,
                                    onAddToCart = { viewModel.addProductToCart(product) }
                                )
                            }
                        }
                    }
                }
            } else {
                // TAB 1: KERANJANG & PEMBAYARAN
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Section 1: Customer Info (Unified)
                    SectionCard(label = "Pelanggan", title = "Data Pembeli") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            DepotifyTextField(
                                label = "Nama Pelanggan",
                                value = customerName,
                                onValueChange = { viewModel.setCustomerName(it) },
                                placeholder = "Contoh: Pelanggan Umum / Budi"
                            )
                            DepotifyTextField(
                                label = "No. Telepon (Opsional)",
                                value = customerPhone,
                                onValueChange = { viewModel.setCustomerPhone(it) },
                                placeholder = "08xxxxxxxxxx",
                                keyboardType = KeyboardType.Phone
                            )
                        }
                    }

                    // Section 2: Cart Item List
                    SectionCard(label = "Keranjang", title = "Item Dibeli") {
                        if (cartItems.isEmpty()) {
                            EmptyState("Keranjang masih kosong. Pilih produk dari katalog.")
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                cartItems.forEach { item ->
                                    PosCartItemRow(
                                        item = item,
                                        onQtyChange = { q -> viewModel.updateCartItemQuantity(item.product.id, q) },
                                        onTypeChange = { t -> viewModel.updateCartItemTransactionType(item.product.id, t) },
                                        onRemove = { viewModel.removeCartItem(item.product.id) }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        "Kosongkan Keranjang",
                                        color = DepotifyColors.Danger,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { showClearCartDialog = true }
                                    )
                                }
                            }
                        }
                    }

                    // Section 3: Summary & Payment Method
                    if (cartItems.isNotEmpty()) {
                        SectionCard(label = "Pembayaran", title = "Metode Pembayaran") {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                SummaryRow("Subtotal Produk", subtotalProducts.toRupiah())
                                if (subtotalDeposits > 0) {
                                    SummaryRow("Total Deposit", subtotalDeposits.toRupiah())
                                }

                                HorizontalDivider(color = DepotifyColors.Border)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("TOTAL BAYAR", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = DepotifyColors.Navy)
                                    Text(totalAmount.toRupiah(), fontWeight = FontWeight.Black, fontSize = 17.sp, color = DepotifyColors.GreenDark)
                                }

                                HorizontalDivider(color = DepotifyColors.Border)

                                Text("Pilih Metode Pembayaran:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("cash" to "Tunai", "transfer" to "Transfer", "other" to "Lainnya").forEach { (key, label) ->
                                        val isSel = paymentMethod == key || (key == "cash" && paymentMethod == "tunai")
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .then(
                                                    if (isSel) Modifier.clayButton(cornerRadius = 12.dp)
                                                    else Modifier.clayInset(cornerRadius = 12.dp)
                                                )
                                                .clickable { viewModel.setPaymentMethod(key) }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else DepotifyColors.Navy)
                                        }
                                    }
                                }

                                if (paymentMethod == "tunai" || paymentMethod == "cash") {
                                    Spacer(Modifier.height(4.dp))
                                    DepotifyTextField(
                                        label = "Uang Diterima",
                                        value = cashReceivedText,
                                        onValueChange = { viewModel.setCashReceivedText(it) },
                                        placeholder = "Masukkan nominal pembayaran",
                                        keyboardType = KeyboardType.Number
                                    )

                                    // Quick Cash Chips
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(totalAmount to "Pas", 20000.0 to "20rb", 50000.0 to "50rb", 100000.0 to "100rb").forEach { (valAmt, lbl) ->
                                            Box(
                                                modifier = Modifier
                                                    .clayInset(cornerRadius = 10.dp, backgroundColor = DepotifyColors.SurfaceInset)
                                                    .clickable { viewModel.setQuickCashAmount(valAmt) }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Text(lbl, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = DepotifyColors.Navy)
                                            }
                                        }
                                    }

                                    if (cashReceived > 0) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Kembalian:", fontSize = 12.sp, color = DepotifyColors.TextSecondary, fontWeight = FontWeight.Bold)
                                            Box(
                                                modifier = Modifier
                                                    .clayInset(
                                                        cornerRadius = 10.dp,
                                                        backgroundColor = DepotifyColors.GreenSoft
                                                    )
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    changeAmount.toRupiah(),
                                                    color = DepotifyColors.GreenDark,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // Submit Button
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .clayButton(cornerRadius = 16.dp, enabled = isCashEnough && !isSubmitting)
                                        .clickable(enabled = isCashEnough && !isSubmitting) {
                                            viewModel.processSale()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text("Proses Transaksi", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Struk jika transaksi berhasil
    completedTransaction?.let { sale ->
        KasirReceiptDialog(
            transaction = sale,
            cashReceived = cashReceived,
            changeAmount = changeAmount,
            onDismiss = { viewModel.dismissCompletedTransaction() }
        )
    }

    if (showClearCartDialog) {
        DepotifyConfirmDialog(
            title = "Kosongkan Keranjang",
            message = "Hapus seluruh item dari keranjang kasir?",
            confirmText = "Kosongkan",
            onConfirm = {
                viewModel.clearCart()
                showClearCartDialog = false
            },
            onDismiss = { showClearCartDialog = false }
        )
    }
}

@Composable
private fun ProductPosCard(
    product: Product,
    inCartQty: Int,
    onAddToCart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clayCard(cornerRadius = 16.dp)
            .clickable { onAddToCart() }
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier
                        .clayInset(cornerRadius = 8.dp, backgroundColor = DepotifyColors.SurfaceInset)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(product.unit.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                }

                if (inCartQty > 0) {
                    Box(
                        modifier = Modifier
                            .clayButton(cornerRadius = 10.dp)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("${inCartQty}x", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }

            Text(product.name, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = DepotifyColors.Navy, maxLines = 1)
            if (!product.variant.isNullOrBlank()) {
                Text(product.variant, fontSize = 10.sp, color = DepotifyColors.TextSecondary)
            }

            Text(product.selling_price.toRupiah(), fontWeight = FontWeight.Black, fontSize = 13.sp, color = DepotifyColors.GreenDark)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Stok: ${product.stock_content}",
                    fontSize = 10.sp,
                    color = if (product.stock_content <= product.minimum_stock) DepotifyColors.Warning else DepotifyColors.TextMuted,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clayButton(cornerRadius = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun PosCartItemRow(
    item: PosCartItem,
    onQtyChange: (Int) -> Unit,
    onTypeChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clayInset(cornerRadius = 14.dp, backgroundColor = DepotifyColors.SurfaceInset)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.product.name, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = DepotifyColors.Navy)
                    Text(item.unitPrice.toRupiah(), fontSize = 11.sp, color = DepotifyColors.GreenDark, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = DepotifyColors.Danger, modifier = Modifier.size(18.dp))
                }
            }

            // If product has deposit price (e.g. Gallon/LPG), show transaction type selector
            if (item.product.deposit_price != null && item.product.deposit_price > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("top_up" to "Isi Ulang", "new_deposit" to "Beli Baru (+Deposit)", "buy_disconnect" to "Tanpa Deposit").forEach { (typeKey, typeLabel) ->
                        val isSel = item.transactionType == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (isSel) Modifier.clayButton(cornerRadius = 8.dp)
                                    else Modifier.clayInset(cornerRadius = 8.dp)
                                )
                                .clickable { onTypeChange(typeKey) }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(typeLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else DepotifyColors.Navy)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stepper Qty
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clayButton(cornerRadius = 8.dp)
                            .clickable { onQtyChange(item.qty - 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Text("${item.qty}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = DepotifyColors.Navy)

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clayButton(cornerRadius = 8.dp)
                            .clickable { onQtyChange(item.qty + 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Text(
                    item.itemSubtotal.toRupiah(),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = DepotifyColors.GreenDark
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = DepotifyColors.TextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
    }
}
