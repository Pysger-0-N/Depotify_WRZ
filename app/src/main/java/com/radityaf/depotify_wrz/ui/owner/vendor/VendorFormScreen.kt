package com.radityaf.depotify_wrz.ui.owner.vendor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.radityaf.depotify_wrz.ui.components.*
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

@Composable
fun VendorFormScreen(
    modifier: Modifier = Modifier,
    vendorId: Int? = null,
    onBack: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        containerColor = DepotifyColors.Background,
        topBar = { DepotifyTopBar(if (vendorId == null) "Tambah Vendor" else "Ubah Vendor", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader("Master Data", if (vendorId == null) "Form Vendor Baru" else "Edit Vendor", "Kelola data rekanan pemasok barang.")

            SectionCard(label = "Form", title = "Data Vendor") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DepotifyTextField("Nama Vendor", name, { name = it }, placeholder = "PT Nama Vendor")
                    DepotifyTextField("Contact Person", contactPerson, { contactPerson = it }, placeholder = "Nama narasumber")
                    DepotifyTextField("No. Telepon / HP", phone, { phone = it }, placeholder = "08xxxxxxxxxx", keyboardType = KeyboardType.Phone)
                    DepotifyTextField("Email", email, { email = it }, placeholder = "email@vendor.com", keyboardType = KeyboardType.Email)
                    DepotifyTextField("Alamat", address, { address = it }, placeholder = "Alamat lengkap vendor")
                }
            }

            DepotifyButton(
                text = "Simpan Vendor",
                onClick = { onBack() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}