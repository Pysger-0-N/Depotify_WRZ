package com.radityaf.depotify_wrz.ui.owner.po

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.owner.sales.ErrorBanner
import com.radityaf.depotify_wrz.ui.owner.sales.SuccessBanner
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
    var isConfirmedChecked by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { DepotifyTopBar("Penerimaan Barang", onBack) }
    ) { padding ->
        if (po == null) {
            Box(Modifier.padding(padding).padding(16.dp)) { EmptyState("PO tidak ditemukan.") }
            return@Scaffold
        }

        Column(
            modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader("Penerimaan", "PO #${po.id}", po.vendorName)

            if (!state.errorMessage.isNullOrBlank()) {
                ErrorBanner(message = state.errorMessage!!) { viewModel.clearMessages() }
            }
            if (!state.successMessage.isNullOrBlank()) {
                SuccessBanner(message = state.successMessage!!) { viewModel.clearMessages() }
            }

            SectionCard(label = "Barang", title = "Barang yang Diterima") {
                po.items.forEachIndexed { index, item ->
                    if (index > 0) HorizontalDivider(color = DepotifyColors.Border, modifier = Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.productName, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 14.sp)
                        Text("x${item.qty}", fontSize = 13.sp, color = DepotifyColors.GreenDark, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            SectionCard(label = "Catatan & Konfirmasi", title = "Keterangan Penerimaan") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DepotifyTextField("Catatan (opsional)", note, { note = it }, placeholder = "Misal: 1 tabung penyok")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isConfirmedChecked,
                            onCheckedChange = { isConfirmedChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = DepotifyColors.GreenDark)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Barang diterima dengan benar dan sesuai",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DepotifyColors.Navy,
                            modifier = Modifier.clickable { isConfirmedChecked = !isConfirmedChecked }
                        )
                    }
                }
            }

            DepotifyButton(
                text = if (state.isLoading) "Memproses..." else "Konfirmasi Terima Barang",
                onClick = {
                    viewModel.receiveGoods(po.id, note) {
                        onDone()
                    }
                },
                enabled = !state.isLoading && isConfirmedChecked,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
