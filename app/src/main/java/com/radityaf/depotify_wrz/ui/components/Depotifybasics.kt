package com.radityaf.depotify_wrz.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

/** Top bar Floating Claymorphism (Soft rounded white floating header). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepotifyTopBar(title: String, onBack: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clayCard(cornerRadius = 24.dp, color = DepotifyColors.Surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    Box(
                        modifier = Modifier
                            .clayInset(cornerRadius = 12.dp, backgroundColor = DepotifyColors.SurfaceInset)
                            .clickable { onBack() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("← Kembali", color = DepotifyColors.GreenDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                }
                Text(title, color = DepotifyColors.Navy, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            }
        }
    }
}

/** Header halaman: label hijau terbenam, judul besar dark slate, subjudul. */
@Composable
fun PageHeader(label: String, title: String, subtitle: String? = null) {
    Column {
        SectionLabel(label)
        Spacer(Modifier.height(6.dp))
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = DepotifyColors.Navy)
        if (subtitle != null) {
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 12.sp, color = DepotifyColors.TextSecondary)
        }
    }
}

/** Tombol utama Claymorphism (Inflated pill button dengan gradient mint & soft shadow). */
@Composable
fun DepotifyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clayButton(cornerRadius = 26.dp, enabled = enabled)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp
        )
    }
}

/** Text field Claymorphism terbenam (Sunken / Inset Input Box). */
@Composable
fun DepotifyTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DepotifyColors.Navy)
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clayInset(cornerRadius = 16.dp, backgroundColor = DepotifyColors.SurfaceInset)
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = DepotifyColors.TextMuted, fontSize = 13.sp) },
                singleLine = true,
                visualTransformation = visualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = DepotifyColors.Navy,
                    unfocusedTextColor = DepotifyColors.Navy
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Lencana status kecil Claymorphism terbenam. positive = hijau, selain itu = oranye. */
@Composable
fun StatusBadge(text: String, positive: Boolean, modifier: Modifier = Modifier) {
    val bg = if (positive) DepotifyColors.GreenSoft else DepotifyColors.WarningSoft
    val fg = if (positive) DepotifyColors.GreenDark else DepotifyColors.Warning
    Box(
        modifier
            .clayInset(cornerRadius = 10.dp, backgroundColor = bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
            color = fg
        )
    }
}

/** Lencana status Purchase Order (PO) khusus dengan pemetaan label Bahasa Indonesia & warna yang sesuai. */
@Composable
fun PoStatusBadge(status: String, modifier: Modifier = Modifier) {
    val (label, bg, fg) = when (status.uppercase()) {
        "PENDING", "DRAFT", "ORDERED" -> Triple("MENUNGGU KONFIRMASI", DepotifyColors.WarningSoft, DepotifyColors.Warning)
        "DIKONFIRMASI", "CONFIRMED" -> Triple("DIKELOLA", Color(0xFFE3F2FD), Color(0xFF1565C0))
        "DIKIRIM", "SHIPPED" -> Triple("DIKIRIM", Color(0xFFE0F7FA), Color(0xFF00838F))
        "DITERIMA", "RECEIVED" -> Triple("DITERIMA", DepotifyColors.GreenSoft, DepotifyColors.GreenDark)
        "DITOLAK", "REJECTED", "CANCELLED" -> Triple("DITOLAK", DepotifyColors.WarningSoft, DepotifyColors.Danger)
        else -> Triple(status.uppercase(), DepotifyColors.SurfaceInset, DepotifyColors.TextSecondary)
    }

    Box(
        modifier
            .clayInset(cornerRadius = 10.dp, backgroundColor = bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
            color = fg
        )
    }
}

/** Komponen Swipe-to-Refresh universal (Pull to Refresh seperti YouTube) untuk semua screen. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepotifyRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = rememberPullToRefreshState()
    
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            state.startRefresh()
        } else {
            state.endRefresh()
        }
    }

    if (state.isRefreshing) {
        LaunchedEffect(true) {
            onRefresh()
        }
    }

    Box(
        modifier = modifier.nestedScroll(state.nestedScrollConnection)
    ) {
        content()
        PullToRefreshContainer(
            state = state,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

/** Dialog konfirmasi Claymorphism universal untuk tindakan penting seperti hapus data atau mengosongkan keranjang. */
@Composable
fun DepotifyConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Ya, Hapus",
    cancelText: String = "Batal",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = DepotifyColors.Navy
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 13.sp,
                color = DepotifyColors.TextSecondary
            )
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clayButton(cornerRadius = 14.dp)
                    .clickable { onConfirm() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = confirmText,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = cancelText,
                    color = DepotifyColors.Navy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    )
}



