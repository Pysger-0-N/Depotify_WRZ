package com.radityaf.depotify_wrz.ui.owner.product

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.product.ProductViewModel
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun ProductDetailScreen(
    productId: Int,
    modifier: Modifier = Modifier,
    viewModel: ProductViewModel = hiltViewModel(),
    onEditClick: (Int) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val product = state.allItems.find { it.id == productId }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 4.dp)
                    .clayCard(cornerRadius = 24.dp, color = DepotifyColors.Navy)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clayInset(cornerRadius = 12.dp, backgroundColor = Color(0x33FFFFFF))
                            .clickable { onBack() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("← Kembali", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Detail Produk", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Box(
                        modifier = Modifier
                            .clayButton(cornerRadius = 12.dp)
                            .clickable { onEditClick(productId) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Edit", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        if (product == null) {
            Box(Modifier.padding(padding).padding(16.dp)) {
                EmptyState("Produk tidak ditemukan.")
            }
            return@Scaffold
        }

        val lowStock = product.stock <= 10

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader(
                label = product.category,
                title = product.name,
                subtitle = "Informasi rinci stok & harga produk."
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "Stok Tersedia",
                    value = product.stock.toString(),
                    caption = if (lowStock) "stok menipis" else "aman",
                    badge = if (lowStock) "!" else "▢",
                    valueColor = if (lowStock) DepotifyColors.Warning else DepotifyColors.Navy,
                    badgeBackground = if (lowStock) DepotifyColors.WarningSoft else DepotifyColors.GreenSoft,
                    badgeColor = if (lowStock) DepotifyColors.Warning else DepotifyColors.GreenDark,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Harga Jual",
                    value = formatRupiah(product.price),
                    caption = "per satuan",
                    badge = "Rp",
                    modifier = Modifier.weight(1f)
                )
            }

            SectionCard(label = "Detail", title = "Informasi Tambahan") {
                InfoRow("Kategori", product.category)
                HorizontalDivider(color = DepotifyColors.Border, modifier = Modifier.padding(vertical = 8.dp))
                InfoRow("Barcode / SKU", product.barcode)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = DepotifyColors.TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
    }
}
