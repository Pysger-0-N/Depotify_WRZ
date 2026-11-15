package com.radityaf.depotify_wrz.ui.owner.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.data.local.entity.CategoryEntity
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun CategoryScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoryViewModel = hiltViewModel(),
    onCategoryClick: (Int) -> Unit = {},
    onAddCategoryClick: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { DepotifyTopBar("Kategori", onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PageHeader("Master Data", "Kategori", "Kelompokkan jenis produk depot Anda.")
            }

            item {
                DepotifyButton(
                    text = "+ Tambah Kategori Baru",
                    onClick = onAddCategoryClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                SectionCard(label = "Data", title = "Daftar Kategori") {
                    if (state.items.isEmpty()) {
                        EmptyState("Belum ada kategori.")
                    } else {
                        state.items.forEachIndexed { index, category ->
                            if (index > 0) HorizontalDivider(color = DepotifyColors.Border, modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onCategoryClick(category.id) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            category.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = DepotifyColors.Navy
                                        )
                                        Box(
                                            Modifier
                                                .clayInset(cornerRadius = 8.dp, backgroundColor = DepotifyColors.SurfaceInset)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                category.code,
                                                fontSize = 10.sp,
                                                color = DepotifyColors.GreenDark,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                    if (!category.description.isNullOrBlank()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            category.description,
                                            fontSize = 12.sp,
                                            color = DepotifyColors.TextSecondary
                                        )
                                    }
                                }
                                TextButton(onClick = { categoryToDelete = category }) {
                                    Text("Hapus", color = DepotifyColors.Danger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    categoryToDelete?.let { category ->
        DepotifyConfirmDialog(
            title = "Hapus Kategori",
            message = "Hapus kategori '${category.name}'?",
            confirmText = "Hapus",
            onConfirm = {
                viewModel.delete(category.id)
                categoryToDelete = null
            },
            onDismiss = { categoryToDelete = null }
        )
    }
}

