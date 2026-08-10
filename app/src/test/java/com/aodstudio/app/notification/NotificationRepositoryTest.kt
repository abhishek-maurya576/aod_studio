package com.aodstudio.app.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for NotificationRepositoryImpl blacklisting, limits, and grouping logic.
 */
class NotificationRepositoryTest {

    private lateinit var repository: NotificationRepositoryImpl

    @Before
    fun setup() {
        repository = NotificationRepositoryImpl()
    }

    @Test
    fun `posted notification is added to activeNotifications`() {
        val item = NotificationItem(key = "key1", packageName = "com.whatsapp")
        repository.onNotificationPosted(item)

        val active = repository.activeNotifications.value
        assertEquals(1, active.size)
        assertEquals("key1", active.first().key)
    }

    @Test
    fun `blacklisted app notifications are ignored`() {
        repository.setBlacklist(setOf("com.facebook.katana"))

        val allowedItem = NotificationItem(key = "key1", packageName = "com.whatsapp")
        val blockedItem = NotificationItem(key = "key2", packageName = "com.facebook.katana")

        repository.onNotificationPosted(allowedItem)
        repository.onNotificationPosted(blockedItem)

        val active = repository.activeNotifications.value
        assertEquals(1, active.size)
        assertEquals("com.whatsapp", active.first().packageName)
    }

    @Test
    fun `maxNotificationCount truncates active list`() {
        repository.setMaxNotificationCount(2)

        for (i in 1..5) {
            repository.onNotificationPosted(NotificationItem(key = "key_$i", packageName = "app_$i", timestamp = i * 1000L))
        }

        val active = repository.activeNotifications.value
        assertEquals(2, active.size)
        assertEquals("key_5", active[0].key) // Most recent first
    }

    @Test
    fun `removed notification is evicted from active list`() {
        val item = NotificationItem(key = "key1", packageName = "com.whatsapp")
        repository.onNotificationPosted(item)
        assertEquals(1, repository.activeNotifications.value.size)

        repository.onNotificationRemoved("key1")
        assertTrue(repository.activeNotifications.value.isEmpty())
    }
}
