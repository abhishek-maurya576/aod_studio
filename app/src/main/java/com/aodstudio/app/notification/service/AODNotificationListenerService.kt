package com.aodstudio.app.notification.service

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.aodstudio.app.media.MediaRepository
import com.aodstudio.app.notification.NotificationItem
import com.aodstudio.app.notification.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Official Android NotificationListenerService implementation.
 * Receives system notification callbacks, filters out irrelevant system/ongoing notifications,
 * extracts app icon bitmaps, and updates active notification state in real-time.
 */
@AndroidEntryPoint
class AODNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var mediaRepository: MediaRepository

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val pkg = sbn.packageName

        // 1. Filter out system UI & android internal packages
        if (pkg == "android" || pkg == "com.android.systemui" || pkg == "com.android.providers.downloads") {
            return
        }

        // 2. Filter out ongoing system status notifications
        val isOngoing = (sbn.notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
        if (isOngoing) {
            return
        }

        val category = sbn.notification.category ?: ""
        if (category == Notification.CATEGORY_TRANSPORT || category == Notification.CATEGORY_SERVICE) {
            return
        }

        // 3. Extract notification metadata & app icon
        val title = sbn.notification.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val appName = try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
        } catch (e: Exception) {
            pkg
        }

        val iconBitmap = extractAppIconBitmap(pkg)

        val item = NotificationItem(
            key = sbn.key,
            packageName = pkg,
            appName = appName,
            title = title,
            timestamp = sbn.postTime,
            category = category,
            isOngoing = isOngoing,
            iconBitmap = iconBitmap
        )
        notificationRepository.onNotificationPosted(item)
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
            notificationRepository.clearAll()
            val activeSbns = activeNotifications ?: return
            for (sbn in activeSbns) {
                onNotificationPosted(sbn)
            }
        } catch (e: Exception) {
            // Handle security exception if permission not granted
        }
    }

    private fun extractAppIconBitmap(packageName: String): Bitmap? {
        return try {
            val drawable = packageManager.getApplicationIcon(packageName)
            drawableToBitmap(drawable)
        } catch (e: Exception) {
            null
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceAtMost(128) else 64
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceAtMost(128) else 64
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}

