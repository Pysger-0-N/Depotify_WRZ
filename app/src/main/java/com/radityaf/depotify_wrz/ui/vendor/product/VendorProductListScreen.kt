package com.radityaf.depotify_wrz.ui.vendor.product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.data.model.Product
import com.radityaf.depotify_wrz.data.model.User
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.owner.sales.ErrorBanner
import com.radityaf.depotify_wrz.ui.owner.sales.SuccessBanner
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun VendorProductListScreen(
    user: User,
    modifier: Modifier = Modifier,
    viewModel: VendorProductViewModel = hiltViewModel(),
    onAddClick: () -> Unit = {},
    onProductEditClick: (Int) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var productToDelete by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(user.effectiveVendorId) {
        viewModel.setVendorId(user.effectiveVendorId)
    }

    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Hapus Produk", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy) },
            text = { Text("Apakah Anda yakin ingin menghapus produk '${productToDelete?.name}' dari server?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productToDelete?.let { viewModel.deleteProduct(it) }
                        productToDelete = null
                    }
                ) {
                    Text("Hapus", color = DepotifyColors.Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Batal", color = DepotifyColors.TextSecondary)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clayButton(cornerRadius = 28.dp)
                    .clickable { onAddClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Produk", tint = Color.White)
            }
        }
    ) { padding ->
        DepotifyRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PageHeader(
                        "Vendor",
                        "Katalog Produk Server",
                        "Kelola produk langsung dari server database."
                    )
                }

                if (!state.errorMessage.isNullOrBlank()) {
                    item { ErrorBanner(message = state.errorMessage!!) { viewModel.clearMessages() } }
                }
                if (!state.successMessage.isNullOrBlank()) {
                    item { SuccessBanner(message = state.successMessage!!) { viewModel.clearMessages() } }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clayInset(cornerRadius = 18.dp, backgroundColor = DepotifyColors.SurfaceInset)
                    ) {
                        TextField(
                            value = state.query,
                            onValueChange = viewModel::onQueryChange,
                            placeholder = { Text("Cari nama produk / SKU...", color = DepotifyColors.TextMuted, fontSize = 13.sp) },
                            singleLine = true,
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
                }

                if (state.filteredProducts.isEmpty()) {
                    item { EmptyState("Belum ada produk terdaftar di server untuk Vendor ini. Klik + untuk membuat produk baru.") }
                } else {
                    items(state.filteredProducts, key = { it.id }) { product ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clayCard(cornerRadius = 20.dp)
                                .clickable { onProductEditClick(product.id) }
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            product.name,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = DepotifyColors.Navy,
                                            fontSize = 16.sp
                                        )
                                        if (!product.variant.isNullOrBlank()) {
                                            Text(
                                                "Varian: ${product.variant}",
                                                fontSize = 12.sp,
                                                color = DepotifyColors.TextSecondary
                                            )
                                        }
                                        if (!product.sku.isNullOrBlank()) {
                                            Text(
                                                "SKU: ${product.sku}",
                                                fontSize = 11.sp,
                                                color = DepotifyColors.TextMuted
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = { onProductEditClick(product.id) }) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Produk",
                                                tint = DepotifyColors.GreenDark
                                            )
                                        }
                                        IconButton(onClick = { productToDelete = product }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Hapus Produk",
                                                tint = DepotifyColors.Danger
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Harga Modal / Beli: ${formatRupiah(product.purchase_price.toLong())}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = DepotifyColors.GreenDark
                                        )
                                        Text(
                                            "Harga Jual Depot: ${formatRupiah(product.selling_price.toLong())}",
                                            fontSize = 12.sp,
                                            color = DepotifyColors.TextSecondary
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clayInset(
                                                cornerRadius = 10.dp,
                                                backgroundColor = if (product.is_active) DepotifyColors.GreenSoft else DepotifyColors.WarningSoft
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            if (product.is_active) "Aktif" else "Non-Aktif",
                                            color = if (product.is_active) DepotifyColors.GreenDark else DepotifyColors.Warning,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
