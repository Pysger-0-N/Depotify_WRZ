package com.radityaf.depotify_wrz.utils

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object FormatUtils {

    private val rupiahFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    fun BigDecimal.toRupiah(): String {
        return try {
            rupiahFormat.format(this)
        } catch (e: Exception) {
            "Rp 0"
        }
    }

    fun Double?.toRupiah(): String {
        if (this == null) return "Rp 0"
        return try {
            val bd = BigDecimal(this)
            bd.toRupiah()
        } catch (e: Exception) {
            "Rp 0"
        }
    }

    fun String?.toRupiah(): String {
        if (this.isNullOrBlank()) return "Rp 0"
        return try {
            val bd = BigDecimal(this)
            bd.toRupiah()
        } catch (e: Exception) {
            "Rp 0"
        }
    }

    // Format ISO date (yyyy-MM-dd or full ISO) to "dd MMM yyyy"
    fun formatDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "-"
        return try {
            val dateStr = if (isoString.length >= 10) isoString.substring(0, 10) else isoString
            val date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
            date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id", "ID")))
        } catch (e: Exception) {
            isoString
        }
    }

    // Format for sending date (yyyy-MM-dd)
    fun toApiDateString(date: LocalDate): String {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    // Format for sending received_date to POST /goods-receipts (yyyy-MM-dd HH:mm:ss)
    fun toApiDateTimeString(dateTime: LocalDateTime): String {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }

    // Relative time formatting for created_at (< 24 hours -> "3 jam lalu", otherwise "dd MMM yyyy HH:mm")
    fun formatRelativeTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "-"
        return try {
            val instant = Instant.parse(isoString)
            val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            val now = LocalDateTime.now()

            val hoursBetween = ChronoUnit.HOURS.between(dateTime, now)
            val minutesBetween = ChronoUnit.MINUTES.between(dateTime, now)
            val daysBetween = ChronoUnit.DAYS.between(dateTime, now)

            when {
                minutesBetween < 1 -> "Baru saja"
                minutesBetween < 60 -> "$minutesBetween menit lalu"
                hoursBetween < 24 -> "$hoursBetween jam lalu"
                daysBetween < 7 -> "$daysBetween hari lalu"
                else -> dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale("id", "ID")))
            }
        } catch (e: Exception) {
            isoString
        }
    }
}
