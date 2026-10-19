package com.radityaf.depotify_wrz.ui.owner.vendor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun VendorDetailScreen(
    vendorId: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
        topBar = { DepotifyTopBar("Detail Vendor", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader("Master Data", "Vendor #$vendorId", "Informasi lengkap dan kategori pasokan.")

            SectionCard(label = "Profil", title = "Informasi Pemasok") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nama Vendor: PT Sumber Gas Sejahtera", fontWeight = FontWeight.Bold)
                    Text("Contact Person: Bpk. Budi")
                    Text("No. Telepon: 081234567890")
                    Text("Email: info@sumbergas.com")
                    Text("Alamat: Jl. Raya Industri No. 45, Kota")
                }
            }

            SectionCard(label = "Pasokan", title = "Kategori Produk") {
                Text("• LPG 3 Kg & 12 Kg", color = DepotifyColors.Navy)
                Text("• Tabung Gas Kosong", color = DepotifyColors.Navy)
            }
        }
    }
}