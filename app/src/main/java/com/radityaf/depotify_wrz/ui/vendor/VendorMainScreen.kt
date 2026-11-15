package com.radityaf.depotify_wrz.ui.vendor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.radityaf.depotify_wrz.data.model.User
import com.radityaf.depotify_wrz.ui.components.ClayBackground
import com.radityaf.depotify_wrz.ui.components.clayButton
import com.radityaf.depotify_wrz.ui.components.clayCard
import com.radityaf.depotify_wrz.ui.components.clayInset
import com.radityaf.depotify_wrz.ui.profile.ProfileScreen
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors
import com.radityaf.depotify_wrz.ui.vendor.po.VendorPoDetailScreen
import com.radityaf.depotify_wrz.ui.vendor.po.VendorPoListScreen
import com.radityaf.depotify_wrz.ui.vendor.product.VendorProductFormScreen
import com.radityaf.depotify_wrz.ui.vendor.product.VendorProductListScreen

private enum class VendorTab(val label: String) {
    PO("Purchase Order"), PRODUK("Produk Saya"), PROFIL("Profil")
}

private sealed interface VendorSubRoute {
    data class PoDetail(val id: Int) : VendorSubRoute
    data class ProductForm(val productId: Int? = null) : VendorSubRoute
}

@Composable
fun VendorMainScreen(
    user: User,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    var tab by remember { mutableStateOf(VendorTab.PO) }
    var subRoute by remember { mutableStateOf<VendorSubRoute?>(null) }

    BackHandler(enabled = subRoute != null) {
        subRoute = null
    }

    ClayBackground {
        val activeSub = subRoute
        if (activeSub != null) {
            when (activeSub) {
                is VendorSubRoute.PoDetail -> VendorPoDetailScreen(
                    poId = activeSub.id,
                    modifier = modifier,
                    onBack = { subRoute = null }
                )
                is VendorSubRoute.ProductForm -> VendorProductFormScreen(
                    user = user,
                    productId = activeSub.productId,
                    modifier = modifier,
                    onBack = { subRoute = null }
                )
            }
        } else {
            Scaffold(
                modifier = modifier,
                containerColor = Color.Transparent,
                topBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 4.dp)
                            .clayCard(cornerRadius = 24.dp, color = DepotifyColors.Surface)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clayButton(cornerRadius = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("V", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Depotify Vendor", color = DepotifyColors.Navy, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                                    Text(user.name, color = DepotifyColors.TextSecondary, fontSize = 11.sp)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clayInset(cornerRadius = 12.dp, backgroundColor = DepotifyColors.SurfaceInset)
                                    .clickable { onLogout() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Keluar", color = DepotifyColors.GreenDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                },
                bottomBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 12.dp)
                            .clayCard(cornerRadius = 28.dp, color = DepotifyColors.Surface)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VendorTab.values().forEach { t ->
                                val selected = tab == t
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .then(
                                            if (selected) Modifier.clayButton(cornerRadius = 20.dp)
                                            else Modifier.clickable { tab = t }
                                        )
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            when (t) {
                                                VendorTab.PO -> Icons.Default.Edit
                                                VendorTab.PRODUK -> Icons.Default.ShoppingCart
                                                VendorTab.PROFIL -> Icons.Default.Person
                                            },
                                            contentDescription = t.label,
                                            tint = if (selected) Color.White else DepotifyColors.TextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            t.label,
                                            fontSize = 10.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selected) Color.White else DepotifyColors.TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            ) { padding ->
                when (tab) {
                    VendorTab.PO -> VendorPoListScreen(
                        modifier = Modifier.padding(padding),
                        onPoClick = { poId -> subRoute = VendorSubRoute.PoDetail(poId) }
                    )
                    VendorTab.PRODUK -> VendorProductListScreen(
                        user = user,
                        modifier = Modifier.padding(padding),
                        onAddClick = { subRoute = VendorSubRoute.ProductForm(null) },
                        onProductEditClick = { id -> subRoute = VendorSubRoute.ProductForm(id) }
                    )
                    VendorTab.PROFIL -> ProfileScreen(user = user, modifier = Modifier.padding(padding), onLogout = onLogout)
                }
            }
        }
    }
}
