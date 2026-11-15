package com.radityaf.depotify_wrz.ui.owner.report

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
import com.radityaf.depotify_wrz.data.model.PurchaseOrder
import com.radityaf.depotify_wrz.data.model.SaleTransaction
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors
import com.radityaf.depotify_wrz.utils.FormatUtils.toRupiah

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
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    val totalOmset by viewModel.totalOmset.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val estimatedProfit by viewModel.estimatedProfit.collectAsState()

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
                PageHeader("Keuangan", "Laporan Operasional", "Rekapitulasi omset penjualan, pengeluaran pembelian, dan laba kotor.")

                // Export Excel/CSV Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clayButton(cornerRadius = 14.dp)
                        .clickable {
                            val path = exportFinancialReport(context, totalOmset, totalExpense, estimatedProfit, sales, purchaseOrders)
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
                        Text("Ekspor Laporan (Excel)", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }

                // Financial Summary KPI Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clayCard(cornerRadius = 16.dp, color = DepotifyColors.Surface)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Penjualan", fontSize = 11.sp, color = DepotifyColors.TextSecondary)
                            Text(totalOmset.toRupiah(), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = DepotifyColors.GreenDark)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clayCard(cornerRadius = 16.dp, color = DepotifyColors.Surface)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Pengeluaran PO", fontSize = 11.sp, color = DepotifyColors.TextSecondary)
                            Text(totalExpense.toRupiah(), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = DepotifyColors.Danger)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clayCard(cornerRadius = 16.dp, color = DepotifyColors.Surface)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Estimasi Laba Kotor", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DepotifyColors.TextSecondary)
                            Text(estimatedProfit.toRupiah(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = if (estimatedProfit >= 0) DepotifyColors.GreenDark else DepotifyColors.Danger)
                        }
                        Box(
                            modifier = Modifier
                                .clayInset(cornerRadius = 10.dp, backgroundColor = DepotifyColors.SurfaceInset)
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = DepotifyColors.Warning, modifier = Modifier.size(24.dp))
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
                        text = { Text("Ringkasan", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == FinancialTab.SALES,
                        onClick = { viewModel.setTab(FinancialTab.SALES) },
                        text = { Text("Penjualan", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == FinancialTab.PURCHASE_ORDERS,
                        onClick = { viewModel.setTab(FinancialTab.PURCHASE_ORDERS) },
                        text = { Text("Purchase Order", fontWeight = FontWeight.Bold) }
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
                                            Text("Ringkasan Operasional", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 15.sp)
                                            HorizontalDivider(color = DepotifyColors.Border)
                                            SummaryRow("Total Penjualan", "${sales.size} Transaksi")
                                            SummaryRow("Total Purchase Order", "${purchaseOrders.size} PO")
                                            SummaryRow("PO Selesai", "${purchaseOrders.count { it.status == "received" || it.status == "completed" }} PO")
                                            SummaryRow("Status Sistem", "Online & Sinkron")
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
                            Text("File laporan keuangan berhasil dibuat dan disimpan ke perangkat.", fontSize = 13.sp, color = DepotifyColors.TextSecondary)
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

private fun exportFinancialReport(
    context: android.content.Context,
    totalOmset: Double,
    totalExpense: Double,
    profit: Double,
    sales: List<SaleTransaction>,
    pos: List<PurchaseOrder>
): String {
    val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
    val fileName = "Laporan_Keuangan_$timeStamp.csv"

    val csvContent = buildString {
        appendLine("DEPOTIFY — LAPORAN KEUANGAN")
        appendLine("Waktu Ekspor,${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
        appendLine()
        appendLine("RINGKASAN KEUANGAN")
        appendLine("Total Omset,${totalOmset}")
        appendLine("Total Pengeluaran PO,${totalExpense}")
        appendLine("Estimasi Laba Kotor,$profit")
        appendLine()
        appendLine("DAFTAR PENJUALAN")
        appendLine("Invoice,Pelanggan,Total,Status,Tanggal")
        sales.forEach { s ->
            val safeCustomer = s.displayCustomerName.replace(",", " ")
            appendLine("${s.displayInvoice},$safeCustomer,${s.total_amount},${s.status},${s.created_at ?: "-"}")
        }
        appendLine()
        appendLine("DAFTAR PURCHASE ORDER")
        appendLine("No PO,Vendor,Total,Status,Tanggal")
        pos.forEach { p ->
            val safeVendor = (p.vendor?.name ?: "Supplier").replace(",", " ")
            appendLine("${p.displayPoNumber},$safeVendor,${p.displayTotal},${p.displayStatus},${p.po_date ?: "-"}")
        }
    }

    return com.radityaf.depotify_wrz.utils.ExportUtils.saveCsvToDownloads(context, fileName, csvContent)
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
