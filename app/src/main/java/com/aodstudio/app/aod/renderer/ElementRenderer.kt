package com.aodstudio.app.aod.renderer

import android.graphics.Canvas
import com.aodstudio.app.domain.model.AODElement

/**
 * Base interface for individual AOD element renderers.
 * Implementations handle specific element types (CLOCK, DATE, BATTERY, etc.).
 */
interface ElementRenderer {

    /**
     * Renders the given AODElement onto the Android Canvas using the current RenderContext.
     */
    fun render(canvas: Canvas, element: AODElement, context: RenderContext)
}
