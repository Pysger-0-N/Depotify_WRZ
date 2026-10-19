package com.radityaf.depotify_wrz.ui.product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        containerColor = DepotifyColors.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProductClick,
                containerColor = DepotifyColors.Green,
                contentColor = DepotifyColors.Navy
            ) { Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { PageHeader("Inventaris", "Produk", "Kelola produk depot Anda.") }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = { Text("Cari produk / barcode") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DepotifyColors.Green,
                            unfocusedBorderColor = DepotifyColors.Border,
                            focusedContainerColor = DepotifyColors.Surface,
                            unfocusedContainerColor = DepotifyColors.Surface
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }

            if (products.isEmpty()) {
                item { EmptyState("Produk tidak ditemukan.") }
            }

            items(products, key = { it.id }) { p ->
                val lowStock = p.stock <= p.minimumStock
                DepotifyCard(Modifier.fillMaxWidth().clickable { onProductClick(p.id) }) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(p.name, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                            Text(p.category.uppercase(), fontSize = 10.sp, letterSpacing = 1.sp, color = DepotifyColors.TextMuted)
                            Spacer(Modifier.height(6.dp))
                            Text(formatRupiah(p.price), fontSize = 13.sp, color = DepotifyColors.TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                p.stock.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (lowStock) DepotifyColors.Warning else DepotifyColors.Navy
                            )
                            Text("stok", fontSize = 10.sp, color = DepotifyColors.TextMuted)
                        }
                    }
                }
            }
        }
    }
}
