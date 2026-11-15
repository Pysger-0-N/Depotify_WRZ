package com.radityaf.depotify_wrz.ui.unsupported

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun UnsupportedScreen(
    message: String = "Akses akun ini belum didukung di aplikasi mobile",
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Akses Dibatasi",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DepotifyColors.Navy
                )
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = DepotifyColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                DepotifyButton(
                    text = "Keluar Akun",
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}