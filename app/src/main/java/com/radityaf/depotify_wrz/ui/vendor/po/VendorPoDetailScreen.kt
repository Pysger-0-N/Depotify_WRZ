package com.radityaf.depotify_wrz.ui.vendor.po

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
import com.radityaf.depotify_wrz.ui.owner.sales.ErrorBanner
import com.radityaf.depotify_wrz.ui.owner.sales.SuccessBanner
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun VendorPoDetailScreen(
    poId: Int,
    modifier: Modifier = Modifier,
    viewModel: VendorPoViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val po = state.items.find { it.id == poId }

    LaunchedEffect(poId) {
        viewModel.fetchServerPurchaseOrders()
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { DepotifyTopBar("Detail PO Vendor", onBack) }
    ) { padding ->
        if (po == null) {
            Box(Modifier.padding(padding).padding(16.dp)) { EmptyState("PO tidak ditemukan.") }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader("Purchase Order", "PO #${po.id}", "Vendor: ${po.vendorName}")

            if (!state.errorMessage.isNullOrBlank()) {
                ErrorBanner(message = state.errorMessage!!) { viewModel.clearMessages() }
            }
            if (!state.successMessage.isNullOrBlank()) {
                SuccessBanner(message = state.successMessage!!) { viewModel.clearMessages() }
            }

            DepotifyCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Status Pesanan", fontSize = 13.sp, color = DepotifyColors.TextSecondary)
                    PoStatusBadge(po.status)
                }
            }

            SectionCard(label = "Rincian", title = "Item Pesanan dari Owner") {
                po.items.forEachIndexed { index, item ->
                    if (index > 0) HorizontalDivider(color = DepotifyColors.Border, modifier = Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(item.productName, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 14.sp)
                            Text("${item.qty} x ${formatRupiah(item.price)}", fontSize = 12.sp, color = DepotifyColors.TextSecondary)
                        }
                        Text(formatRupiah(item.qty * item.price), fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 14.sp)
                    }
                }
                HorizontalDivider(color = DepotifyColors.Border, modifier = Modifier.padding(vertical = 12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Pesanan", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                    Text(formatRupiah(po.total), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = DepotifyColors.GreenDark)
                }
            }

            // Action Buttons based on PO Status
            when (po.status) {
                "PENDING" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clayInset(cornerRadius = 14.dp, backgroundColor = DepotifyColors.WarningSoft)
                                .padding(12.dp)
                        ) {
                            Text(
                                "Ada pesanan PO baru dari pemilik depot. Konfirmasi untuk memproses pesanan.",
                                color = DepotifyColors.Warning,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            DepotifyButton(
                                text = if (state.isLoading) "Memproses..." else "Konfirmasi Pesanan",
                                onClick = { viewModel.confirmPo(po.id) },
                                enabled = !state.isLoading,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clayInset(cornerRadius = 26.dp, backgroundColor = DepotifyColors.WarningSoft)
                                    .clickable(enabled = !state.isLoading) { viewModel.rejectPo(po.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Tolak", color = DepotifyColors.Danger, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            }
                        }
                    }
                }
                "DIKONFIRMASI" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clayInset(cornerRadius = 14.dp, backgroundColor = Color(0xFFE3F2FD))
                                .padding(12.dp)
                        ) {
                            Text(
                                "Pesanan telah dikonfirmasi. Perbarui status saat barang siap dikirim.",
                                color = Color(0xFF1565C0),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        DepotifyButton(
                            text = if (state.isLoading) "Memproses..." else "Kirim Pesanan",
                            onClick = { viewModel.shipPo(po.id) },
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                "DIKIRIM" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clayInset(cornerRadius = 16.dp, backgroundColor = Color(0xFFE0F7FA))
                            .padding(14.dp)
                    ) {
                        Text(
                            "Pesanan sedang dalam proses pengiriman ke depot Owner. Menunggu konfirmasi penerimaan barang oleh Admin/Owner.",
                            color = Color(0xFF00838F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
                "DITERIMA" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clayInset(cornerRadius = 16.dp, backgroundColor = DepotifyColors.GreenSoft)
                            .padding(14.dp)
                    ) {
                        Text(
                            "Pesanan telah diterima dan dikonfirmasi oleh Admin/Owner.",
                            color = DepotifyColors.GreenDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
                "DITOLAK" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clayInset(cornerRadius = 16.dp, backgroundColor = DepotifyColors.WarningSoft)
                            .padding(14.dp)
                    ) {
                        Text(
                            "Pesanan ini telah ditolak.",
                            color = DepotifyColors.Danger,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
