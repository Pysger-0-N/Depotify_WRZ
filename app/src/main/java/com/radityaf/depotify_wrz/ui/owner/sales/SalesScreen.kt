package com.radityaf.depotify_wrz.ui.owner.sales

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
import com.radityaf.depotify_wrz.data.model.SaleTransaction
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.owner.kasir.KasirReceiptDialog
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors
import com.radityaf.depotify_wrz.utils.FormatUtils.toRupiah

@Composable
fun SalesScreen(
    modifier: Modifier = Modifier,
    viewModel: SalesViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val sales by viewModel.sales.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSales(null)
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { DepotifyTopBar("Penjualan", onBack) }
    ) { padding ->
        DepotifyRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refresh(null) },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PageHeader("Penjualan", "Daftar Transaksi", "Data transaksi penjualan.")

                if (!errorMessage.isNullOrEmpty()) {
                    ErrorBanner(message = errorMessage!!) { viewModel.clearMessages() }
                }
                if (!successMessage.isNullOrEmpty()) {
                    SuccessBanner(message = successMessage!!) { viewModel.clearMessages() }
                }

                if (isLoading && sales.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DepotifyColors.Green)
                    }
                } else if (sales.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        EmptyState("Belum ada data penjualan.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sales) { sale ->
                            SaleCard(
                                sale = sale,
                                isLoading = isLoading,
                                onConfirm = { viewModel.confirmSale(sale.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SaleCard(
    sale: SaleTransaction,
    isLoading: Boolean,
    onConfirm: () -> Unit
) {
    var showReceipt by remember { mutableStateOf(false) }

    if (showReceipt) {
        KasirReceiptDialog(
            transaction = sale,
            onDismiss = { showReceipt = false }
        )
    }

    SectionCard(
        label = "Invoice ${sale.displayInvoice}",
        title = sale.displayCustomerName
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("No. Telepon:", color = DepotifyColors.TextSecondary, fontSize = 12.sp)
                Text(sale.displayCustomerPhone ?: "-", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DepotifyColors.Navy)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Alamat:", color = DepotifyColors.TextSecondary, fontSize = 12.sp)
                Text(sale.displayCustomerAddress ?: "-", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DepotifyColors.Navy)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Status:", color = DepotifyColors.TextSecondary, fontSize = 12.sp)
                BadgeStatus(status = sale.status)
            }

            HorizontalDivider(color = DepotifyColors.Border)

            Text("Produk:", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = DepotifyColors.GreenDark)
            sale.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.product_name} (${item.qty}x)", fontSize = 12.sp, color = DepotifyColors.Navy)
                    Text(item.subtotal.toRupiah(), fontSize = 12.sp, color = DepotifyColors.Navy, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = DepotifyColors.Border)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Pembayaran:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DepotifyColors.Navy)
                Text(sale.total_amount.toRupiah(), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = DepotifyColors.GreenDark)
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statusLower = sale.status.lowercase()
                val isUnconfirmed = statusLower == "pending" || statusLower == "processing"

                if (isUnconfirmed) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clayButton(cornerRadius = 14.dp, enabled = !isLoading)
                            .clickable(enabled = !isLoading, onClick = onConfirm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Konformasi", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clayInset(cornerRadius = 14.dp, backgroundColor = DepotifyColors.SurfaceInset),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Konformasi", color = DepotifyColors.TextMuted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clayInset(cornerRadius = 14.dp, backgroundColor = DepotifyColors.SurfaceInset)
                        .clickable { showReceipt = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Lihat Struk", color = DepotifyColors.Navy, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun BadgeStatus(status: String) {
    val (color, text) = when (status.lowercase()) {
        "pending" -> DepotifyColors.Warning to "Pending"
        "processing" -> DepotifyColors.Warning to "Diproses"
        "confirmed" -> DepotifyColors.GreenDark to "Dikonfirmasi"
        "completed", "selesai" -> DepotifyColors.Navy to "Selesai"
        "cancelled" -> DepotifyColors.Danger to "Dibatalkan"
        else -> DepotifyColors.TextMuted to status
    }
    Box(
        modifier = Modifier
            .clayInset(cornerRadius = 8.dp, backgroundColor = color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text.uppercase(), color = color, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
    }
}

@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clayInset(cornerRadius = 12.dp, backgroundColor = DepotifyColors.WarningSoft)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(message, color = DepotifyColors.Danger, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(
                "✕",
                color = DepotifyColors.Danger,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickable { onDismiss() }.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun SuccessBanner(message: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clayInset(cornerRadius = 12.dp, backgroundColor = DepotifyColors.GreenSoft)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(message, color = DepotifyColors.GreenDark, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(
                "✕",
                color = DepotifyColors.GreenDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickable { onDismiss() }.padding(start = 8.dp)
            )
        }
    }
}
