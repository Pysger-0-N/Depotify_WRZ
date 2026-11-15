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

    var exportedFilePath by remember { mutableStateOf<String?>(null) }

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

            // Export PO to Excel Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clayButton(cornerRadius = 25.dp)
                    .clickable {
                        val path = exportPoExcel(context, po)
                        exportedFilePath = path
                        if (po.status == "PENDING" || po.status == "DIKONFIRMASI") {
                            viewModel.markPoAsShippedDueToExport(po.id)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ekspor PO ke Excel",
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
                                "Menunggu konfirmasi vendor. Ekspor laporan ke Excel dan konfirmasi penerimaan barang secara langsung jika perlu.",
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
                                    onReceiveClick(po.id)
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

            // Export Success Dialog
            if (exportedFilePath != null) {
                AlertDialog(
                    onDismissRequest = { exportedFilePath = null },
                    title = { Text("Ekspor PO Berhasil", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("File Excel (CSV) Purchase Order berhasil dibuat dan disimpan ke perangkat.", fontSize = 13.sp, color = DepotifyColors.TextSecondary)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clayInset(cornerRadius = 12.dp, backgroundColor = DepotifyColors.SurfaceInset)
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Direktori: Folder Downloads", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                                    Text("Nama File: ${exportedFilePath!!.substringAfterLast("/")}", fontSize = 12.sp, color = DepotifyColors.GreenDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { exportedFilePath = null },
                            colors = ButtonDefaults.buttonColors(containerColor = DepotifyColors.Navy)
                        ) {
                            Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = DepotifyColors.Surface
                )
            }
        }
    }
}

private fun exportPoExcel(
    context: android.content.Context,
    po: PurchaseOrder
): String {
    val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
    val fileName = "PurchaseOrder_#${po.id}_$timeStamp.csv"

    val csvContent = buildString {
        appendLine("DEPOTIFY — PURCHASE ORDER")
        appendLine("No. PO,#PO-${po.id}")
        appendLine("Vendor,${po.vendorName}")
        appendLine("Status,${po.status}")
        appendLine("Metode Pembayaran,${po.paymentMethod.uppercase()}")
        appendLine()
        appendLine("RINCIAN ITEM PESANAN")
        appendLine("Nama Produk,Jumlah,Harga Satuan,Subtotal")
        po.items.forEach { item ->
            val safeName = item.productName.replace(",", " ")
            val subtotal = item.qty * item.price
            appendLine("$safeName,${item.qty},${item.price},$subtotal")
        }
        appendLine()
        appendLine("TOTAL PEMBELIAN,${po.total}")
    }

    return com.radityaf.depotify_wrz.utils.ExportUtils.saveCsvToDownloads(context, fileName, csvContent)
}
