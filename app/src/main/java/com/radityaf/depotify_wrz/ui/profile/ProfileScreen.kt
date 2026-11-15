package com.radityaf.depotify_wrz.ui.profile

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
        containerColor = Color.Transparent,
        topBar = { DepotifyTopBar("Profil Pengguna", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader("Akun", "Profil", "Informasi akun pengguna.")

            SectionCard(label = "Akun", title = user.name) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Email:", color = DepotifyColors.TextSecondary, fontSize = 13.sp)
                        Text(user.email, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 13.sp)
                    }
                    HorizontalDivider(color = DepotifyColors.Border)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Peran Hak Akses:", color = DepotifyColors.TextSecondary, fontSize = 13.sp)
                        Box(
                            Modifier.clayInset(cornerRadius = 8.dp, backgroundColor = DepotifyColors.GreenSoft)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(user.role.uppercase(), color = DepotifyColors.GreenDark, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                        }
                    }
                    if (user.vendor_id != null) {
                        HorizontalDivider(color = DepotifyColors.Border)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ID Vendor:", color = DepotifyColors.TextSecondary, fontSize = 13.sp)
                            Text("#${user.vendor_id}", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy, fontSize = 13.sp)
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clayInset(cornerRadius = 26.dp, backgroundColor = DepotifyColors.WarningSoft)
                    .clickable { onLogout() },
                contentAlignment = Alignment.Center
            ) {
                Text("Keluar", color = DepotifyColors.Danger, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            }
        }
    }
}
