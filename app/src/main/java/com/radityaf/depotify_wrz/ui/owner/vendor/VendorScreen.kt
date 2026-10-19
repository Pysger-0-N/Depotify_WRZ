package com.radityaf.depotify_wrz.ui.owner.vendor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun VendorScreen(
    modifier: Modifier = Modifier,
    viewModel: VendorViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
        topBar = { DepotifyTopBar("Vendor", onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PageHeader("Master Data", "Vendor", "Pemasok gas LPG dan air mineral.")
            }

            // form tambah vendor
            item {
                SectionCard(label = "Form", title = "Tambah Vendor") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DepotifyTextField(
                            label = "Nama Vendor",
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "Nama vendor"
                        )
                        DepotifyTextField(
                            label = "No. Telepon / HP",
                            value = phone,
                            onValueChange = { phone = it },
                            placeholder = "08xxxxxxxxxx",
                            keyboardType = KeyboardType.Phone
                        )
                        DepotifyButton(
                            text = "Tambah Vendor",
                            onClick = {
                                viewModel.add(name, phone)
                                name = ""
                                phone = ""
                            },
                            enabled = name.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // daftar vendor
            item {
                SectionCard(label = "Data", title = "Daftar Vendor (${state.items.size})") {
                    if (state.items.isEmpty()) {
                        EmptyState("Belum ada vendor.")
                    } else {
                        state.items.forEachIndexed { index, vendor ->
                            if (index > 0) HorizontalDivider(color = DepotifyColors.Border)
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        vendor.name,
                                        fontWeight = FontWeight.Bold,
                                        color = DepotifyColors.Navy
                                    )
                                    Text(
                                        vendor.phone ?: "",
                                        fontSize = 12.sp,
                                        color = DepotifyColors.TextSecondary
                                    )
                                }
                                TextButton(onClick = { viewModel.delete(vendor.id) }) {
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
