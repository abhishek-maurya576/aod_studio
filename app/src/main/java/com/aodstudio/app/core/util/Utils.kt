package com.aodstudio.app.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Utility functions used across the app.
 * Stateless, pure functions only — no side effects.
 */

/**
 * Generates a unique ID for themes and elements.
 * Uses UUID v4 — collision probability is negligible.
 */
fun generateId(): String = UUID.randomUUID().toString()

/**
 * Formats current time according to the specified pattern.
 * Thread-safe: creates a new formatter per call (SimpleDateFormat is not thread-safe).
 * Exception-safe: falls back to "HH:mm" if pattern contains invalid SimpleDateFormat characters.
 */
fun formatTime(pattern: String, date: Date = Date()): String {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    } catch (e: Exception) {
        try {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        } catch (e2: Exception) {
            "12:45"
        }
    }
}

/**
 * Formats the current date according to the specified pattern.
 * Exception-safe: falls back to "EEE • MMM dd" if pattern contains invalid SimpleDateFormat characters.
 */
fun formatDate(pattern: String, date: Date = Date()): String {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    } catch (e: Exception) {
        try {
            SimpleDateFormat("EEE • MMM dd", Locale.getDefault()).format(date)
        } catch (e2: Exception) {
            "MON • AUG 10"
        }
    }
}

/**
 * Returns the day of the week as a short string (e.g., "MON").
 */
fun getDayOfWeekShort(date: Date = Date()): String {
    return SimpleDateFormat("EEE", Locale.ENGLISH).format(date).uppercase()
}

/**
 * Returns the month as a short string (e.g., "AUG").
 */
fun getMonthShort(date: Date = Date()): String {
    return SimpleDateFormat("MMM", Locale.ENGLISH).format(date).uppercase()
}

/**
 * Returns the day of the month (e.g., 10).
 */
fun getDayOfMonth(date: Date = Date()): Int {
    val cal = Calendar.getInstance()
    cal.time = date
    return cal.get(Calendar.DAY_OF_MONTH)
}

/**
 * Clamps a value between min and max (inclusive).
 */
fun Float.clamp(min: Float, max: Float): Float = coerceIn(min, max)
fun Int.clamp(min: Int, max: Int): Int = coerceIn(min, max)
