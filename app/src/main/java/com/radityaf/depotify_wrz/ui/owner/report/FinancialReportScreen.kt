package com.radityaf.depotify_wrz.ui.owner.report

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.radityaf.depotify_wrz.data.model.Product
import com.radityaf.depotify_wrz.data.model.PurchaseOrder
import com.radityaf.depotify_wrz.data.model.SaleTransaction
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors
import com.radityaf.depotify_wrz.utils.ExportUtils
import com.radityaf.depotify_wrz.utils.FormatUtils.toRupiah
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: FinancialReportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sales by viewModel.sales.collectAsState()
    val purchaseOrders by viewModel.purchaseOrders.collectAsState()
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    val totalOmset by viewModel.totalOmset.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val netProfit by viewModel.netProfit.collectAsState()
    val netLoss by viewModel.netLoss.collectAsState()
    val netBalance by viewModel.netBalance.collectAsState()

    val bestSellingProducts by viewModel.bestSellingProducts.collectAsState()
    val slowMovingProducts by viewModel.slowMovingProducts.collectAsState()

    var exportedFilePath by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Laporan Keuangan", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = DepotifyColors.Navy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DepotifyColors.Surface)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PageHeader("Keuangan", "Laporan Operasional", "Rekapitulasi omset, pengeluaran PO, laba/rugi, dan performa produk.")

                // Export PDF Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clayButton(cornerRadius = 14.dp)
                        .clickable {
                            val path = exportFinancialReportPdf(
                                context, totalOmset, totalExpense, netProfit, netLoss, netBalance,
                                bestSellingProducts, slowMovingProducts, sales, purchaseOrders
                            )
                            if (path.isNotBlank()) {
                                exportedFilePath = path
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text("Ekspor Laporan (PDF)", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }

                // 5 Financial Cards (Bagian 2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pemasukan
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clayCard(cornerRadius = 16.dp, color = DepotifyColors.Surface)
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Pemasukan", fontSize = 10.sp, color = DepotifyColors.TextSecondary)
                            Text(totalOmset.toRupiah(), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = DepotifyColors.GreenDark)
                        }
                    }
                    // Pengeluaran
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clayCard(cornerRadius = 16.dp, color = DepotifyColors.Surface)
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Pengeluaran", fontSize = 10.sp, color = DepotifyColors.TextSecondary)
                            Text(totalExpense.toRupiah(), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = DepotifyColors.Danger)
                        }
                    }
                    // Laba Bersih
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clayCard(cornerRadius = 16.dp, color = DepotifyColors.Surface)
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Laba Bersih", fontSize = 10.sp, color = DepotifyColors.TextSecondary)
                            Text(netProfit.toRupiah(), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = DepotifyColors.GreenDark)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Rugi
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clayCard(cornerRadius = 16.dp, color = DepotifyColors.Surface)
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Rugi", fontSize = 10.sp, color = DepotifyColors.TextSecondary)
                            Text(netLoss.toRupiah(), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = DepotifyColors.Danger)
                        }
                    }
                    // Saldo Bersih
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clayCard(cornerRadius = 16.dp, color = DepotifyColors.Surface)
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Saldo Bersih", fontSize = 10.sp, color = DepotifyColors.TextSecondary)
                            Text(netBalance.toRupiah(), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (netBalance >= 0) DepotifyColors.Navy else DepotifyColors.Danger)
                        }
                    }
                }

                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = DepotifyColors.Surface,
                    contentColor = DepotifyColors.Navy
                ) {
                    Tab(
                        selected = selectedTab == FinancialTab.SUMMARY,
                        onClick = { viewModel.setTab(FinancialTab.SUMMARY) },
                        text = { Text("Ringkasan", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == FinancialTab.SALES,
                        onClick = { viewModel.setTab(FinancialTab.SALES) },
                        text = { Text("Penjualan", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == FinancialTab.PURCHASE_ORDERS,
                        onClick = { viewModel.setTab(FinancialTab.PURCHASE_ORDERS) },
                        text = { Text("PO", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == FinancialTab.PRODUCTS,
                        onClick = { viewModel.setTab(FinancialTab.PRODUCTS) },
                        text = { Text("Performa", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DepotifyColors.Navy)
                    }
                } else if (error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(error ?: "Terjadi kesalahan", color = DepotifyColors.Danger, fontWeight = FontWeight.Bold)
                    }
                } else {
                    when (selectedTab) {
                        FinancialTab.SUMMARY -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clayCard(cornerRadius = 16.dp, color = DepotifyColors.Surface)
                                            .padding(16.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text("Ringkasan Operasional & Keuangan", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 15.sp)
                                            HorizontalDivider(color = DepotifyColors.Border)
                                            SummaryRow("Total Penjualan", "${sales.size} Transaksi")
                                            SummaryRow("Total Purchase Order", "${purchaseOrders.size} PO")
                                            SummaryRow("PO Selesai / Diterima", "${purchaseOrders.count { it.status == "received" || it.status == "completed" }} PO")
                                            SummaryRow("Total Produk Katalog", "${products.size} Produk")
                                            SummaryRow("Status Laporan", "Sinkron & Real-Time")
                                        }
                                    }
                                }
                            }
                        }
                        FinancialTab.SALES -> {
                            if (sales.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Belum ada data penjualan", color = DepotifyColors.TextMuted, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(sales) { sale ->
                                        SaleReportCard(sale)
                                    }
                                }
                            }
                        }
                        FinancialTab.PURCHASE_ORDERS -> {
                            if (purchaseOrders.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Belum ada data Purchase Order", color = DepotifyColors.TextMuted, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(purchaseOrders) { po ->
                                        PoReportCard(po)
                                    }
                                }
                            }
                        }
                        FinancialTab.PRODUCTS -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Text("Barang Paling Laku (Terlaris)", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 14.sp)
                                }
                                if (bestSellingProducts.isEmpty()) {
                                    item {
                                        Text("Belum ada data penjualan produk", fontSize = 12.sp, color = DepotifyColors.TextMuted)
                                    }
                                } else {
                                    items(bestSellingProducts) { item ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clayCard(cornerRadius = 14.dp, color = DepotifyColors.Surface)
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(item.productName, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 13.sp)
                                                    Text("Terjual: ${item.totalQtySold} unit", fontSize = 11.sp, color = DepotifyColors.TextSecondary)
                                                }
                                                Text(item.totalRevenue.toRupiah(), fontWeight = FontWeight.ExtraBold, color = DepotifyColors.GreenDark, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Barang Jarang Dibeli (Slow-Moving)", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 14.sp)
                                }
                                if (slowMovingProducts.isEmpty()) {
                                    item {
                                        Text("Semua produk aktif memiliki transaksi penjualan.", fontSize = 12.sp, color = DepotifyColors.TextMuted)
                                    }
                                } else {
                                    items(slowMovingProducts) { prod ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clayCard(cornerRadius = 14.dp, color = DepotifyColors.Surface)
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(prod.name, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 13.sp)
                                                    Text("Stok: ${prod.stock_content} ${prod.unit}", fontSize = 11.sp, color = DepotifyColors.TextSecondary)
                                                }
                                                Text("Belum Ada Penjualan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DepotifyColors.Danger)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Export Success Dialog
            if (exportedFilePath != null) {
                AlertDialog(
                    onDismissRequest = { exportedFilePath = null },
                    title = { Text("Ekspor PDF Berhasil", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("File Laporan Keuangan PDF berhasil dibuat dan disimpan ke perangkat.", fontSize = 13.sp, color = DepotifyColors.TextSecondary)
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

private fun exportFinancialReportPdf(
    context: android.content.Context,
    totalOmset: Double,
    totalExpense: Double,
    netProfit: Double,
    netLoss: Double,
    netBalance: Double,
    bestSelling: List<ProductPerformanceItem>,
    slowMoving: List<Product>,
    sales: List<SaleTransaction>,
    pos: List<PurchaseOrder>
): String {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "Laporan_Keuangan_$timeStamp.pdf"

    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint()

    var yPos = 40f
    val leftMargin = 40f

    // Title
    paint.isAntiAlias = true
    paint.textSize = 18f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.rgb(15, 23, 42) // Navy
    canvas.drawText("DEPOTIFY — LAPORAN KEUANGAN & OPERASIONAL", leftMargin, yPos, paint)

    yPos += 20f
    paint.textSize = 10f
    paint.isFakeBoldText = false
    paint.color = android.graphics.Color.rgb(100, 116, 139)
    canvas.drawText("Waktu Cetak: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}", leftMargin, yPos, paint)

    yPos += 25f
    // Section 1: Financial Summary (5 Metrics)
    paint.textSize = 14f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.rgb(15, 23, 42)
    canvas.drawText("1. Ringkasan Keuangan Utama", leftMargin, yPos, paint)

    yPos += 18f
    paint.textSize = 11f
    paint.isFakeBoldText = false
    canvas.drawText("• Total Pemasukan: ${totalOmset.toRupiah()}", leftMargin + 10f, yPos, paint)
    yPos += 16f
    canvas.drawText("• Total Pengeluaran PO: ${totalExpense.toRupiah()}", leftMargin + 10f, yPos, paint)
    yPos += 16f
    canvas.drawText("• Laba Bersih: ${netProfit.toRupiah()}", leftMargin + 10f, yPos, paint)
    yPos += 16f
    canvas.drawText("• Rugi: ${netLoss.toRupiah()}", leftMargin + 10f, yPos, paint)
    yPos += 16f
    canvas.drawText("• Saldo Bersih: ${netBalance.toRupiah()}", leftMargin + 10f, yPos, paint)

    yPos += 25f
    // Section 2: Product Performance
    paint.textSize = 14f
    paint.isFakeBoldText = true
    canvas.drawText("2. Analisis Performa Produk", leftMargin, yPos, paint)

    yPos += 18f
    paint.textSize = 12f
    paint.isFakeBoldText = true
    canvas.drawText("Barang Paling Laku (Terlaris):", leftMargin + 10f, yPos, paint)
    yPos += 16f
    paint.textSize = 10f
    paint.isFakeBoldText = false
    if (bestSelling.isEmpty()) {
        canvas.drawText("- Belum ada data penjualan produk", leftMargin + 20f, yPos, paint)
        yPos += 16f
    } else {
        bestSelling.take(5).forEach { item ->
            canvas.drawText("- ${item.productName} (${item.totalQtySold} unit) - ${item.totalRevenue.toRupiah()}", leftMargin + 20f, yPos, paint)
            yPos += 15f
        }
    }

    yPos += 10f
    paint.textSize = 12f
    paint.isFakeBoldText = true
    canvas.drawText("Barang Jarang Dibeli (Slow-Moving):", leftMargin + 10f, yPos, paint)
    yPos += 16f
    paint.textSize = 10f
    paint.isFakeBoldText = false
    if (slowMoving.isEmpty()) {
        canvas.drawText("- Semua produk aktif memiliki transaksi", leftMargin + 20f, yPos, paint)
        yPos += 16f
    } else {
        slowMoving.take(5).forEach { prod ->
            canvas.drawText("- ${prod.name} (Stok: ${prod.stock_content} ${prod.unit})", leftMargin + 20f, yPos, paint)
            yPos += 15f
        }
    }

    yPos += 25f
    // Section 3: Recent Transactions & PO summary
    paint.textSize = 14f
    paint.isFakeBoldText = true
    canvas.drawText("3. Statistik Ringkas", leftMargin, yPos, paint)
    yPos += 18f
    paint.textSize = 11f
    paint.isFakeBoldText = false
    canvas.drawText("• Total Transaksi Selesai: ${sales.size}", leftMargin + 10f, yPos, paint)
    yPos += 16f
    canvas.drawText("• Total Purchase Order: ${pos.size}", leftMargin + 10f, yPos, paint)

    pdfDocument.finishPage(page)

    val outputStream = ByteArrayOutputStream()
    try {
        pdfDocument.writeTo(outputStream)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        pdfDocument.close()
    }

    return ExportUtils.savePdfToDownloads(context, fileName, outputStream.toByteArray())
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = DepotifyColors.TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
    }
}

@Composable
private fun SaleReportCard(sale: SaleTransaction) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clayCard(cornerRadius = 16.dp, color = DepotifyColors.Surface)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(sale.displayInvoice, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 14.sp)
                Text(sale.total_amount.toRupiah(), fontWeight = FontWeight.ExtraBold, color = DepotifyColors.GreenDark, fontSize = 14.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pelanggan: ${sale.displayCustomerName}", fontSize = 12.sp, color = DepotifyColors.TextSecondary)
                Text(sale.status.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
            }
            if (!sale.created_at.isNullOrBlank()) {
                Text(sale.created_at ?: "", fontSize = 11.sp, color = DepotifyColors.TextMuted)
            }
        }
    }
}

@Composable
private fun PoReportCard(po: PurchaseOrder) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clayCard(cornerRadius = 16.dp, color = DepotifyColors.Surface)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(po.displayPoNumber, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 14.sp)
                Text(po.displayTotal.toRupiah(), fontWeight = FontWeight.ExtraBold, color = DepotifyColors.Danger, fontSize = 14.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vendor: ${po.vendor?.name ?: "Supplier"}", fontSize = 12.sp, color = DepotifyColors.TextSecondary)
                Text(po.displayStatus.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
            }
            if (!po.po_date.isNullOrBlank()) {
                Text("Tanggal: ${po.po_date}", fontSize = 11.sp, color = DepotifyColors.TextMuted)
            }
        }
    }
}
