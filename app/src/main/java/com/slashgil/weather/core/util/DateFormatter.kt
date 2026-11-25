package com.slashgil.weather.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val inputDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val outputDateFormatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    private val timestampFormatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())

    fun formatDateString(dateStr: String): String {
        return try {
            val date = inputDateFormatter.parse(dateStr)
            if (date != null) {
                outputDateFormatter.format(date)
            } else {
                dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatLastUpdatedTimestamp(timestampMillis: Long): String {
        if (timestampMillis <= 0) return "Never updated"
        val date = Date(timestampMillis)
        return "Last updated: ${timestampFormatter.format(date)}"
    }
}
