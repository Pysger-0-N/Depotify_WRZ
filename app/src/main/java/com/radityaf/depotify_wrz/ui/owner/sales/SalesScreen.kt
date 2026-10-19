package com.radityaf.depotify_wrz.ui.owner.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.data.model.SaleTransaction
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors
import com.radityaf.depotify_wrz.utils.FormatUtils.toRupiah

@OptIn(ExperimentalMaterial3Api::class)
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

    var selectedTab by remember { mutableStateOf(0) }
    val statuses = listOf(null, "pending", "confirmed", "selesai")
    val tabTitles = listOf("Semua", "Pending", "Dikonfirmasi", "Selesai")

    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("Fitur Penjualan", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DepotifyColors.Navy)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader("Daftar Penjualan", "Konfirmasi & Selesaikan Transaksi", "Data customer dan produk dari website.")

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DepotifyColors.Surface,
                contentColor = DepotifyColors.Green,
                edgePadding = 0.dp
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            viewModel.loadSales(statuses[index])
                        },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            if (!errorMessage.isNullOrEmpty()) {
                ErrorBanner(message = errorMessage!!) { viewModel.clearMessages() }
            }
            if (!successMessage.isNullOrEmpty()) {
                SuccessBanner(message = successMessage!!) { viewModel.clearMessages() }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DepotifyColors.Green)
                }
            } else if (sales.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Tidak ada data penjualan", color = DepotifyColors.TextMuted)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sales) { sale ->
                        SaleCard(
                            sale = sale,
                            onConfirm = { viewModel.confirmSale(sale.id) },
                            onComplete = { viewModel.completeSale(sale.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SaleCard(
    sale: SaleTransaction,
    onConfirm: () -> Unit,
    onComplete: () -> Unit
) {
    SectionCard(
        label = "Invoice: ${sale.invoice_number}",
        title = sale.customer_name
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("No. WhatsApp / HP:", color = DepotifyColors.TextMuted, fontSize = 12.sp)
                Text(sale.customer_phone ?: "-", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color.White)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Alamat:", color = DepotifyColors.TextMuted, fontSize = 12.sp)
                Text(sale.customer_address ?: "-", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color.White)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Status:", color = DepotifyColors.TextMuted, fontSize = 12.sp)
                BadgeStatus(status = sale.status)
            }

            HorizontalDivider(color = DepotifyColors.Border)

            Text("Produk yang Dibeli:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DepotifyColors.Green)
            sale.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.product_name} (${item.qty}x)", fontSize = 12.sp, color = Color.White)
                    Text(item.subtotal.toRupiah(), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider(color = DepotifyColors.Border)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Pembayaran:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Text(sale.total_amount.toRupiah(), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DepotifyColors.Green)
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (sale.status == "pending") {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DepotifyColors.Green)
                    ) {
                        Text("Konfirmasi", color = DepotifyColors.Navy, fontWeight = FontWeight.Bold)
                    }
                }
                if (sale.status != "selesai") {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DepotifyColors.Navy)
                    ) {
                        Text("Selesai (Catat DB)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeStatus(status: String) {
    val (color, text) = when (status.lowercase()) {
        "pending" -> DepotifyColors.Warning to "Pending"
        "confirmed" -> DepotifyColors.Green to "Dikonfirmasi"
        "selesai" -> DepotifyColors.Navy to "Selesai"
        else -> DepotifyColors.TextMuted to status
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        color = DepotifyColors.Danger.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(message, color = DepotifyColors.Danger, fontSize = 12.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = null, tint = DepotifyColors.Danger)
            }
        }
    }
}

@Composable
fun SuccessBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        color = DepotifyColors.Green.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(message, color = DepotifyColors.Green, fontSize = 12.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = null, tint = DepotifyColors.Green)
            }
        }
    }
}
