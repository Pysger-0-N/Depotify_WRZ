package com.radityaf.depotify_wrz.ui.vendor.po

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun VendorPoListScreen(
    modifier: Modifier = Modifier,
    onPoClick: (Int) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { PageHeader("Vendor Portal", "Purchase Orders", "Daftar pesanan pembelian masuk (Draft disembunyikan).") }
            item { EmptyState("Belum ada Purchase Order dari depot.") }
        }
    }
}