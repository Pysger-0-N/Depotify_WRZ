package com.radityaf.depotify_wrz.ui.sync

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.ui.components.ClayBackground
import com.radityaf.depotify_wrz.ui.components.clayButton
import com.radityaf.depotify_wrz.ui.components.clayCard
import com.radityaf.depotify_wrz.ui.components.clayInset
import com.radityaf.depotify_wrz.ui.owner.sync.SyncViewModel
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors
import kotlinx.coroutines.delay

@Composable
fun InitialSyncScreen(
    onSyncComplete: () -> Unit,
    viewModel: SyncViewModel = hiltViewModel()
) {
    var statusText by remember { mutableStateOf("Memeriksa jaringan...") }
    val isOnline by viewModel.isOnline.collectAsState()

    LaunchedEffect(Unit) {
        if (isOnline) {
            statusText = "Menyinkronkan data dari Server Database..."
            viewModel.syncFullDatabase()
            delay(500)
            statusText = "Menyiapkan Room DB lokal..."
            delay(300)
        } else {
            statusText = "Perangkat Offline. Memuat data dari Room DB..."
            delay(800)
        }
        onSyncComplete()
    }

    ClayBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clayCard(cornerRadius = 28.dp, color = DepotifyColors.Surface)
                    .padding(28.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clayButton(cornerRadius = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "D",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Depotify Sync",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DepotifyColors.Navy
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = statusText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = DepotifyColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = DepotifyColors.Green,
                        strokeWidth = 3.5.dp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .clayInset(
                                cornerRadius = 12.dp,
                                backgroundColor = if (isOnline) DepotifyColors.GreenSoft else DepotifyColors.WarningSoft
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isOnline) "● Online - Terhubung ke Server" else "○ Offline - Mode Room DB Lokal",
                            color = if (isOnline) DepotifyColors.GreenDark else DepotifyColors.Warning,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
