package com.aodstudio.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.aodstudio.app.config.ThemeConfig

/**
 * Color palette for AOD Studio.
 * All values sourced from ThemeConfig.Colors — never define colors inline.
 *
 * Design rules:
 * - No blue/indigo variants
 * - AMOLED-optimized: pure black backgrounds
 * - Warm/neutral palette: amber, rose, emerald accents
 */

// ─── Primary ───────────────────────────────────────────────────────
val Primary = Color(ThemeConfig.Colors.PRIMARY)
val PrimaryVariant = Color(ThemeConfig.Colors.PRIMARY_VARIANT)
val OnPrimary = Color(ThemeConfig.Colors.ON_PRIMARY)

// ─── Secondary ─────────────────────────────────────────────────────
val Secondary = Color(ThemeConfig.Colors.SECONDARY)
val SecondaryVariant = Color(ThemeConfig.Colors.SECONDARY_VARIANT)
val OnSecondary = Color(ThemeConfig.Colors.ON_SECONDARY)

// ─── Tertiary ──────────────────────────────────────────────────────
val Tertiary = Color(ThemeConfig.Colors.TERTIARY)
val TertiaryVariant = Color(ThemeConfig.Colors.TERTIARY_VARIANT)
val OnTertiary = Color(ThemeConfig.Colors.ON_TERTIARY)

// ─── Background & Surface (AMOLED) ────────────────────────────────
val Background = Color(ThemeConfig.Colors.BACKGROUND)
val Surface = Color(ThemeConfig.Colors.SURFACE)
val SurfaceVariant = Color(ThemeConfig.Colors.SURFACE_VARIANT)
val SurfaceContainer = Color(ThemeConfig.Colors.SURFACE_CONTAINER)
val OnBackground = Color(ThemeConfig.Colors.ON_BACKGROUND)
val OnSurface = Color(ThemeConfig.Colors.ON_SURFACE)
val OnSurfaceVariant = Color(ThemeConfig.Colors.ON_SURFACE_VARIANT)

// ─── Status ────────────────────────────────────────────────────────
val Error = Color(ThemeConfig.Colors.ERROR)
val OnError = Color(ThemeConfig.Colors.ON_ERROR)
val Success = Color(ThemeConfig.Colors.SUCCESS)
val Warning = Color(ThemeConfig.Colors.WARNING)

// ─── AOD-specific ──────────────────────────────────────────────────
val AodElementDefault = Color(ThemeConfig.Colors.AOD_ELEMENT_DEFAULT)
val AodElementDim = Color(ThemeConfig.Colors.AOD_ELEMENT_DIM)
val AodClockAccent = Color(ThemeConfig.Colors.AOD_CLOCK_ACCENT)
