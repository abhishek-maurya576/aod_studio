package com.aodstudio.app.config

/**
 * Centralized theme configuration for AOD Studio.
 * ALL visual tokens must flow from here — never hardcode colors,
 * spacing, or typography values directly into components.
 *
 * Design philosophy:
 * - AMOLED-first: pure black backgrounds for battery efficiency
 * - Warm/neutral palette: no blue/indigo (per project rules)
 * - High contrast: critical for AOD visibility
 */
object ThemeConfig {

    // ─── Color Palette ─────────────────────────────────────────────
    object Colors {
        // Primary — warm amber/gold accent
        const val PRIMARY = 0xFFE8A838
        const val PRIMARY_VARIANT = 0xFFD4912A
        const val ON_PRIMARY = 0xFF1A1200

        // Secondary — soft rose accent
        const val SECONDARY = 0xFFE87C7C
        const val SECONDARY_VARIANT = 0xFFD46B6B
        const val ON_SECONDARY = 0xFF1A0E0E

        // Tertiary — emerald accent
        const val TERTIARY = 0xFF5EC98A
        const val TERTIARY_VARIANT = 0xFF4AB876
        const val ON_TERTIARY = 0xFF0A1A10

        // Background & Surface (AMOLED optimized)
        const val BACKGROUND = 0xFF000000L          // Pure black — pixels OFF on AMOLED
        const val SURFACE = 0xFF0D0D0D              // Near-black for cards
        const val SURFACE_VARIANT = 0xFF1A1A1A      // Elevated surfaces
        const val SURFACE_CONTAINER = 0xFF141414     // Container backgrounds
        const val ON_BACKGROUND = 0xFFE8E8E8        // Light text on dark
        const val ON_SURFACE = 0xFFE0E0E0
        const val ON_SURFACE_VARIANT = 0xFF9E9E9E   // Muted text

        // Status
        const val ERROR = 0xFFCF6679
        const val ON_ERROR = 0xFF1A0E12
        const val SUCCESS = 0xFF5EC98A
        const val WARNING = 0xFFE8A838

        // AOD-specific
        const val AOD_ELEMENT_DEFAULT = 0xFFFFFFFF   // White elements on black
        const val AOD_ELEMENT_DIM = 0x99FFFFFF        // 60% white for secondary elements
        const val AOD_CLOCK_ACCENT = 0xFFE8A838       // Clock accent color
    }

    // ─── Typography ────────────────────────────────────────────────
    object Typography {
        const val FONT_FAMILY_PRIMARY = "Inter"       // Main UI font
        const val FONT_FAMILY_DISPLAY = "Outfit"      // Display/headlines
        const val FONT_FAMILY_MONO = "JetBrains Mono" // Clock digits

        // Font sizes (sp)
        const val DISPLAY_LARGE = 57
        const val DISPLAY_MEDIUM = 45
        const val DISPLAY_SMALL = 36
        const val HEADLINE_LARGE = 32
        const val HEADLINE_MEDIUM = 28
        const val HEADLINE_SMALL = 24
        const val TITLE_LARGE = 22
        const val TITLE_MEDIUM = 16
        const val TITLE_SMALL = 14
        const val BODY_LARGE = 16
        const val BODY_MEDIUM = 14
        const val BODY_SMALL = 12
        const val LABEL_LARGE = 14
        const val LABEL_MEDIUM = 12
        const val LABEL_SMALL = 11
    }

    // ─── Spacing ───────────────────────────────────────────────────
    object Spacing {
        const val XXXS = 2    // dp
        const val XXS = 4
        const val XS = 8
        const val SM = 12
        const val MD = 16
        const val LG = 24
        const val XL = 32
        const val XXL = 48
        const val XXXL = 64
    }

    // ─── Radius ────────────────────────────────────────────────────
    object Radius {
        const val NONE = 0     // dp
        const val XS = 4
        const val SM = 8
        const val MD = 12
        const val LG = 16
        const val XL = 24
        const val FULL = 999   // Fully rounded
    }

    // ─── Elevation ─────────────────────────────────────────────────
    object Elevation {
        const val NONE = 0     // dp
        const val XS = 1
        const val SM = 2
        const val MD = 4
        const val LG = 8
        const val XL = 12
    }

    // ─── Animation ─────────────────────────────────────────────────
    object Animation {
        const val DURATION_FAST = 150     // ms
        const val DURATION_NORMAL = 300
        const val DURATION_SLOW = 500
        const val DURATION_VERY_SLOW = 800
    }

    // ─── AOD Specific ──────────────────────────────────────────────
    object AOD {
        const val DEFAULT_CANVAS_WIDTH = 1080       // px (Vivo T4 Pro)
        const val DEFAULT_CANVAS_HEIGHT = 2400      // px
        const val BURN_IN_SHIFT_INTERVAL_MS = 300_000L  // 5 minutes
        const val BURN_IN_MAX_OFFSET_PX = 4
        const val IDLE_REDRAW_INTERVAL_MS = 60_000L     // 1 minute (no seconds)
        const val SECONDS_REDRAW_INTERVAL_MS = 1_000L   // 1 second
        const val ANIMATION_MAX_FPS = 15                 // Cap for animations
        const val MIN_BRIGHTNESS = 0.05f                 // Minimum overlay brightness
    }
}
