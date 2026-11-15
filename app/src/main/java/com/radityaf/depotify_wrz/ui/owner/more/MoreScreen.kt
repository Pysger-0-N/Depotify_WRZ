package com.radityaf.depotify_wrz.ui.owner.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun MoreScreen(
    modifier: Modifier = Modifier,
    onCategoryClick: () -> Unit = {},
    onVendorClick: () -> Unit = {},
    onStockClick: () -> Unit = {},
    onAddStockMovementClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onSalesClick: () -> Unit = {},
    onKasirClick: () -> Unit = {},
    onFinancialReportClick: () -> Unit = {},
    onSyncClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader("Menu", "Lainnya", "Akses fitur dan pengaturan.")

            SectionCard(label = "Keuangan", title = "Laporan Keuangan") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoreMenuItem("Laporan Keuangan", Icons.Default.Star, onFinancialReportClick, color = DepotifyColors.Navy)
                }
            }

            SectionCard(label = "Penjualan", title = "Transaksi") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoreMenuItem("Kasir", Icons.Default.ShoppingCart, onKasirClick, color = DepotifyColors.Navy)
                    HorizontalDivider(color = DepotifyColors.Border)
                    MoreMenuItem("Daftar Penjualan", Icons.Default.List, onSalesClick)
                    HorizontalDivider(color = DepotifyColors.Border)
                    MoreMenuItem("Sinkronisasi Data", Icons.Default.Refresh, onSyncClick)
                }
            }

            SectionCard(label = "Master Data", title = "Data Utama") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoreMenuItem("Kategori Produk", Icons.Default.List, onCategoryClick)
                    HorizontalDivider(color = DepotifyColors.Border)
                    MoreMenuItem("Vendor", Icons.Default.Person, onVendorClick)
                }
            }

            SectionCard(label = "Persediaan", title = "Stok") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoreMenuItem("Riwayat Stok", Icons.Default.DateRange, onStockClick)
                    HorizontalDivider(color = DepotifyColors.Border)
                    MoreMenuItem("Tambah Pergerakan Stok", Icons.Default.Add, onAddStockMovementClick)
                    HorizontalDivider(color = DepotifyColors.Border)
                    MoreMenuItem("Notifikasi Stok", Icons.Default.Notifications, onNotificationClick)
                }
            }

            SectionCard(label = "Akun", title = "Pengguna") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoreMenuItem("Profil", Icons.Default.AccountCircle, onProfileClick)
                    HorizontalDivider(color = DepotifyColors.Border)
                    MoreMenuItem("Keluar", Icons.Default.ExitToApp, onLogout, color = DepotifyColors.Danger)
                }
            }
        }
    }
}

@Composable
private fun MoreMenuItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color = DepotifyColors.Navy
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clayInset(cornerRadius = 10.dp, backgroundColor = DepotifyColors.SurfaceInset)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(title, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = DepotifyColors.TextMuted)
    }
}
