package com.radityaf.depotify_wrz.ui.vendor.po

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
fun VendorPoDetailScreen(
    poId: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
        topBar = { DepotifyTopBar("Detail PO Vendor", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader("Purchase Order", "PO #$poId", "Detail pesanan (Read-Only) dan konfirmasi status.")

            SectionCard(label = "Informasi", title = "Ringkasan PO") {
                Text("Status: Ordered", fontWeight = FontWeight.Bold, color = DepotifyColors.GreenDark)
                Text("Tanggal: 07 Oct 2026")
                Text("Total: Rp 1.500.000")
            }

            // Tombol aksi D.5(b): Konfirmasi, Tolak, Tandai Dikirim
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                DepotifyButton(text = "Konfirmasi", onClick = {}, modifier = Modifier.weight(1f))
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = DepotifyColors.Danger),
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) { Text("Tolak", fontWeight = FontWeight.Bold) }
            }
        }
    }
}