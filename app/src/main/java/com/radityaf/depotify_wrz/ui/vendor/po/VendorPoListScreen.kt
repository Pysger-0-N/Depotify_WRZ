package com.radityaf.depotify_wrz.ui.vendor.po

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun VendorPoListScreen(
    modifier: Modifier = Modifier,
    viewModel: VendorPoViewModel = hiltViewModel(),
    onPoClick: (Int) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent
    ) { padding ->
        DepotifyRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PageHeader("Vendor", "Purchase Order", "Daftar pesanan masuk dari Owner.")
                }

                if (!state.errorMessage.isNullOrBlank()) {
                    item { ErrorBanner(message = state.errorMessage!!) { viewModel.clearMessages() } }
                }
                if (!state.successMessage.isNullOrBlank()) {
                    item { SuccessBanner(message = state.successMessage!!) { viewModel.clearMessages() } }
                }

                if (state.items.isEmpty()) {
                    item { EmptyState("Belum ada Purchase Order dari depot Owner.") }
                } else {
                    items(state.items, key = { it.id }) { po ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clayCard(cornerRadius = 20.dp)
                                .clickable { onPoClick(po.id) }
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("PO #${po.id}", fontWeight = FontWeight.ExtraBold, color = DepotifyColors.Navy, fontSize = 16.sp)
                                    PoStatusBadge(po.status)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("Vendor: ${po.vendorName}", fontSize = 13.sp, color = DepotifyColors.TextSecondary)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    formatRupiah(po.total),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = DepotifyColors.GreenDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
