package com.radityaf.depotify_wrz.ui.owner.kasir

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun KasirReceiptDialog(
    transaction: SaleTransaction,
    cashReceived: Double = 0.0,
    changeAmount: Double = 0.0,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clayInset(cornerRadius = 12.dp, backgroundColor = DepotifyColors.SurfaceInset)
                                .clickable {
                                    shareReceiptText(context, transaction, cashReceived, changeAmount)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Bagikan Struk", color = DepotifyColors.Navy, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clayInset(cornerRadius = 12.dp, backgroundColor = DepotifyColors.SurfaceInset)
                                .clickable {
                                    exportReceiptExcel(context, transaction, cashReceived, changeAmount)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Ekspor Excel / CSV", color = DepotifyColors.GreenDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clayButton(cornerRadius = 14.dp)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Selesai", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
            }
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

private fun shareReceiptText(
    context: android.content.Context,
    transaction: SaleTransaction,
    cashReceived: Double,
    changeAmount: Double
) {
    val builder = StringBuilder()
    builder.appendLine("*DEPOTIFY — STRUK PENJUALAN*")
    builder.appendLine("Invoice: ${transaction.displayInvoice}")
    builder.appendLine("Pelanggan: ${transaction.displayCustomerName}")
    builder.appendLine("--------------------------------")
    transaction.items.forEach { item ->
        builder.appendLine("${item.product_name} (${item.qty}x) : ${item.subtotal.toRupiah()}")
    }
    builder.appendLine("--------------------------------")
    builder.appendLine("TOTAL: ${transaction.total_amount.toRupiah()}")
    if ((transaction.payment_method.equals("cash", ignoreCase = true) || transaction.payment_method.equals("tunai", ignoreCase = true)) && cashReceived > 0) {
        builder.appendLine("TUNAI: ${cashReceived.toRupiah()}")
        builder.appendLine("KEMBALI: ${changeAmount.toRupiah()}")
    }
    builder.appendLine("--------------------------------")
    builder.appendLine("Terima kasih telah berbelanja di Depotify!")

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, builder.toString())
    }
    context.startActivity(Intent.createChooser(shareIntent, "Bagikan Struk Penjualan"))
}

private fun exportReceiptExcel(
    context: android.content.Context,
    transaction: SaleTransaction,
    cashReceived: Double,
    changeAmount: Double
) {
    val builder = StringBuilder()
    builder.appendLine("DEPOTIFY — LAPORAN STRUK TRANSAKSI (CSV/EXCEL FORMAT)")
    builder.appendLine("Invoice,${transaction.displayInvoice}")
    builder.appendLine("Pelanggan,${transaction.displayCustomerName}")
    builder.appendLine("Tanggal,${transaction.created_at ?: "-"}")
    builder.appendLine("Metode Pembayaran,${transaction.payment_method ?: "Cash"}")
    builder.appendLine("---")
    builder.appendLine("Nama Produk,Jumlah,Subtotal")
    transaction.items.forEach { item ->
        builder.appendLine("${item.product_name},${item.qty},${item.subtotal}")
    }
    builder.appendLine("---")
    builder.appendLine("Total Pembayaran,,${transaction.total_amount}")
    if (cashReceived > 0) {
        builder.appendLine("Uang Diterima,,$cashReceived")
        builder.appendLine("Kembalian,,$changeAmount")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_TEXT, builder.toString())
        putExtra(Intent.EXTRA_SUBJECT, "Struk_${transaction.displayInvoice}.csv")
    }
    context.startActivity(Intent.createChooser(intent, "Ekspor Struk ke Excel / CSV"))
}
