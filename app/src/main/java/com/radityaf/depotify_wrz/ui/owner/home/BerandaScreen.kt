package com.radityaf.depotify_wrz.ui.owner.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun BerandaScreen(
    modifier: Modifier = Modifier,
    viewModel: OwnerHomeViewModel = hiltViewModel(),
    onCategoryClick: () -> Unit = {},
    onVendorClick: () -> Unit = {},
    onScanClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header halaman (seperti "OVERVIEW / Dashboard" di web)
        Column {
            SectionLabel("Overview")
            Text(
                "Dashboard",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DepotifyColors.Navy
            )
            Text(
                "Ringkasan aktivitas depot hari ini.",
                fontSize = 12.sp,
                color = DepotifyColors.TextSecondary
            )
        }

        // 4 kartu statistik, grid 2 x 2
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = "Transaksi hari ini",
                value = state.transaksiHariIni.toString(),
                caption = "transaksi selesai",
                badge = "↗",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Pendapatan hari ini",
                value = formatRupiah(state.pendapatanHariIni),
                caption = "total penjualan",
                badge = "Rp",
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = "Produk aktif",
                value = state.produkAktif.toString(),
                caption = "produk tersedia",
                badge = "▢",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Stok menipis",
                value = state.stokMenipis.toString(),
                caption = "perlu diperhatikan",
                badge = "!",
                valueColor = DepotifyColors.Warning,
                badgeBackground = DepotifyColors.WarningSoft,
                badgeColor = DepotifyColors.Warning,
                modifier = Modifier.weight(1f)
            )
        }

        // Chart penjualan 7 hari
        SectionCard(label = "Performa", title = "Penjualan 7 Hari Terakhir") {
            SalesBarChart(state.sales7Days)
        }

        // Stok menipis
        SectionCard(label = "Persediaan", title = "Stok Menipis") {
            if (state.lowStock.isEmpty()) {
                EmptyState("Semua stok aman.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.lowStock.forEach { LowStockRow(it) }
                }
            }
        }

        // Produk terlaris
        SectionCard(label = "Penjualan", title = "Produk Terlaris") {
            if (state.topProducts.isEmpty()) {
                EmptyState("Belum ada data penjualan.")
            } else {
                state.topProducts.forEach {
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(it.name, fontWeight = FontWeight.Medium)
                        Text("${it.sold} terjual", color = DepotifyColors.TextSecondary)
                    }
                }
            }
        }

        // Transaksi terbaru
        SectionCard(label = "Aktivitas", title = "Transaksi Terbaru") {
            if (state.recentTransactions.isEmpty()) {
                EmptyState("Belum ada transaksi.")
            } else {
                state.recentTransactions.forEach {
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(it.code, fontWeight = FontWeight.Medium)
                            Text(it.time, fontSize = 11.sp, color = DepotifyColors.TextMuted)
                        }
                        Text(formatRupiah(it.total), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DepotifyColors.Border),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = DepotifyColors.Navy),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LowStockRow(item: LowStockItem) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = androidx.compose.ui.graphics.Color(0xFFFAFAF8),
        border = BorderStroke(1.dp, DepotifyColors.Border)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(item.unit.uppercase(), fontSize = 10.sp, color = DepotifyColors.TextMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    item.stock.toString(),
                    fontWeight = FontWeight.Bold,
                    color = if (item.stock == 0) DepotifyColors.Danger else DepotifyColors.Warning
                )
                Text("Min. ${item.minStock}", fontSize = 10.sp, color = DepotifyColors.TextMuted)
            }
        }
    }
}

/** Bar chart sederhana tanpa library: tinggi batang = proporsi terhadap nilai terbesar. */
@Composable
private fun SalesBarChart(data: List<DailySales>) {
    val maxValue = (data.maxOfOrNull { it.total } ?: 0L).coerceAtLeast(1L)

    Row(
        Modifier.fillMaxWidth().height(170.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { day ->
            val fraction = day.total.toFloat() / maxValue
            Column(
                Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(compactRupiah(day.total), fontSize = 9.sp, color = DepotifyColors.TextSecondary)
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .fillMaxWidth(0.6f)
                        .height(maxOf(4.dp, 100.dp * fraction)) // minimal 4dp, seperti garis hijau di web saat Rp 0
                        .clip(RoundedCornerShape(4.dp))
                        .background(DepotifyColors.Green)
                )
                Spacer(Modifier.height(6.dp))
                Text(day.label, fontSize = 9.sp, color = DepotifyColors.TextMuted)
            }
        }
    }
}