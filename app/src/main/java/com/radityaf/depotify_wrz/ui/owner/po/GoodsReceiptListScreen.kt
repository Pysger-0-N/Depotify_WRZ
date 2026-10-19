package com.radityaf.depotify_wrz.ui.owner.po

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun GoodsReceiptListScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onReceiptClick: (Int) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
        topBar = { DepotifyTopBar("Penerimaan Barang", onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { PageHeader("Riwayat", "Goods Receipts", "Daftar penerimaan barang dari purchase order.") }
            item { EmptyState("Belum ada riwayat penerimaan barang.") }
        }
    }
}