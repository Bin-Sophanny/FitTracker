package com.example.fittrack.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    /**
     * Get current date in yyyy-MM-dd format using GMT+7 timezone
     */
    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Bangkok")
        return dateFormat.format(Date())
    }

    /**
     * Get current date with start-of-day time in GMT+7 timezone for MongoDB storage
     */
    fun getCurrentDateWithTimezone(): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Bangkok"))
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        isoFormat.timeZone = TimeZone.getTimeZone("Asia/Bangkok")
        return isoFormat.format(calendar.time)
    }

    /**
     * Parse ISO date string from MongoDB and extract date in GMT+7
     */
    fun parseIsoDateToGmt7(isoDateString: String): String {
        return try {
            when {
                !isoDateString.contains("T") -> isoDateString
                else -> {
                    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
                    val date = isoFormat.parse(isoDateString)

                    if (date != null) {
                        val gmt7Format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        gmt7Format.timeZone = TimeZone.getTimeZone("Asia/Bangkok")
                        gmt7Format.format(date)
                    } else {
                        isoDateString.substringBefore("T")
                    }
                }
            }
        } catch (e: Exception) {
            if (isoDateString.contains("T")) {
                isoDateString.substringBefore("T")
            } else {
                isoDateString
            }
        }
    }

    /**
     * Extract date part from ISO format string
     */
    fun extractDateFromIso(dateString: String): String {
        return if (dateString.contains("T")) {
            dateString.substringBefore("T")
        } else {
            dateString
        }
    }

    /**
     * Format a date for display
     */
    fun formatDisplayDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("Asia/Bangkok")

        val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getTimeZone("Asia/Bangkok")

        return try {
            val date = inputFormat.parse(dateString)
            if (date != null) {
                outputFormat.format(date)
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Format a timestamp for logging
     */
    fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Bangkok")
        return dateFormat.format(Date(timestamp))
    }

    /**
     * Format time for logging
     */
    fun formatTime(timestamp: Long): String {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        timeFormat.timeZone = TimeZone.getTimeZone("Asia/Bangkok")
        return timeFormat.format(Date(timestamp))
    }

    /**
     * Check if a date string is today
     * Handles both ISO format (2025-12-11T00:00:00.000+07:00) and simple format (2025-12-11)
     */
    fun isToday(dateString: String): Boolean {
        val dateOnly = extractDateFromIso(dateString)  // Extract "2025-12-11" from ISO string
        val today = getCurrentDate()                    // Get today's date "2025-12-11"
        return dateOnly == today
    }

    /**
     * Convert GMT+7 date string to UTC for MongoDB storage
     */
    fun convertGmt7ToUtc(gmt7DateString: String): String {
        return try {
            val gmt7Format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            gmt7Format.timeZone = TimeZone.getTimeZone("Asia/Bangkok")
            val gmt7Date = gmt7Format.parse(gmt7DateString)

            if (gmt7Date != null) {
                val utcFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                utcFormat.timeZone = TimeZone.getTimeZone("UTC")
                utcFormat.format(gmt7Date)
            } else {
                gmt7DateString
            }
        } catch (e: Exception) {
            gmt7DateString
        }
    }

    /**
     * Convert UTC date string from MongoDB to GMT+7
     */
    fun convertUtcToGmt7(utcDateString: String): String {
        return try {
            val utcDate = when {
                utcDateString.contains("T") -> {
                    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    isoFormat.timeZone = TimeZone.getTimeZone("UTC")
                    isoFormat.parse(utcDateString.split(".")[0])
                }
                else -> {
                    val simpleFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    simpleFormat.timeZone = TimeZone.getTimeZone("UTC")
                    simpleFormat.parse(utcDateString)
                }
            }

            if (utcDate != null) {
                val gmt7Format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                gmt7Format.timeZone = TimeZone.getTimeZone("Asia/Bangkok")
                gmt7Format.format(utcDate)
            } else {
                utcDateString
            }
        } catch (e: Exception) {
            utcDateString
        }
    }

    /**
     * Format a date string for display
     */
    fun formatUtcDateForDisplay(utcDateString: String): String {
        val gmt7Date = convertUtcToGmt7(utcDateString)
        return formatDisplayDate(gmt7Date)
    }
}

