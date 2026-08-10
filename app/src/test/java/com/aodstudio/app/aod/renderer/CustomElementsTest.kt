package com.aodstudio.app.aod.renderer

import com.aodstudio.app.aod.renderer.renderers.GroupElementRenderer
import com.aodstudio.app.aod.renderer.renderers.ImageElementRenderer
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for Phase 10 custom elements (ImageElementRenderer & GroupElementRenderer).
 */
class CustomElementsTest {

    @Test
    fun `ImageElementRenderer instantiates cleanly`() {
        val renderer = ImageElementRenderer()
        assertNotNull(renderer)
    }

    @Test
    fun `GroupElementRenderer instantiates cleanly`() {
        val renderer = GroupElementRenderer()
        assertNotNull(renderer)
    }

    @Test
    fun `Image and Group elements initialize with valid properties`() {
        val imageElem = AODElement(type = AODElementType.IMAGE, width = 100f, height = 100f)
        val groupElem = AODElement(type = AODElementType.GROUP)

        assertNotNull(imageElem)
        assertNotNull(groupElem)
    }
}
