package com.radityaf.depotify_wrz.ui.owner.sync

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
fun SyncScreen(
    modifier: Modifier = Modifier,
    viewModel: SyncViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val message by viewModel.message.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { DepotifyTopBar("Sinkronisasi", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader("Sinkronisasi", "Penyesuaian Data", "Penyelarasan data lokal dengan server.")

            if (!message.isNullOrEmpty()) {
                if (isSuccess) {
                    SuccessBanner(message = message!!) { viewModel.clearMessage() }
                } else {
                    ErrorBanner(message = message!!) { viewModel.clearMessage() }
                }
            }

            // Card Tunggal Sinkronisasi Database
            SectionCard(label = "Sistem", title = "Sinkronisasi Data") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "Proses sinkronisasi akan mengirimkan perubahan data lokal ke server, kemudian memperbarui data master terbaru dari server.",
                        fontSize = 12.sp,
                        color = DepotifyColors.TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status Jaringan:", fontSize = 12.sp, color = DepotifyColors.TextMuted)
                        Box(
                            modifier = Modifier
                                .clayInset(
                                    cornerRadius = 8.dp,
                                    backgroundColor = if (isOnline) DepotifyColors.GreenSoft else DepotifyColors.WarningSoft
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (isOnline) "ONLINE" else "OFFLINE",
                                color = if (isOnline) DepotifyColors.GreenDark else DepotifyColors.Warning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    HorizontalDivider(color = DepotifyColors.Border)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clayButton(cornerRadius = 25.dp, enabled = !isLoading)
                            .clickable(enabled = !isLoading) { viewModel.syncFullDatabase() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Text("Menyelaraskan Data...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else {
                            Text("Sinkronisasi Data", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
