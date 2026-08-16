package com.aodstudio.app.feature.clock.radial

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Immutable state representing the temporal values needed by the RadialOrbitClockElement.
 */
data class RadialOrbitTimeState(
    val hour: Int,
    val minute: Int,
    val second: Int,
    val millisecond: Int,
    val dateFormatted: String,
    val dayOfWeek: String,
    val is24Hour: Boolean = true
) {
    /**
     * Exact fractional second including milliseconds for continuous 60fps rotational interpolation.
     */
    val continuousSecond: Float
        get() = second + (millisecond / 1000f)

    /**
     * Exact fractional minute including continuous seconds and milliseconds for smooth minute dial rotation.
     */
    val continuousMinute: Float
        get() = minute + (continuousSecond / 60f)

    /**
     * Formatted 2-digit hour string.
     */
    val formattedHour: String
        get() = if (is24Hour) {
            String.format(Locale.getDefault(), "%02d", hour)
        } else {
            val h12 = if (hour % 12 == 0) 12 else hour % 12
            String.format(Locale.getDefault(), "%02d", h12)
        }

    /**
     * Formatted 2-digit minute string.
     */
    val formattedMinute: String
        get() = String.format(Locale.getDefault(), "%02d", minute)

    companion object {
        private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        private val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())

        fun fromCalendar(calendar: Calendar, is24Hour: Boolean = true): RadialOrbitTimeState {
            val date = calendar.time
            return RadialOrbitTimeState(
                hour = if (is24Hour) calendar.get(Calendar.HOUR_OF_DAY) else calendar.get(Calendar.HOUR),
                minute = calendar.get(Calendar.MINUTE),
                second = calendar.get(Calendar.SECOND),
                millisecond = calendar.get(Calendar.MILLISECOND),
                dateFormatted = dateFormat.format(date).uppercase(Locale.getDefault()),
                dayOfWeek = dayFormat.format(date).uppercase(Locale.getDefault()),
                is24Hour = is24Hour
            )
        }
    }
}

/**
 * Centralized design tokens for RadialOrbitClockElement layout, geometry, and styling.
 */
data class RadialOrbitTokens(
    // Typography sizes
    val hourFontSize: TextUnit = 88.sp,
    val minuteFontSize: TextUnit = 24.sp,
    val dialFontSize: TextUnit = 11.sp,
    val dateFontSize: TextUnit = 13.sp,
    val dayFontSize: TextUnit = 14.sp,
    val dateLetterSpacing: TextUnit = 2.5.sp,
    val dayLetterSpacing: TextUnit = 3.sp,

    // Highlight Capsule Geometry
    val capsuleWidth: Dp = 130.dp,
    val capsuleHeight: Dp = 46.dp,
    val capsuleStrokeWidth: Dp = 1.5.dp,
    val capsuleCornerRadius: Dp = 23.dp,

    // Rotating Dials Geometry (Concentric Inner Minutes & Outer Seconds)
    val outerDialRadius: Dp = 185.dp,
    val innerDialRadius: Dp = 115.dp,
    val majorTickLength: Dp = 10.dp,
    val minorTickLength: Dp = 5.dp,
    val majorTickStrokeWidth: Dp = 1.5.dp,
    val minorTickStrokeWidth: Dp = 1.dp,
    val dialTextRadiusOffset: Dp = 20.dp,

    // Layout spacing & offsets
    val hourToCapsuleGap: Dp = 16.dp,
    val capsuleToDateGap: Dp = 32.dp,

    // Color tokens (Strict AMOLED #000000 baseline, warm/neutral accents, no forbidden blues)
    val hourColor: Color = Color.White,
    val minuteColor: Color = Color.White,
    val capsuleStrokeColor: Color = Color(0xEEFFFFFF),
    val majorTickColor: Color = Color(0xDDFFFFFF),
    val minorTickColor: Color = Color(0x55FFFFFF),
    val dialTextColor: Color = Color(0xCCFFFFFF),
    val dateTextColor: Color = Color(0xDDFFFFFF),
    val dayTextColor: Color = Color.White,
    val backgroundColor: Color = Color(0xFF000000)
) {
    // Backwards compatibility property
    val dialRadius: Dp get() = outerDialRadius

    companion object {
        val Default = RadialOrbitTokens()
    }
}
