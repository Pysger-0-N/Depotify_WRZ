package com.radityaf.depotify_wrz.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Palet warna Claymorphism Depotify (selaras dengan tampilan web). */
object DepotifyColors {
    val Navy = Color(0xFF0F172A)          // Judul utama & teks navy gelap/slate
    val Green = Color(0xFF00A884)         // Mint teal aksen / tombol utama
    val GreenDark = Color(0xFF059669)     // Teks aksen hijau / label kecil
    val GreenSoft = Color(0xFFE6F7F0)     // Inset badge / icon container hijau muda
    val Background = Color(0xFFF0F4F8)    // Latar halaman Claymorphism (ice-blue soft)
    val Surface = Color(0xFFFFFFFF)       // Permukaan kartu clay putih bersih
    val SurfaceInset = Color(0xFFF4F7FB)  // Permukaan terbenam / sunken input & dropdown terang
    val Border = Color(0xFFE2E8F0)        // Border halus
    val TextMuted = Color(0xFF94A3B8)
    val TextSecondary = Color(0xFF64748B)
    val Warning = Color(0xFFD97706)
    val WarningSoft = Color(0xFFFEF3E2)
    val Danger = Color(0xFFDC2626)

    // Clay Double Shadow Colors
    val ShadowDark = Color(0x180F172A)
    val ShadowLight = Color(0xFFFFFFFF)

    // Clay Button Mint Gradient
    val GreenGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF00B894),
            Color(0xFF008080)
        )
    )
}

/**
 * MaterialTheme ColorScheme
 */
val DepotifyLightColorScheme = lightColorScheme(
    primary = DepotifyColors.Green,
    onPrimary = Color.White,
    secondary = DepotifyColors.Navy,
    onSecondary = Color.White,
    background = DepotifyColors.Background,
    onBackground = DepotifyColors.Navy,
    surface = DepotifyColors.Surface,
    onSurface = DepotifyColors.Navy,
    surfaceVariant = DepotifyColors.SurfaceInset,
    onSurfaceVariant = DepotifyColors.TextSecondary,
    outline = DepotifyColors.Border,
    error = DepotifyColors.Danger
)
