package com.aodstudio.app.config

/**
 * Centralized application configuration.
 * App-wide constants, feature flags, and environment settings.
 */
object AppConfig {

    // ─── App Info ──────────────────────────────────────────────────
    const val APP_NAME = "AOD Studio"
    const val APP_PACKAGE = "com.aodstudio.app"
    const val SCHEMA_VERSION = 1

    // ─── Target Device ─────────────────────────────────────────────
    object Device {
        const val TARGET_DEVICE = "Vivo T4 Pro"
        const val TARGET_OS = "OriginOS 6"
        const val TARGET_ANDROID = "Android 16 (API 36)"
        const val SCREEN_WIDTH_PX = 1080
        const val SCREEN_HEIGHT_PX = 2400
        const val SCREEN_DENSITY = 440  // DPI
    }

    // ─── Feature Flags ─────────────────────────────────────────────
    object Features {
        const val AOD_OVERLAY_ENABLED = true
        const val NOTIFICATION_LISTENER_ENABLED = true
        const val MEDIA_SESSION_ENABLED = true
        const val BURN_IN_PROTECTION_ENABLED = true
        const val ANIMATION_ENABLED = true
        const val VIVO_COMPATIBILITY_ENABLED = true
    }

    // ─── Storage ───────────────────────────────────────────────────
    object Storage {
        const val THEMES_DIR = "themes"
        const val BUILTIN_THEMES_ASSET_DIR = "themes"
        const val THEME_FILE_EXTENSION = ".aod.json"
        const val DATABASE_NAME = "aod_studio_db"
        const val PREFERENCES_NAME = "aod_studio_prefs"
    }

    // ─── AOD Service ───────────────────────────────────────────────
    object Service {
        const val NOTIFICATION_CHANNEL_ID = "aod_service_channel"
        const val NOTIFICATION_CHANNEL_NAME = "AOD Service"
        const val NOTIFICATION_ID = 1001
        const val SERVICE_NOTIFICATION_TITLE = "AOD Studio is active"
        const val SERVICE_NOTIFICATION_TEXT = "Your custom AOD is running"
    }

    // ─── Limits ────────────────────────────────────────────────────
    object Limits {
        const val MAX_THEME_NAME_LENGTH = 50
        const val MAX_ELEMENTS_PER_THEME = 50
        const val MAX_NOTIFICATION_ICONS = 10
        const val MAX_CUSTOM_THEMES = 100
    }
}
