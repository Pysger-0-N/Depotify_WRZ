package com.radityaf.depotify_wrz.ui.main

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.data.model.User
import com.radityaf.depotify_wrz.ui.components.ClayBackground
import com.radityaf.depotify_wrz.ui.components.clayButton
import com.radityaf.depotify_wrz.ui.components.clayCard
import com.radityaf.depotify_wrz.ui.components.clayInset
import com.radityaf.depotify_wrz.ui.owner.category.CategoryFormScreen
import com.radityaf.depotify_wrz.ui.owner.category.CategoryScreen
import com.radityaf.depotify_wrz.ui.owner.home.BerandaScreen
import com.radityaf.depotify_wrz.ui.owner.kasir.KasirScreen
import com.radityaf.depotify_wrz.ui.owner.more.MoreScreen
import com.radityaf.depotify_wrz.ui.owner.notification.NotificationScreen
import com.radityaf.depotify_wrz.ui.owner.po.GoodsReceiptFormScreen
import com.radityaf.depotify_wrz.ui.owner.po.PoDetailScreen
import com.radityaf.depotify_wrz.ui.owner.po.PoFormScreen
import com.radityaf.depotify_wrz.ui.owner.po.PoListScreen
import com.radityaf.depotify_wrz.ui.owner.product.ProductDetailScreen
import com.radityaf.depotify_wrz.ui.owner.product.ProductFormScreen
import com.radityaf.depotify_wrz.ui.owner.report.FinancialReportScreen
import com.radityaf.depotify_wrz.ui.owner.sales.SalesScreen
import com.radityaf.depotify_wrz.ui.owner.sync.SyncScreen
import com.radityaf.depotify_wrz.ui.owner.sync.SyncViewModel
import com.radityaf.depotify_wrz.ui.owner.vendor.VendorFormScreen
import com.radityaf.depotify_wrz.ui.owner.vendor.VendorScreen
import com.radityaf.depotify_wrz.ui.product.ProductListScreen
import com.radityaf.depotify_wrz.ui.profile.ProfileScreen
import com.radityaf.depotify_wrz.ui.stock.AddStockMovementScreen
import com.radityaf.depotify_wrz.ui.stock.StockHistoryScreen
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

private enum class Tab(val label: String) {
    BERANDA("Beranda"), PRODUK("Produk"), PO("Purchase Order"), LAINNYA("Lainnya")
}

private sealed interface OwnerSubRoute {
    object Category : OwnerSubRoute
    data class CategoryForm(val categoryId: Int? = null) : OwnerSubRoute
    object Vendor : OwnerSubRoute
    data class VendorForm(val vendorId: Int? = null) : OwnerSubRoute
    object ProductForm : OwnerSubRoute
    data class ProductDetail(val id: Int) : OwnerSubRoute
    data class ProductEdit(val id: Int) : OwnerSubRoute
    object PoForm : OwnerSubRoute
    data class PoDetail(val id: Int) : OwnerSubRoute
    data class GoodsReceiptForm(val poId: Int) : OwnerSubRoute
    object StockHistory : OwnerSubRoute
    data class AddStockMovement(val productId: Int? = null) : OwnerSubRoute
    object Notifications : OwnerSubRoute
    object Profile : OwnerSubRoute
    object Sales : OwnerSubRoute
    object Sync : OwnerSubRoute
    object Kasir : OwnerSubRoute
    object FinancialReport : OwnerSubRoute
}

@Composable
fun MainScreen(
    user: User,
    onLogout: () -> Unit,
    syncViewModel: SyncViewModel = hiltViewModel()
) {
    var tab by remember { mutableStateOf(Tab.BERANDA) }
    var subRoute by remember { mutableStateOf<OwnerSubRoute?>(null) }
    val isOnline by syncViewModel.isOnline.collectAsState()

    BackHandler(enabled = subRoute != null) {
        subRoute = null
    }

    ClayBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (subRoute == null) {
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
                                    Text("D", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Depotify", color = DepotifyColors.Navy, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                                    Text(user.name, color = DepotifyColors.TextSecondary, fontSize = 11.sp)
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Network Connectivity Badge
                                Box(
                                    modifier = Modifier
                                        .clayInset(
                                            cornerRadius = 12.dp,
                                            backgroundColor = if (isOnline) DepotifyColors.GreenSoft else DepotifyColors.WarningSoft
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isOnline) "● Online" else "○ Offline",
                                        color = if (isOnline) DepotifyColors.GreenDark else DepotifyColors.Warning,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    )
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
                    }
                }
            },
            bottomBar = {
                if (subRoute == null) {
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
                            Tab.values().forEach { t ->
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
                                                Tab.BERANDA -> Icons.Default.Home
                                                Tab.PRODUK -> Icons.Default.ShoppingCart
                                                Tab.PO -> Icons.Default.Edit
                                                Tab.LAINNYA -> Icons.Default.Menu
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
            }
        ) { padding ->
            val mod = Modifier.padding(padding)
            val activeSub = subRoute

            if (activeSub != null) {
                when (activeSub) {
                    OwnerSubRoute.Category -> CategoryScreen(
                        modifier = mod,
                        onCategoryClick = { id -> subRoute = OwnerSubRoute.CategoryForm(id) },
                        onAddCategoryClick = { subRoute = OwnerSubRoute.CategoryForm(null) },
                        onBack = { subRoute = null }
                    )
                    is OwnerSubRoute.CategoryForm -> CategoryFormScreen(
                        categoryId = activeSub.categoryId,
                        modifier = mod,
                        onBack = { subRoute = OwnerSubRoute.Category }
                    )
                    OwnerSubRoute.Vendor -> VendorScreen(
                        modifier = mod,
                        onVendorClick = { id -> subRoute = OwnerSubRoute.VendorForm(id) },
                        onAddVendorClick = { subRoute = OwnerSubRoute.VendorForm(null) },
                        onBack = { subRoute = null }
                    )
                    is OwnerSubRoute.VendorForm -> VendorFormScreen(
                        vendorId = activeSub.vendorId,
                        modifier = mod,
                        onBack = { subRoute = OwnerSubRoute.Vendor }
                    )
                    OwnerSubRoute.ProductForm -> ProductFormScreen(modifier = mod, onBack = { subRoute = null })
                    is OwnerSubRoute.ProductDetail -> ProductDetailScreen(
                        productId = activeSub.id, 
                        modifier = mod, 
                        onEditClick = { id -> subRoute = OwnerSubRoute.ProductEdit(id) },
                        onBack = { subRoute = null }
                    )
                    is OwnerSubRoute.ProductEdit -> ProductFormScreen(productId = activeSub.id, modifier = mod, onBack = { subRoute = null })
                    OwnerSubRoute.PoForm -> PoFormScreen(modifier = mod, onSaved = { subRoute = null }, onBack = { subRoute = null })
                    is OwnerSubRoute.PoDetail -> PoDetailScreen(
                        poId = activeSub.id,
                        modifier = mod,
                        onReceiveClick = { poId -> subRoute = OwnerSubRoute.GoodsReceiptForm(poId) },
                        onBack = { subRoute = null }
                    )
                    is OwnerSubRoute.GoodsReceiptForm -> GoodsReceiptFormScreen(
                        poId = activeSub.poId,
                        modifier = mod,
                        onDone = { subRoute = OwnerSubRoute.PoDetail(activeSub.poId) },
                        onBack = { subRoute = OwnerSubRoute.PoDetail(activeSub.poId) }
                    )
                    OwnerSubRoute.StockHistory -> StockHistoryScreen(modifier = mod, onBack = { subRoute = null })
                    is OwnerSubRoute.AddStockMovement -> AddStockMovementScreen(modifier = mod, initialProductId = activeSub.productId, onBack = { subRoute = null })
                    OwnerSubRoute.Notifications -> NotificationScreen(
                        modifier = mod,
                        onBack = { subRoute = null },
                        onProductClick = { id -> subRoute = OwnerSubRoute.ProductDetail(id) },
                        onAddStockClick = { prodId -> subRoute = OwnerSubRoute.AddStockMovement(prodId) },
                        onCreatePoClick = { subRoute = OwnerSubRoute.PoForm }
                    )
                    OwnerSubRoute.Profile -> ProfileScreen(user = user, modifier = mod, onLogout = onLogout, onBack = { subRoute = null })
                    OwnerSubRoute.Sales -> SalesScreen(modifier = mod, onBack = { subRoute = null })
                    OwnerSubRoute.Sync -> SyncScreen(modifier = mod, onBack = { subRoute = null })
                    OwnerSubRoute.Kasir -> KasirScreen(modifier = mod, onBack = { subRoute = null })
                    OwnerSubRoute.FinancialReport -> FinancialReportScreen(modifier = mod, onBack = { subRoute = null })
                }
            } else {
                when (tab) {
                    Tab.BERANDA -> BerandaScreen(
                        modifier = mod,
                        onCategoryClick = { subRoute = OwnerSubRoute.Category },
                        onVendorClick = { subRoute = OwnerSubRoute.Vendor },
                        onKasirClick = { subRoute = OwnerSubRoute.Kasir },
                        onSalesClick = { subRoute = OwnerSubRoute.Sales },
                        onProductClick = { tab = Tab.PRODUK },
                        onStockClick = { subRoute = OwnerSubRoute.Notifications }
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
                        onAddStockMovementClick = { subRoute = OwnerSubRoute.AddStockMovement() },
                        onNotificationClick = { subRoute = OwnerSubRoute.Notifications },
                        onSalesClick = { subRoute = OwnerSubRoute.Sales },
                        onKasirClick = { subRoute = OwnerSubRoute.Kasir },
                        onFinancialReportClick = { subRoute = OwnerSubRoute.FinancialReport },
                        onSyncClick = { subRoute = OwnerSubRoute.Sync },
                        onProfileClick = { subRoute = OwnerSubRoute.Profile },
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}
