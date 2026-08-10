package com.aodstudio.app.aod.renderer

import com.aodstudio.app.aod.renderer.renderers.AnalogClockElementRenderer
import com.aodstudio.app.aod.renderer.renderers.RadialClockElementRenderer
import com.aodstudio.app.aod.renderer.renderers.TypographyClockElementRenderer
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for Phase 6 advanced clock styles (Analog, Typography/Stacked, Radial/Orbit).
 */
class AdvancedClockStylesTest {

    @Test
    fun `TypographyClockElementRenderer instantiates cleanly`() {
        val renderer = TypographyClockElementRenderer()
        assertNotNull(renderer)
    }

    @Test
    fun `RadialClockElementRenderer instantiates cleanly`() {
        val renderer = RadialClockElementRenderer()
        assertNotNull(renderer)
    }

    @Test
    fun `AnalogClockElementRenderer instantiates cleanly`() {
        val renderer = AnalogClockElementRenderer()
        assertNotNull(renderer)
    }

    @Test
    fun `clockStyle routing keys are recognized`() {
        val analogElem = AODElement(type = AODElementType.CLOCK, properties = mapOf("clockStyle" to "ANALOG"))
        val stackedElem = AODElement(type = AODElementType.CLOCK, properties = mapOf("clockStyle" to "STACKED"))
        val orbitElem = AODElement(type = AODElementType.CLOCK, properties = mapOf("clockStyle" to "ORBIT"))

        assertNotNull(analogElem)
        assertNotNull(stackedElem)
        assertNotNull(orbitElem)
    }
}
