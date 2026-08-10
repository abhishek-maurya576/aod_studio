package com.aodstudio.app.domain.model

import com.aodstudio.app.config.AppConfig
import com.aodstudio.app.config.ThemeConfig
import kotlinx.serialization.Serializable

/**
 * Canvas configuration for an AOD theme.
 * Defines dimensions and background color.
 */
@Serializable
data class AODCanvas(
    val width: Int = AppConfig.Device.SCREEN_WIDTH_PX,
    val height: Int = AppConfig.Device.SCREEN_HEIGHT_PX,
    val background: String = "#000000" // Pure black for AMOLED
)
