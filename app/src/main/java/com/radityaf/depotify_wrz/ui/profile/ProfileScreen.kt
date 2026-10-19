package com.radityaf.depotify_wrz.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.radityaf.depotify_wrz.data.model.User
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun ProfileScreen(
    user: User,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
        topBar = { DepotifyTopBar("", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader("Akun", "Profil Pengguna", "Informasi kredensial dan hak akses.")

            SectionCard(label = "Akun", title = user.name) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Email: ${user.email}", fontWeight = FontWeight.Medium)
                    Text("Role: ${user.role.uppercase()}", color = DepotifyColors.GreenDark, fontWeight = FontWeight.Bold)
                    if (user.vendor_id != null) {
                        Text("Vendor ID: ${user.vendor_id}")
                    }
                }
            }

            DepotifyButton(
                text = "Keluar (Logout)",
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}