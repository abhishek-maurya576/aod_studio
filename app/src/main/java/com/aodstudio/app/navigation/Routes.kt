package com.aodstudio.app.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for AOD Studio.
 * Each route is a serializable object/data class for Compose Navigation.
 */

// ─── Top-level destinations ────────────────────────────────────────

@Serializable
object HomeRoute

@Serializable
object ThemeLibraryRoute

@Serializable
object SettingsRoute

// ─── Feature destinations (will be added in later phases) ──────────

@Serializable
data class EditorRoute(val themeId: String? = null)

@Serializable
data class PreviewRoute(val themeId: String)
