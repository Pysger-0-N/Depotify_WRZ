package com.radityaf.depotify_wrz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors
import java.util.Locale

/** Label kecil hijau terbenam (inset pill badge) seperti "OVERVIEW", "PERFORMA" di web. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clayInset(cornerRadius = 10.dp)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = DepotifyColors.GreenDark,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp
        )
    }
}

/** Kartu Inflated Claymorphism dengan double shadow lembut. */
@Composable
fun DepotifyCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier.clayCard(cornerRadius = cornerRadius)
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

/** Kartu berisi label hijau terbenam + judul + garis pemisah + isi. */
@Composable
fun SectionCard(
    label: String,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    DepotifyCard(modifier) {
        SectionLabel(label)
        Spacer(Modifier.height(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DepotifyColors.Navy)
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = DepotifyColors.Border)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

/** Kartu angka ringkasan Claymorphism (Transaksi hari ini, Pendapatan, dst). */
@Composable
fun StatCard(
    label: String,
    value: String,
    caption: String,
    badge: String,
    modifier: Modifier = Modifier,
    valueColor: Color = DepotifyColors.Navy,
    badgeBackground: Color = DepotifyColors.GreenSoft,
    badgeColor: Color = DepotifyColors.GreenDark
) {
    DepotifyCard(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                label.uppercase(),
                modifier = Modifier.weight(1f),
                color = DepotifyColors.TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Box(
                Modifier
                    .size(28.dp)
                    .clayInset(cornerRadius = 10.dp, backgroundColor = badgeBackground)
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(badge, color = badgeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(2.dp))
        Text(caption, color = DepotifyColors.TextMuted, fontSize = 11.sp)
    }
}

/** Kotak garis putus-putus Claymorphism untuk kondisi data kosong. */
@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    val dashColor = DepotifyColors.Border
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clayInset(cornerRadius = 16.dp, backgroundColor = DepotifyColors.SurfaceInset)
            .drawBehind {
                drawRoundRect(
                    color = dashColor,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                    )
                )
            }
            .padding(vertical = 22.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = DepotifyColors.TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

// ---------- helper format angka ----------

/** 1500000 -> "Rp 1.500.000" */
fun formatRupiah(value: Long): String =
    "Rp " + String.format(Locale("id", "ID"), "%,d", value)

/** Versi singkat untuk label chart: 1500000 -> "1,5jt", 25000 -> "25rb" */
fun compactRupiah(value: Long): String = when {
    value >= 1_000_000 -> String.format(Locale("id", "ID"), "%.1fjt", value / 1_000_000.0)
    value >= 1_000 -> "${value / 1_000}rb"
    else -> value.toString()
}
