package com.radityaf.depotify_wrz.ui.product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun ProductListScreen(
    modifier: Modifier = Modifier,
    viewModel: ProductViewModel = hiltViewModel(),
    onProductClick: (Int) -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onScanClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val products = state.filtered

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clayButton(cornerRadius = 28.dp)
                    .clickable { onAddProductClick() },
                contentAlignment = Alignment.Center
            ) {
                Text("+", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
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
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            item { PageHeader("Inventaris", "Daftar Produk", "Kelola stok dan harga produk depot.") }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clayInset(cornerRadius = 18.dp, backgroundColor = DepotifyColors.SurfaceInset)
                ) {
                    TextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = { Text("Cari nama produk, barcode, atau SKU", color = DepotifyColors.TextMuted, fontSize = 13.sp) },
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

            if (products.isEmpty()) {
                item { EmptyState("Produk tidak ditemukan.") }
            }

            items(products, key = { it.id }) { p ->
                val lowStock = p.stock <= p.minimumStock
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clayCard(cornerRadius = 20.dp)
                        .clickable { onProductClick(p.id) }
                        .padding(16.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            SectionLabel(p.category)
                            Spacer(Modifier.height(4.dp))
                            Text(p.name, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 15.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(formatRupiah(p.price), fontSize = 13.sp, color = DepotifyColors.GreenDark, fontWeight = FontWeight.ExtraBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                p.stock.toString(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (lowStock) DepotifyColors.Warning else DepotifyColors.Navy
                            )
                            Text("stok", fontSize = 10.sp, color = DepotifyColors.TextMuted, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
}
