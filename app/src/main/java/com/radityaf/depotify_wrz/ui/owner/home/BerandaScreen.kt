package com.radityaf.depotify_wrz.ui.owner.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun BerandaScreen(
    modifier: Modifier = Modifier,
    viewModel: OwnerHomeViewModel = hiltViewModel(),
    onCategoryClick: () -> Unit = {},
    onVendorClick: () -> Unit = {},
    onKasirClick: () -> Unit = {},
    onSalesClick: () -> Unit = {},
    onProductClick: () -> Unit = {},
    onStockClick: () -> Unit = {},
    onScanClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    DepotifyRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header halaman
            PageHeader(
                label = if (state.isOnline) "Online" else "Offline",
                title = "Dashboard",
                subtitle = "Ringkasan penjualan dan aktivitas depot hari ini."
            )

            // Banner Utama Kasir POS Tatap Muka
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clayButton(cornerRadius = 20.dp)
                    .clickable { onKasirClick() }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kasir", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(2.dp))
                        Text("Catat transaksi langsung di tempat", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clayInset(cornerRadius = 12.dp, backgroundColor = Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Buka Kasir →", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                    }
                }
            }

            // 4 kartu statistik Claymorphism, grid 2 x 2
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "Transaksi hari ini",
                    value = state.transaksiHariIni.toString(),
                    caption = "transaksi selesai",
                    badge = "↗",
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSalesClick() }
                )
                StatCard(
                    label = "Pendapatan hari ini",
                    value = formatRupiah(state.pendapatanHariIni),
                    caption = "total penjualan",
                    badge = "Rp",
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSalesClick() }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "Produk aktif",
                    value = state.produkAktif.toString(),
                    caption = "produk tersedia",
                    badge = "▢",
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onProductClick() }
                )
                StatCard(
                    label = "Stok menipis",
                    value = state.stokMenipis.toString(),
                    caption = "perlu perhatian",
                    badge = "!",
                    valueColor = DepotifyColors.Warning,
                    badgeBackground = DepotifyColors.WarningSoft,
                    badgeColor = DepotifyColors.Warning,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onStockClick() }
                )
            }

            // Chart penjualan 7 hari
            SectionCard(label = "Performa", title = "Penjualan 7 Hari") {
                SalesBarChart(state.sales7Days)
            }

            // Stok menipis
            SectionCard(label = "Persediaan", title = "Stok Menipis") {
                if (state.lowStock.isEmpty()) {
                    EmptyState("Semua stok dalam kondisi aman.")
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
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(it.name, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 13.sp)
                            Box(
                                Modifier
                                    .clayInset(cornerRadius = 8.dp, backgroundColor = DepotifyColors.GreenSoft)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("${it.sold} terjual", color = DepotifyColors.GreenDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
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
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { onSalesClick() },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(it.code, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 13.sp)
                                Text(it.time, fontSize = 11.sp, color = DepotifyColors.TextMuted)
                            }
                            Text(formatRupiah(it.total), fontWeight = FontWeight.ExtraBold, color = DepotifyColors.GreenDark, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LowStockRow(item: LowStockItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clayInset(cornerRadius = 14.dp, backgroundColor = DepotifyColors.SurfaceInset)
            .padding(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DepotifyColors.Navy)
                Text(item.unit.uppercase(), fontSize = 10.sp, color = DepotifyColors.TextMuted, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    item.stock.toString(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = if (item.stock == 0) DepotifyColors.Danger else DepotifyColors.Warning
                )
                Text("Min. ${item.minStock}", fontSize = 10.sp, color = DepotifyColors.TextMuted)
            }
        }
    }
}

/** Bar chart sederhana Claymorphism: tinggi batang = proporsi terhadap nilai terbesar dengan Clay Gradient. */
@Composable
private fun SalesBarChart(data: List<DailySales>) {
    val maxValue = (data.maxOfOrNull { it.total } ?: 0L).coerceAtLeast(1L)

    Row(
        Modifier
            .fillMaxWidth()
            .height(170.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { day ->
            val fraction = day.total.toFloat() / maxValue
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(compactRupiah(day.total), fontSize = 9.sp, color = DepotifyColors.TextSecondary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .fillMaxWidth(0.6f)
                        .height(maxOf(6.dp, 100.dp * fraction))
                        .clayButton(cornerRadius = 6.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(day.label, fontSize = 9.sp, color = DepotifyColors.TextMuted, fontWeight = FontWeight.Bold)
            }
        }
    }
}
