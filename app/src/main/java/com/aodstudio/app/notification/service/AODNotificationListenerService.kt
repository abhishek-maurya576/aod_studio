package com.aodstudio.app.notification.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.aodstudio.app.media.MediaRepository
import com.aodstudio.app.notification.NotificationItem
import com.aodstudio.app.notification.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Official Android NotificationListenerService implementation.
 * Receives system notification callbacks (onNotificationPosted / onNotificationRemoved)
 * and initializes live system media session tracking.
 */
@AndroidEntryPoint
class AODNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var mediaRepository: MediaRepository

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let { notification ->
            val item = NotificationItem(
                key = notification.key,
                packageName = notification.packageName,
                timestamp = notification.postTime,
                category = notification.notification.category ?: ""
            )
            notificationRepository.onNotificationPosted(item)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn?.let { notification ->
            notificationRepository.onNotificationRemoved(notification.key)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            mediaRepository.initSessionListener()
            val activeSbns = activeNotifications ?: return
            for (sbn in activeSbns) {
                onNotificationPosted(sbn)
            }
        } catch (e: Exception) {
            // Handle security exception if permission not granted
        }
    }
}

