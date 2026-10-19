package com.radityaf.depotify_wrz.ui.owner.po

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun PoListScreen(
    modifier: Modifier = Modifier,
    viewModel: PurchaseOrderViewModel = hiltViewModel(),
    onPoClick: (Int) -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // sudah ditangani Scaffold di MainScreen
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = DepotifyColors.Green,
                contentColor = DepotifyColors.Navy
            ) { Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { PageHeader("Pembelian", "Purchase Order", "Daftar pesanan barang ke vendor.") }

            if (state.items.isEmpty()) {
                item { EmptyState("Belum ada purchase order.") }
            }

            items(state.items, key = { it.id }) { po ->
                DepotifyCard(Modifier.fillMaxWidth().clickable { onPoClick(po.id) }) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PO #${po.id}", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                        StatusBadge(po.status, positive = po.status == "DITERIMA")
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(po.vendorName, fontSize = 13.sp, color = DepotifyColors.TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        formatRupiah(po.total),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = DepotifyColors.Navy
                    )
                }
            }
        }
    }
}