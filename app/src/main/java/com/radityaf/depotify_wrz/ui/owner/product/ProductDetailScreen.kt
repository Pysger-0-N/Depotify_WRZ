package com.radityaf.depotify_wrz.ui.owner.product

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
import com.radityaf.depotify_wrz.ui.components.EmptyState
import com.radityaf.depotify_wrz.ui.components.SectionCard
import com.radityaf.depotify_wrz.ui.components.SectionLabel
import com.radityaf.depotify_wrz.ui.components.StatCard
import com.radityaf.depotify_wrz.ui.components.formatRupiah
import com.radityaf.depotify_wrz.ui.product.ProductViewModel
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@OptIn(ExperimentalMaterial3Api::class)
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
        containerColor = DepotifyColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("Detail Produk", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Kembali", color = DepotifyColors.Green) }
                },
                actions = {
                    TextButton(onClick = { onEditClick(productId) }) {
                        Text("Edit", color = DepotifyColors.Green, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DepotifyColors.Navy)
            )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                SectionLabel(product.category)
                Text(
                    product.name,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DepotifyColors.Navy
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "Stok",
                    value = product.stock.toString(),
                    caption = if (lowStock) "stok menipis" else "tersedia",
                    badge = if (lowStock) "!" else "▢",
                    valueColor = if (lowStock) DepotifyColors.Warning else DepotifyColors.Navy,
                    badgeBackground = if (lowStock) DepotifyColors.WarningSoft else DepotifyColors.GreenSoft,
                    badgeColor = if (lowStock) DepotifyColors.Warning else DepotifyColors.GreenDark,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Harga",
                    value = formatRupiah(product.price),
                    caption = "per satuan",
                    badge = "Rp",
                    modifier = Modifier.weight(1f)
                )
            }

            SectionCard(label = "Detail", title = "Informasi Produk") {
                InfoRow("Kategori", product.category)
                HorizontalDivider(color = DepotifyColors.Border, modifier = Modifier.padding(vertical = 8.dp))
                InfoRow("Barcode", product.barcode)
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
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DepotifyColors.Navy)
    }
}
