package com.aodstudio.app.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of NotificationRepository.
 * Handles blacklisting, maximum notification limits, and grouping by package.
 */
@Singleton
class NotificationRepositoryImpl @Inject constructor() : NotificationRepository {

    private val _notificationsMap = mutableMapOf<String, NotificationItem>()
    private val _activeNotifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    override val activeNotifications: StateFlow<List<NotificationItem>> = _activeNotifications.asStateFlow()

    private var blacklistPackages = setOf<String>()
    private var maxCount = 5

    override fun onNotificationPosted(item: NotificationItem) {
        if (blacklistPackages.contains(item.packageName)) return

        _notificationsMap[item.key] = item
        updateFlow()
    }

    override fun onNotificationRemoved(key: String) {
        _notificationsMap.remove(key)
        updateFlow()
    }

    override fun clearAll() {
        _notificationsMap.clear()
        updateFlow()
    }

    override fun setBlacklist(packages: Set<String>) {
        this.blacklistPackages = packages
        _notificationsMap.entries.removeIf { blacklistPackages.contains(it.value.packageName) }
        updateFlow()
    }

    override fun setMaxNotificationCount(count: Int) {
        this.maxCount = count.coerceAtLeast(1)
        updateFlow()
    }

    private fun updateFlow() {
        val sortedList = _notificationsMap.values
            .sortedByDescending { it.timestamp }
            .take(maxCount)
        _activeNotifications.value = sortedList
    }
}
