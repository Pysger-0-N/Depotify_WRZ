package com.radityaf.depotify_wrz.ui.stock

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
fun StockHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: StockViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
        topBar = {
            if (onBack != null) {
                DepotifyTopBar("Riwayat Stok", onBack)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(if (onBack != null) padding else PaddingValues(0.dp)).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { PageHeader("Persediaan", "Riwayat Stok", "Pergerakan stok masuk dan keluar.") }

            if (state.items.isEmpty()) {
                item { EmptyState("Belum ada pergerakan stok.") }
            }

            items(state.items, key = { it.id }) { m ->
                val masuk = m.type == "MASUK"
                DepotifyCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(m.productName, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
                            Text(m.date, fontSize = 11.sp, color = DepotifyColors.TextMuted)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                (if (masuk) "+" else "-") + m.qty,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (masuk) DepotifyColors.GreenDark else DepotifyColors.Warning
                            )
                            Spacer(Modifier.height(2.dp))
                            StatusBadge(m.type, positive = masuk)
                        }
                    }
                }
            }
        }
    }
}