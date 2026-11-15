package com.radityaf.depotify_wrz.ui.owner.kasir

import android.widget.Toast
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.radityaf.depotify_wrz.data.model.SaleTransaction
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors
import com.radityaf.depotify_wrz.utils.FormatUtils.toRupiah
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun KasirReceiptDialog(
    transaction: SaleTransaction,
    cashReceived: Double = 0.0,
    changeAmount: Double = 0.0,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var exportedFilePath by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .clayCard(cornerRadius = 28.dp, color = DepotifyColors.Surface)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Struk
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clayButton(cornerRadius = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }

                Text("DEPOTIFY", color = DepotifyColors.Navy, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 2.sp)
                Text("STRUK PENJUALAN", color = DepotifyColors.GreenDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                HorizontalDivider(color = DepotifyColors.Border)

                // Transaction Meta
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ReceiptRow("No. Invoice", transaction.displayInvoice)
                    ReceiptRow("Pelanggan", transaction.displayCustomerName)
                    if (!transaction.displayCustomerPhone.isNullOrBlank()) {
                        ReceiptRow("No. Telepon", transaction.displayCustomerPhone!!)
                    }
                    if (!transaction.created_at.isNullOrBlank()) {
                        ReceiptRow("Tanggal", transaction.created_at)
                    }
                    ReceiptRow("Metode Bayar", transaction.payment_method?.uppercase() ?: "CASH")
                }

                HorizontalDivider(color = DepotifyColors.Border)

                // Item breakdown
                Text("Rincian Pesanan", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = DepotifyColors.Navy)

                transaction.items.forEach { item ->
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "${item.product_name} (${item.qty}x)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = DepotifyColors.Navy,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                item.subtotal.toRupiah(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = DepotifyColors.Navy
                            )
                        }
                        if (!item.transaction_type.isNullOrBlank()) {
                            val typeLabel = when (item.transaction_type) {
                                "top_up" -> "Isi Ulang (Tukar Galon)"
                                "new_deposit" -> "Beli Baru + Deposit"
                                else -> "Beli Tanpa Deposit"
                            }
                            Text("• $typeLabel", fontSize = 10.sp, color = DepotifyColors.TextSecondary)
                        }
                    }
                }

                HorizontalDivider(color = DepotifyColors.Border)

                // Totals & Payment
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL PEMBAYARAN", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = DepotifyColors.Navy)
                        Text(transaction.total_amount.toRupiah(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = DepotifyColors.GreenDark)
                    }

                    if ((transaction.payment_method.equals("cash", ignoreCase = true) || transaction.payment_method.equals("tunai", ignoreCase = true)) && cashReceived > 0) {
                        ReceiptRow("Uang Diterima", cashReceived.toRupiah())
                        ReceiptRow("Kembalian", changeAmount.toRupiah(), isHighlight = true)
                    }
                }

                HorizontalDivider(color = DepotifyColors.Border)

                Text(
                    "Terima kasih telah berbelanja di Depotify.",
                    fontSize = 11.sp,
                    color = DepotifyColors.TextMuted,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(4.dp))

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clayButton(cornerRadius = 14.dp)
                            .clickable {
                                val path = exportReceiptExcel(context, transaction, cashReceived, changeAmount)
                                if (path.isNotBlank()) {
                                    exportedFilePath = path
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Ekspor Struk (Excel)", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clayInset(cornerRadius = 14.dp, backgroundColor = DepotifyColors.SurfaceInset)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Selesai", color = DepotifyColors.Navy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Export Success Dialog
        if (exportedFilePath != null) {
            AlertDialog(
                onDismissRequest = { exportedFilePath = null },
                title = { Text("Ekspor Berhasil", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("File struk penjualan berhasil dibuat dan disimpan ke perangkat.", fontSize = 13.sp, color = DepotifyColors.TextSecondary)
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

@Composable
private fun ReceiptRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = DepotifyColors.TextSecondary)
        Text(
            value,
            fontSize = 12.sp,
            fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isHighlight) DepotifyColors.GreenDark else DepotifyColors.Navy
        )
    }
}

private fun exportReceiptExcel(
    context: android.content.Context,
    transaction: SaleTransaction,
    cashReceived: Double,
    changeAmount: Double
): String {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "Struk_${transaction.displayInvoice}_$timeStamp.csv"

    val csvContent = buildString {
        appendLine("DEPOTIFY — STRUK PENJUALAN")
        appendLine("Invoice,${transaction.displayInvoice}")
        appendLine("Pelanggan,${transaction.displayCustomerName}")
        appendLine("Tanggal,${transaction.created_at ?: "-"}")
        appendLine("Metode Pembayaran,${transaction.payment_method ?: "Cash"}")
        appendLine()
        appendLine("RINCIAN PRODUK")
        appendLine("Nama Produk,Jumlah,Subtotal")
        transaction.items.forEach { item ->
            val safeName = item.product_name.replace(",", " ")
            appendLine("$safeName,${item.qty},${item.subtotal}")
        }
        appendLine()
        appendLine("TOTAL PEMBAYARAN,${transaction.total_amount}")
        if (cashReceived > 0) {
            appendLine("Uang Diterima,$cashReceived")
            appendLine("Kembalian,$changeAmount")
        }
    }

    return com.radityaf.depotify_wrz.utils.ExportUtils.saveCsvToDownloads(context, fileName, csvContent)
}
