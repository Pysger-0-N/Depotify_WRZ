package com.radityaf.depotify_wrz.ui.owner.po

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun GoodsReceiptFormScreen(
    poId: Int,
    modifier: Modifier = Modifier,
    viewModel: PurchaseOrderViewModel = hiltViewModel(),
    onDone: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val po = state.items.find { it.id == poId }
    var note by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
        topBar = { DepotifyTopBar("Penerimaan Barang", onBack) }
    ) { padding ->
        if (po == null) {
            Box(Modifier.padding(padding).padding(16.dp)) { EmptyState("PO tidak ditemukan.") }
            return@Scaffold
        }

        Column(
            modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PageHeader("Penerimaan", "PO #${po.id}", po.vendorName)

            SectionCard(label = "Barang", title = "Barang yang Diterima") {
                po.items.forEachIndexed { index, item ->
                    if (index > 0) HorizontalDivider(color = DepotifyColors.Border, modifier = Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.productName, fontWeight = FontWeight.SemiBold, color = DepotifyColors.Navy)
                        Text("x${item.qty}", fontSize = 13.sp, color = DepotifyColors.TextSecondary)
                    }
                }
            }

            SectionCard(label = "Catatan", title = "Keterangan") {
                DepotifyTextField("Catatan (opsional)", note, { note = it }, placeholder = "Misal: 1 tabung penyok")
            }

            DepotifyButton(
                text = "Konfirmasi Terima",
                onClick = { viewModel.receiveGoods(po.id); onDone() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}