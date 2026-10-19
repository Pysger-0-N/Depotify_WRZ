package com.radityaf.depotify_wrz.ui.main

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
import com.radityaf.depotify_wrz.ui.owner.category.CategoryScreen
import com.radityaf.depotify_wrz.ui.owner.home.BerandaScreen
import com.radityaf.depotify_wrz.ui.owner.more.MoreScreen
import com.radityaf.depotify_wrz.ui.owner.notification.NotificationScreen
import com.radityaf.depotify_wrz.ui.owner.po.PoDetailScreen
import com.radityaf.depotify_wrz.ui.owner.po.PoFormScreen
import com.radityaf.depotify_wrz.ui.owner.po.PoListScreen
import com.radityaf.depotify_wrz.ui.owner.product.ProductDetailScreen
import com.radityaf.depotify_wrz.ui.owner.product.ProductFormScreen
import com.radityaf.depotify_wrz.ui.owner.sales.SalesScreen
import com.radityaf.depotify_wrz.ui.owner.sync.SyncScreen
import com.radityaf.depotify_wrz.ui.owner.vendor.VendorScreen
import com.radityaf.depotify_wrz.ui.product.ProductListScreen
import com.radityaf.depotify_wrz.ui.profile.ProfileScreen
import com.radityaf.depotify_wrz.ui.stock.AddStockMovementScreen
import com.radityaf.depotify_wrz.ui.stock.StockHistoryScreen
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors
import androidx.activity.compose.BackHandler

private enum class Tab(val label: String) {
    BERANDA("Beranda"), PRODUK("Produk"), PO("Purchase Order"), LAINNYA("Lainnya")
}

private sealed interface OwnerSubRoute {
    object Category : OwnerSubRoute
    object Vendor : OwnerSubRoute
    object ProductForm : OwnerSubRoute
    data class ProductDetail(val id: Int) : OwnerSubRoute
    data class ProductEdit(val id: Int) : OwnerSubRoute
    object PoForm : OwnerSubRoute
    data class PoDetail(val id: Int) : OwnerSubRoute
    object StockHistory : OwnerSubRoute
    object AddStockMovement : OwnerSubRoute
    object Notifications : OwnerSubRoute
    object Profile : OwnerSubRoute
    object Sales : OwnerSubRoute
    object Sync : OwnerSubRoute
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    user: User,
    onLogout: () -> Unit
) {
    var tab by remember { mutableStateOf(Tab.BERANDA) }
    var subRoute by remember { mutableStateOf<OwnerSubRoute?>(null) }

    BackHandler(enabled = subRoute != null) {
        subRoute = null
    }

    Scaffold(
        containerColor = DepotifyColors.Background,
        topBar = {
            if (subRoute == null) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(DepotifyColors.Green),
                                contentAlignment = Alignment.Center
                            ) { Text("D", fontWeight = FontWeight.Bold, color = DepotifyColors.Navy) }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Depotify", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
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
            }
        },
        bottomBar = {
            if (subRoute == null) {
                NavigationBar(containerColor = DepotifyColors.Surface) {
                    Tab.values().forEach { t ->
                        NavigationBarItem(
                            selected = tab == t,
                            onClick = { tab = t },
                            icon = {
                                Icon(
                                    when (t) {
                                        Tab.BERANDA -> Icons.Default.Home
                                        Tab.PRODUK -> Icons.Default.ShoppingCart
                                        Tab.PO -> Icons.Default.Edit
                                        Tab.LAINNYA -> Icons.Default.Menu
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
        }
    ) { padding ->
        val mod = Modifier.padding(padding)
        val activeSub = subRoute

        if (activeSub != null) {
            when (activeSub) {
                OwnerSubRoute.Category -> CategoryScreen(modifier = mod, onBack = { subRoute = null })
                OwnerSubRoute.Vendor -> VendorScreen(modifier = mod, onBack = { subRoute = null })
                OwnerSubRoute.ProductForm -> ProductFormScreen(modifier = mod, onBack = { subRoute = null })
                is OwnerSubRoute.ProductDetail -> ProductDetailScreen(
                    productId = activeSub.id, 
                    modifier = mod, 
                    onEditClick = { id -> subRoute = OwnerSubRoute.ProductEdit(id) },
                    onBack = { subRoute = null }
                )
                is OwnerSubRoute.ProductEdit -> ProductFormScreen(productId = activeSub.id, modifier = mod, onBack = { subRoute = null })
                OwnerSubRoute.PoForm -> PoFormScreen(modifier = mod, onBack = { subRoute = null })
                is OwnerSubRoute.PoDetail -> PoDetailScreen(poId = activeSub.id, modifier = mod, onBack = { subRoute = null })
                OwnerSubRoute.StockHistory -> StockHistoryScreen(modifier = mod, onBack = { subRoute = null })
                OwnerSubRoute.AddStockMovement -> AddStockMovementScreen(modifier = mod, onBack = { subRoute = null })
                OwnerSubRoute.Notifications -> NotificationScreen(modifier = mod, onBack = { subRoute = null })
                OwnerSubRoute.Profile -> ProfileScreen(user = user, modifier = mod, onLogout = onLogout, onBack = { subRoute = null })
                OwnerSubRoute.Sales -> SalesScreen(modifier = mod, onBack = { subRoute = null })
                OwnerSubRoute.Sync -> SyncScreen(modifier = mod, onBack = { subRoute = null })
            }
        } else {
            when (tab) {
                Tab.BERANDA -> BerandaScreen(
                    modifier = mod,
                    onCategoryClick = { subRoute = OwnerSubRoute.Category },
                    onVendorClick = { subRoute = OwnerSubRoute.Vendor }
                )
                Tab.PRODUK -> ProductListScreen(
                    modifier = mod,
                    onProductClick = { id -> subRoute = OwnerSubRoute.ProductDetail(id) },
                    onAddProductClick = { subRoute = OwnerSubRoute.ProductForm }
                )
                Tab.PO -> PoListScreen(
                    modifier = mod,
                    onPoClick = { id -> subRoute = OwnerSubRoute.PoDetail(id) },
                    onAddClick = { subRoute = OwnerSubRoute.PoForm }
                )
                Tab.LAINNYA -> MoreScreen(
                    modifier = mod,
                    onCategoryClick = { subRoute = OwnerSubRoute.Category },
                    onVendorClick = { subRoute = OwnerSubRoute.Vendor },
                    onStockClick = { subRoute = OwnerSubRoute.StockHistory },
                    onAddStockMovementClick = { subRoute = OwnerSubRoute.AddStockMovement },
                    onNotificationClick = { subRoute = OwnerSubRoute.Notifications },
                    onSalesClick = { subRoute = OwnerSubRoute.Sales },
                    onSyncClick = { subRoute = OwnerSubRoute.Sync },
                    onProfileClick = { subRoute = OwnerSubRoute.Profile },
                    onLogout = onLogout
                )
            }
        }
    }
}
