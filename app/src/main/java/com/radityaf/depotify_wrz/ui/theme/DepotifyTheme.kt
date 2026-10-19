package com.radityaf.depotify_wrz.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Warna yang diambil dari tampilan web Depotify. */
object DepotifyColors {
    val Navy = Color(0xFF1B2A4A)          // sidebar web, judul
    val Green = Color(0xFF34D399)         // aksen / menu aktif
    val GreenDark = Color(0xFF059669)     // label kecil, teks aksen
    val GreenSoft = Color(0xFFE6F7F0)     // latar badge ikon
    val Background = Color(0xFFEDECE6)    // latar halaman (krem)
    val Surface = Color(0xFFFFFFFF)       // kartu
    val Border = Color(0xFFE5E5DF)
    val TextMuted = Color(0xFF94A3B8)
    val TextSecondary = Color(0xFF64748B)
    val Warning = Color(0xFFD97706)
    val WarningSoft = Color(0xFFFEF3E2)
    val Danger = Color(0xFFDC2626)
}

/**
 * Pakai di Theme.kt bawaan project:
 *
 * MaterialTheme(
 *     colorScheme = DepotifyLightColorScheme,
 *     typography = Typography,
 *     content = content
 * )
 *
 * (matikan dynamic color supaya warna tidak berubah mengikuti wallpaper)
 */
val DepotifyLightColorScheme = lightColorScheme(
    primary = DepotifyColors.GreenDark,
    onPrimary = Color.White,
    secondary = DepotifyColors.Navy,
    onSecondary = Color.White,
    background = DepotifyColors.Background,
    onBackground = DepotifyColors.Navy,
    surface = DepotifyColors.Surface,
    onSurface = DepotifyColors.Navy,
    surfaceVariant = DepotifyColors.Surface,
    onSurfaceVariant = DepotifyColors.TextSecondary,
    outline = DepotifyColors.Border,
    error = DepotifyColors.Danger
)