package com.radityaf.depotify_wrz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

/** Top bar navy (warna sidebar web) dengan tombol Kembali opsional. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepotifyTopBar(title: String, onBack: (() -> Unit)? = null) {
    TopAppBar(
        title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (onBack != null) {
                TextButton(onClick = onBack) { Text("Kembali", color = DepotifyColors.Green) }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DepotifyColors.Navy)
    )
}

/** Header halaman: label hijau kecil, judul besar, subjudul. */
@Composable
fun PageHeader(label: String, title: String, subtitle: String? = null) {
    Column {
        SectionLabel(label)
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = DepotifyColors.Navy)
        if (subtitle != null) {
            Text(subtitle, fontSize = 12.sp, color = DepotifyColors.TextSecondary)
        }
    }
}

/** Tombol utama hijau mint, sama seperti tombol "Masuk" di login. */
@Composable
fun DepotifyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val dark = Color(0xFF064E3B)
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DepotifyColors.Green,
            contentColor = dark,
            disabledContainerColor = DepotifyColors.Green.copy(alpha = 0.4f),
            disabledContentColor = dark.copy(alpha = 0.5f)
        )
    ) { Text(text, fontWeight = FontWeight.Bold) }
}

/** Text field dengan label di atas, border hijau saat fokus. */
@Composable
fun DepotifyTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = DepotifyColors.Navy)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DepotifyColors.Green,
                unfocusedBorderColor = DepotifyColors.Border,
                focusedContainerColor = DepotifyColors.Surface,
                unfocusedContainerColor = DepotifyColors.Surface
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Lencana status kecil. positive = hijau, selain itu = oranye. */
@Composable
fun StatusBadge(text: String, positive: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (positive) DepotifyColors.GreenSoft else DepotifyColors.WarningSoft)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = if (positive) DepotifyColors.GreenDark else DepotifyColors.Warning
        )
    }
}