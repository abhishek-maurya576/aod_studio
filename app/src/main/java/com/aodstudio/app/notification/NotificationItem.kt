package com.aodstudio.app.notification

import android.graphics.Bitmap

/**
 * Data model for notification metadata displayed on the AOD screen.
 * Stores app package name, title, timestamp, and optional app icon bitmap.
 */
data class NotificationItem(
    val key: String,
    val packageName: String,
    val appName: String = "",
    val title: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "",
    val isGroupHeader: Boolean = false,
    val isOngoing: Boolean = false,
    val iconBitmap: Bitmap? = null
)
