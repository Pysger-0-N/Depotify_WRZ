package com.radityaf.depotify_wrz.ui.owner.sync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    modifier: Modifier = Modifier,
    viewModel: SyncViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("Sinkronisasi Database", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DepotifyColors.Navy)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader("Room DB ke Server DB", "Sinkronisasi Data Lokal", "Kirim seluruh perubahan data lokal Room database ke database server utama.")

            if (!message.isNullOrEmpty()) {
                if (isSuccess) {
                    SuccessBanner(message = message!!) { viewModel.clearMessage() }
                } else {
                    ErrorBanner(message = message!!) { viewModel.clearMessage() }
                }
            }

            SectionCard(label = "Sinkronisasi", title = "Kirim Data Lokal ke Server") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Proses ini akan mengirimkan data produk, kategori, vendor, dan pergerakan stok dari Room Database di perangkat ke Database Server.",
                        fontSize = 13.sp,
                        color = DepotifyColors.TextMuted
                    )

                    Button(
                        onClick = { viewModel.syncData() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DepotifyColors.Green),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DepotifyColors.Navy)
                            Spacer(Modifier.width(8.dp))
                            Text("Mengirim Data...", color = DepotifyColors.Navy, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = DepotifyColors.Navy)
                            Spacer(Modifier.width(8.dp))
                            Text("Kirim ke Server Database", color = DepotifyColors.Navy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
