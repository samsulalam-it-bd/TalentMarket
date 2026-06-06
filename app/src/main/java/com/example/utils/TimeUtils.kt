package com.example.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeUtils {
    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            diff < 60000 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hours ago"
            days < 7 -> "$days days ago"
            else -> {
                val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0L) return "N/A"
        val sdf = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
