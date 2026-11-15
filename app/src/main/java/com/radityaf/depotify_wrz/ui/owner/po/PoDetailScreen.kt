package com.radityaf.depotify_wrz.ui.owner.po

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun PoDetailScreen(
    poId: Int,
    modifier: Modifier = Modifier,
    viewModel: PurchaseOrderViewModel = hiltViewModel(),
    onReceiveClick: (Int) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val po = state.items.find { it.id == poId }

    LaunchedEffect(poId) {
        viewModel.fetchServerPurchaseOrders()
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { DepotifyTopBar("Detail Purchase Order", onBack) }
    ) { padding ->
        if (po == null) {
            Box(Modifier.padding(padding).padding(16.dp)) { EmptyState("PO tidak ditemukan.") }
            return@Scaffold
        }

        Column(
            modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader("Pembelian", "PO #${po.id}", po.vendorName)

            DepotifyCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status Pesanan", fontSize = 13.sp, color = DepotifyColors.TextSecondary)
                        PoStatusBadge(po.status)
                    }
                    HorizontalDivider(color = DepotifyColors.Border)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Metode Pembayaran", fontSize = 13.sp, color = DepotifyColors.TextSecondary)
                        Text(po.paymentMethod.uppercase(), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DepotifyColors.Navy)
                    }
                }
            }

            SectionCard(label = "Rincian", title = "Item Pesanan") {
                po.items.forEachIndexed { index, item ->
                    if (index > 0) HorizontalDivider(color = DepotifyColors.Border, modifier = Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(item.productName, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 14.sp)
                            Text(
                                "${item.qty} x ${formatRupiah(item.price)}",
                                fontSize = 12.sp,
                                color = DepotifyColors.TextSecondary
                            )
                        }
                        Text(formatRupiah(item.qty * item.price), fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 14.sp)
                    }
                }
                HorizontalDivider(color = DepotifyColors.Border, modifier = Modifier.padding(vertical = 12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Pembelian", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                    Text(formatRupiah(po.total), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = DepotifyColors.GreenDark)
                }
            }

            // WhatsApp Share PO Option
            val waUrl = viewModel.formatWhatsAppUrl(po)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clayButton(cornerRadius = 25.dp)
                    .clickable {
                        viewModel.sendPoToWhatsAppAndMarkShipped(po, context)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (waUrl != null) "Kirim Laporan PO ke WhatsApp Vendor" else "Vendor Belum Ada No. WhatsApp",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }

            when (po.status) {
                "PENDING", "DIKONFIRMASI" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clayInset(cornerRadius = 16.dp, backgroundColor = DepotifyColors.WarningSoft)
                                .padding(14.dp)
                        ) {
                            Text(
                                "Menunggu konfirmasi vendor. Kirim laporan via WhatsApp dan konfirmasi penerimaan barang secara langsung jika perlu.",
                                color = DepotifyColors.Warning,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clayInset(cornerRadius = 26.dp, backgroundColor = DepotifyColors.GreenSoft)
                                .clickable {
                                    viewModel.directReceiveGoods(po.id) {
                                        onReceiveClick(po.id)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Konfirmasi Langsung Barang Diterima",
                                color = DepotifyColors.GreenDark,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                "DIKIRIM" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clayInset(cornerRadius = 16.dp, backgroundColor = Color(0xFFE0F7FA))
                                .padding(14.dp)
                        ) {
                            Text(
                                "Pesanan telah dikirim ke Vendor/dikirimkan oleh Vendor.",
                                color = Color(0xFF00838F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clayInset(cornerRadius = 26.dp, backgroundColor = DepotifyColors.GreenSoft)
                                .clickable { onReceiveClick(po.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Konfirmasi Barang Diterima",
                                color = DepotifyColors.GreenDark,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }
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
                            "Barang PO ini sudah diterima dan stok depot telah diperbarui.",
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
                            "Pesanan ini telah ditolak oleh Vendor.",
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
