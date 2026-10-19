package com.radityaf.depotify_wrz.ui.owner.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.ui.components.EmptyState
import com.radityaf.depotify_wrz.ui.components.SectionCard
import com.radityaf.depotify_wrz.ui.components.SectionLabel
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoryViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("Kategori", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBack != null) {
                        TextButton(onClick = onBack) {
                            Text("Kembali", color = DepotifyColors.Green)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DepotifyColors.Navy) // (1) top bar navy
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    SectionLabel("Master Data")
                    Text(
                        "Kategori",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DepotifyColors.Navy
                    )
                    Text(
                        "Kelompokkan produk depot Anda.",
                        fontSize = 12.sp,
                        color = DepotifyColors.TextSecondary
                    )
                }
            }

            // form tambah
            item {
                SectionCard(label = "Form", title = "Tambah Kategori") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("Nama kategori") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DepotifyColors.Green,
                                unfocusedBorderColor = DepotifyColors.Border
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))

                        // tombol tambah
                        Button(
                            onClick = { viewModel.add(name); name = "" },
                            enabled = name.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DepotifyColors.Green,
                                contentColor = DepotifyColors.Navy
                            ),
                            modifier = Modifier.height(56.dp)
                        ) { Text("Tambah", fontWeight = FontWeight.Bold) }
                    }
                }
            }

            // daftar kategori
            item {
                SectionCard(label = "Data", title = "Daftar Kategori (${state.items.size})") {
                    if (state.items.isEmpty()) {
                        EmptyState("Belum ada kategori.")
                    } else {
                        state.items.forEachIndexed { index, category ->
                            if (index > 0) HorizontalDivider(color = DepotifyColors.Border)
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    category.name,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DepotifyColors.Navy
                                )
                                TextButton(onClick = { viewModel.delete(category.id) }) {
                                    Text("Hapus", color = DepotifyColors.Danger, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}