package com.radityaf.depotify_wrz.ui.vendor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radityaf.depotify_wrz.data.model.User
import com.radityaf.depotify_wrz.ui.profile.ProfileScreen
import com.radityaf.depotify_wrz.ui.vendor.po.VendorPoListScreen
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

private enum class VendorTab(val label: String) {
    PO("Purchase Order"), PROFIL("Profil")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorMainScreen(
    user: User,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    var tab by remember { mutableStateOf(VendorTab.PO) }

    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(DepotifyColors.Green),
                            contentAlignment = Alignment.Center
                        ) { Text("V", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy) }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Depotify Vendor", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(user.name, color = DepotifyColors.TextMuted, fontSize = 11.sp)
                        }
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Keluar", color = DepotifyColors.Green, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DepotifyColors.Navy)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = DepotifyColors.Surface) {
                VendorTab.values().forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = {
                            Icon(
                                when (t) {
                                    VendorTab.PO -> Icons.Default.ShoppingCart
                                    VendorTab.PROFIL -> Icons.Default.Person
                                },
                                contentDescription = t.label
                            )
                        },
                        label = { Text(t.label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DepotifyColors.Navy,
                            selectedTextColor = DepotifyColors.Navy,
                            indicatorColor = DepotifyColors.Green,
                            unselectedIconColor = DepotifyColors.TextMuted,
                            unselectedTextColor = DepotifyColors.TextMuted
                        )
                    )
                }
            }
        }
    ) { padding ->
        when (tab) {
            VendorTab.PO -> VendorPoListScreen(modifier = Modifier.padding(padding))
            VendorTab.PROFIL -> ProfileScreen(user = user, modifier = Modifier.padding(padding), onLogout = onLogout)
        }
    }
}