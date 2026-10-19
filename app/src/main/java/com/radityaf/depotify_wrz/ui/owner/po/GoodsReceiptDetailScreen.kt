package com.radityaf.depotify_wrz.ui.owner.po

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
fun GoodsReceiptDetailScreen(
    receiptId: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
        topBar = { DepotifyTopBar("Detail Penerimaan", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader("Riwayat", "Receipt #$receiptId", "Informasi detail dokumen penerimaan barang.")

            SectionCard(label = "Dokumen", title = "Informasi QC") {
                Text("No. Dokumen: DO-2026-001", fontWeight = FontWeight.Bold)
                Text("Tipe Dokumen: Surat Jalan / DO")
                Text("Tanggal Terima: 07 Oct 2026")
            }
        }
    }
}