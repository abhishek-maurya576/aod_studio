package com.aodstudio.app.notification

import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for managing and filtering active notifications for AOD.
 */
interface NotificationRepository {

    /**
     * Observes filtered active notifications list as a StateFlow.
     */
    val activeNotifications: StateFlow<List<NotificationItem>>

    /**
     * Updates active notifications when a notification is posted.
     */
    fun onNotificationPosted(item: NotificationItem)

    /**
     * Removes a notification when dismissed/cancelled.
     */
    fun onNotificationRemoved(key: String)

    /**
     * Clears all active notification state.
     */
    fun clearAll()

    /**
     * Configures blacklisted app packages (apps whose notifications won't appear on AOD).
     */
    fun setBlacklist(packages: Set<String>)

    /**
     * Configures maximum number of notification icons to show on AOD.
     */
    fun setMaxNotificationCount(count: Int)
}
