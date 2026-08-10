package com.aodstudio.app.notification

/**
 * Data model for notification metadata displayed on the AOD screen.
 * Stores only minimal required information for privacy & security (no message body text stored).
 */
data class NotificationItem(
    val key: String,
    val packageName: String,
    val appName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "",
    val isGroupHeader: Boolean = false
)
