package com.radityaf.depotify_wrz.ui.owner.notification

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
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun NotificationScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onProductClick: (Int) -> Unit = {},
    onAddStockClick: (Int) -> Unit = {},
    onCreatePoClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { DepotifyTopBar("Notifikasi Stok", onBack) }
    ) { padding ->
        DepotifyRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    PageHeader(
                        label = if (state.isOnline) "Online" else "Offline",
                        title = "Notifikasi Stok",
                        subtitle = "Daftar peringatan stok menipis dan rekomendasi pengisian stok."
                    )
                }

                if (state.items.isEmpty()) {
                    item {
                        EmptyState("Semua stok produk aman. Tidak ada notifikasi stok menipis.")
                    }
                } else {
                    items(state.items, key = { it.id }) { notification ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clayCard(cornerRadius = 18.dp)
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                notification.productName,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = DepotifyColors.Navy,
                                                fontSize = 15.sp,
                                                modifier = Modifier.clickable { onProductClick(notification.productId) }
                                            )
                                            if (notification.isRead) {
                                                Spacer(Modifier.width(6.dp))
                                                Text("Dibaca", fontSize = 10.sp, color = DepotifyColors.TextMuted)
                                            }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            notification.message,
                                            fontSize = 12.sp,
                                            color = DepotifyColors.TextSecondary
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clayInset(
                                                cornerRadius = 10.dp,
                                                backgroundColor = if (notification.currentStock == 0) DepotifyColors.WarningSoft else DepotifyColors.GreenSoft
                                            )
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${notification.currentStock} ${notification.unit}",
                                            color = if (notification.currentStock == 0) DepotifyColors.Danger else DepotifyColors.GreenDark,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                HorizontalDivider(color = DepotifyColors.Border, thickness = 0.8.dp)

                                // Quick Actions Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .clayButton(cornerRadius = 10.dp)
                                                .clickable { onAddStockClick(notification.productId) }
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Text("+ Restok Manual", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clayInset(cornerRadius = 10.dp, backgroundColor = DepotifyColors.SurfaceInset)
                                                .clickable { onCreatePoClick() }
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Text("+ Buat PO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                                        }
                                    }

                                    if (!notification.isRead) {
                                        Text(
                                            text = "Tandai Dibaca",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DepotifyColors.TextMuted,
                                            modifier = Modifier.clickable { viewModel.markAsRead(notification.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
