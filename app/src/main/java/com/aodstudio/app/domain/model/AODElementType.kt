package com.aodstudio.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Supported types for visual AOD elements.
 */
@Serializable
enum class AODElementType {
    CLOCK,
    DATE,
    BATTERY,
    NOTIFICATION,
    MUSIC,
    TEXT,
    IMAGE,
    SHAPE,
    LINE,
    RING,
    PROGRESS,
    GROUP
}
