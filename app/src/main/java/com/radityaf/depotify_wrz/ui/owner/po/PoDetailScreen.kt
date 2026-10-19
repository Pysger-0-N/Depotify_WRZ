package com.radityaf.depotify_wrz.ui.owner.po

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun PoDetailScreen(
    poId: Int,
    modifier: Modifier = Modifier,
    viewModel: PurchaseOrderViewModel = hiltViewModel(),
    onReceiveClick: (Int) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val po = state.items.find { it.id == poId }

    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
        topBar = { DepotifyTopBar("Detail PO", onBack) }
    ) { padding ->
        if (po == null) {
            Box(Modifier.padding(padding).padding(16.dp)) { EmptyState("PO tidak ditemukan.") }
            return@Scaffold
        }

        Column(
            modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PageHeader("Pembelian", "PO #${po.id}", po.vendorName)

            DepotifyCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Status", fontSize = 13.sp, color = DepotifyColors.TextSecondary)
                    StatusBadge(po.status, positive = po.status == "DITERIMA")
                }
            }

            SectionCard(label = "Rincian", title = "Item Pesanan") {
                po.items.forEachIndexed { index, item ->
                    if (index > 0) HorizontalDivider(color = DepotifyColors.Border, modifier = Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(item.productName, fontWeight = FontWeight.SemiBold, color = DepotifyColors.Navy)
                            Text(
                                "${item.qty} x ${formatRupiah(item.price)}",
                                fontSize = 12.sp,
                                color = DepotifyColors.TextSecondary
                            )
                        }
                        Text(formatRupiah(item.qty * item.price), fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                    }
                }
                HorizontalDivider(color = DepotifyColors.Border, modifier = Modifier.padding(vertical = 12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                    Text(formatRupiah(po.total), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = DepotifyColors.Navy)
                }
            }

            if (po.status == "PENDING") {
                DepotifyButton("Barang Telah Diterima", { onReceiveClick(po.id) }, Modifier.fillMaxWidth())
            }
        }
    }
}