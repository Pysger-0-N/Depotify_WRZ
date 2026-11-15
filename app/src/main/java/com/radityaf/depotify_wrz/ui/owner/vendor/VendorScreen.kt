package com.radityaf.depotify_wrz.ui.owner.vendor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.data.local.entity.VendorEntity
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun VendorScreen(
    modifier: Modifier = Modifier,
    viewModel: VendorViewModel = hiltViewModel(),
    onVendorClick: (Int) -> Unit = {},
    onAddVendorClick: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    var vendorToDelete by remember { mutableStateOf<VendorEntity?>(null) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { DepotifyTopBar("Vendor", onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PageHeader("Master Data", "Vendor", "Daftar supplier dan pemasok.")
            }

            item {
                DepotifyButton(
                    text = "+ Tambah Vendor Baru",
                    onClick = onAddVendorClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                SectionCard(label = "Data", title = "Daftar Vendor") {
                    if (state.items.isEmpty()) {
                        EmptyState("Belum ada vendor.")
                    } else {
                        state.items.forEachIndexed { index, vendor ->
                            if (index > 0) HorizontalDivider(color = DepotifyColors.Border, modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onVendorClick(vendor.id) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        vendor.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = DepotifyColors.Navy
                                    )
                                    if (!vendor.phone.isNullOrBlank()) {
                                        Text(
                                            "Telp: ${vendor.phone}",
                                            fontSize = 12.sp,
                                            color = DepotifyColors.TextSecondary
                                        )
                                    }
                                    if (!vendor.address.isNullOrBlank()) {
                                        Text(
                                            "Alamat: ${vendor.address}",
                                            fontSize = 12.sp,
                                            color = DepotifyColors.TextSecondary
                                        )
                                    }
                                }
                                TextButton(onClick = { vendorToDelete = vendor }) {
                                    Text("Hapus", color = DepotifyColors.Danger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    vendorToDelete?.let { vendor ->
        DepotifyConfirmDialog(
            title = "Hapus Supplier",
            message = "Hapus supplier '${vendor.name}'?",
            confirmText = "Hapus",
            onConfirm = {
                viewModel.delete(vendor.id)
                vendorToDelete = null
            },
            onDismiss = { vendorToDelete = null }
        )
    }
}

