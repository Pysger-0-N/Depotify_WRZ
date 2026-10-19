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
    onSyncClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader("Menu Utama", "Lainnya", "Akses master data, stok, notifikasi, dan profil.")

            SectionCard(label = "Penjualan & Operasional", title = "Transaksi & Laporan") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoreMenuItem("Penjualan", Icons.Default.ShoppingCart, onSalesClick)
                    HorizontalDivider(color = DepotifyColors.Border)
                    MoreMenuItem("Sinkronisasi Room ke Server DB", Icons.Default.Refresh, onSyncClick)
                }
            }

            SectionCard(label = "Master Data", title = "Data Pendukung") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoreMenuItem("Kategori Produk", Icons.Default.List, onCategoryClick)
                    HorizontalDivider(color = DepotifyColors.Border)
                    MoreMenuItem("Vendor / Pemasok", Icons.Default.Person, onVendorClick)
                }
            }

            SectionCard(label = "Persediaan & Operasional", title = "Stok & Logistik") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoreMenuItem("Riwayat Pergerakan Stok", Icons.Default.DateRange, onStockClick)
                    HorizontalDivider(color = DepotifyColors.Border)
                    MoreMenuItem("Tambah Pergerakan Manual", Icons.Default.Add, onAddStockMovementClick)
                    HorizontalDivider(color = DepotifyColors.Border)
                    MoreMenuItem("Notifikasi Stok Menipis", Icons.Default.Notifications, onNotificationClick)
                }
            }

            SectionCard(label = "Akun", title = "Pengaturan Pengguna") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoreMenuItem("Profil Pengguna", Icons.Default.AccountCircle, onProfileClick)
                    HorizontalDivider(color = DepotifyColors.Border)
                    MoreMenuItem("Keluar Aplikasi", Icons.Default.ExitToApp, onLogout, color = DepotifyColors.Danger)
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
    color: androidx.compose.ui.graphics.Color = DepotifyColors.Navy
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = title, tint = color)
            Text(title, fontWeight = FontWeight.SemiBold, color = color, fontSize = 14.sp)
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = DepotifyColors.TextMuted)
    }
}
