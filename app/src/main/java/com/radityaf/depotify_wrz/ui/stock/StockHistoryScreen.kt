package com.radityaf.depotify_wrz.ui.stock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.radityaf.depotify_wrz.ui.owner.sales.ErrorBanner
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun StockHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: StockViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            if (onBack != null) {
                DepotifyTopBar("Riwayat Stok", onBack)
            }
        }
    ) { padding ->
        DepotifyRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .padding(if (onBack != null) padding else PaddingValues(0.dp))
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    PageHeader(
                        label = if (state.isOnline) "Online" else "Offline",
                        title = "Riwayat Stok",
                        subtitle = "Log lengkap aktivitas stok dari penjualan, purchase order, dan pergerakan manual."
                    )
                }

                if (!state.errorMessage.isNullOrBlank()) {
                    item {
                        ErrorBanner(message = state.errorMessage!!, onDismiss = { viewModel.clearMessages() })
                    }
                }

                // Search Bar & Filter Chips
                item {
                    SectionCard(label = "Filter & Pencarian", title = "Cari Riwayat Stok") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            DepotifyTextField(
                                label = "Cari Produk / Catatan",
                                value = state.searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = "Ketik nama barang atau keterangan..."
                            )

                            Text("Tipe Pergerakan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val movementOptions = listOf("SEMUA" to "Semua", "MASUK" to "Masuk", "KELUAR" to "Keluar", "PENYESUAIAN" to "Penyesuaian")
                                items(movementOptions) { (key, label) ->
                                    val selected = state.movementTypeFilter.equals(key, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .then(
                                                if (selected) Modifier.clayButton(cornerRadius = 12.dp)
                                                else Modifier.clayInset(cornerRadius = 12.dp)
                                            )
                                            .clickable { viewModel.setMovementTypeFilter(key) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color.White else DepotifyColors.Navy
                                        )
                                    }
                                }
                            }

                            Text("Jenis Stok", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val stockOptions = listOf("SEMUA" to "Semua Jenis", "ISI" to "Stok Isi", "KOSONG" to "Stok Kosong")
                                items(stockOptions) { (key, label) ->
                                    val selected = state.stockTypeFilter.equals(key, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .then(
                                                if (selected) Modifier.clayButton(cornerRadius = 12.dp)
                                                else Modifier.clayInset(cornerRadius = 12.dp)
                                            )
                                            .clickable { viewModel.setStockTypeFilter(key) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color.White else DepotifyColors.Navy
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Summary Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clayCard(cornerRadius = 14.dp)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Total Masuk", fontSize = 10.sp, color = DepotifyColors.TextMuted, fontWeight = FontWeight.Bold)
                                Text("+${state.totalMasukCount}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = DepotifyColors.GreenDark)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clayCard(cornerRadius = 14.dp)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Total Keluar", fontSize = 10.sp, color = DepotifyColors.TextMuted, fontWeight = FontWeight.Bold)
                                Text("-${state.totalKeluarCount}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = DepotifyColors.Warning)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clayCard(cornerRadius = 14.dp)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Total Log", fontSize = 10.sp, color = DepotifyColors.TextMuted, fontWeight = FontWeight.Bold)
                                Text("${state.filteredItems.size}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = DepotifyColors.Navy)
                            }
                        }
                    }
                }

                if (state.filteredItems.isEmpty()) {
                    item { EmptyState("Tidak ada pergerakan stok yang sesuai dengan filter.") }
                }

                items(state.filteredItems, key = { it.id }) { m ->
                    val isMasuk = m.type == "MASUK"
                    val isPenyesuaian = m.type == "PENYESUAIAN"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clayCard(cornerRadius = 18.dp)
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        m.productName,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = DepotifyColors.Navy,
                                        fontSize = 15.sp
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(m.date, fontSize = 11.sp, color = DepotifyColors.TextMuted)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    val prefix = if (isMasuk) "+" else if (isPenyesuaian) "±" else "-"
                                    val color = if (isMasuk) DepotifyColors.GreenDark else if (isPenyesuaian) DepotifyColors.Navy else DepotifyColors.Warning
                                    Text(
                                        "$prefix${m.qty}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = color
                                    )
                                }
                            }

                            HorizontalDivider(color = DepotifyColors.Border, thickness = 0.8.dp)

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    StatusBadge(m.type, positive = isMasuk)

                                    // Stock Type badge (ISI / KOSONG)
                                    Box(
                                        modifier = Modifier
                                            .clayInset(
                                                cornerRadius = 8.dp,
                                                backgroundColor = if (m.stockType == "ISI") DepotifyColors.GreenSoft else DepotifyColors.WarningSoft
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Stok ${m.stockType}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (m.stockType == "ISI") DepotifyColors.GreenDark else DepotifyColors.Warning
                                        )
                                    }
                                }

                                Text(
                                    text = "Sisa stok: ${m.endingBalance}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DepotifyColors.Navy
                                )
                            }

                            if (!m.description.isNullOrBlank() || m.referenceType != "manual") {
                                val refText = when (m.referenceType.lowercase()) {
                                    "sales_transaction" -> "Penjualan Kasir"
                                    "purchase_order" -> "Purchase Order"
                                    "goods_receipt" -> "Penerimaan PO"
                                    else -> "Pergerakan Manual"
                                }
                                val descText = if (!m.description.isNullOrBlank()) "$refText • ${m.description}" else refText

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clayInset(cornerRadius = 10.dp, backgroundColor = DepotifyColors.SurfaceInset)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = descText,
                                        fontSize = 11.sp,
                                        color = DepotifyColors.TextSecondary
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
