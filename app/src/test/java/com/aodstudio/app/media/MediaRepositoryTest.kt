package com.aodstudio.app.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for MediaState data model and MediaRepository behavior.
 */
class MediaRepositoryTest {

    @Test
    fun `MediaState default values have no active media`() {
        val state = MediaState()
        assertNull(state.title)
        assertNull(state.artist)
        assertFalse(state.isPlaying)
        assertFalse(state.hasActiveMedia)
    }

    @Test
    fun `MediaState with title sets hasActiveMedia to true`() {
        val state = MediaState(
            title = "Midnight City",
            artist = "M83",
            isPlaying = true,
            durationMs = 240000L,
            progressMs = 60000L
        )

        assertTrue(state.hasActiveMedia)
        assertEquals("Midnight City", state.title)
        assertEquals("M83", state.artist)
        assertTrue(state.isPlaying)
        assertEquals(240000L, state.durationMs)
        assertEquals(60000L, state.progressMs)
    }
}
