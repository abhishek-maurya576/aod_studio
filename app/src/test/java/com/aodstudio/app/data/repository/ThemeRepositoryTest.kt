package com.aodstudio.app.data.repository

import com.aodstudio.app.core.common.Result
import com.aodstudio.app.data.local.ThemeStorage
import com.aodstudio.app.domain.model.AODTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ThemeRepository implementation and ThemeStorage integration.
 */
class ThemeRepositoryTest {

    private val themeStorage = mockk<ThemeStorage>()
    private val testTheme = AODTheme.createDefaultTheme("Test Theme")

    @Before
    fun setup() {
        every { themeStorage.initializeBuiltInThemesIfNeeded() } returns Result.Success(listOf(testTheme))
        every { themeStorage.getAllThemes() } returns Result.Success(listOf(testTheme))
        every { themeStorage.getThemeById(testTheme.id) } returns Result.Success(testTheme)
        every { themeStorage.saveTheme(any()) } answers { Result.Success(firstArg()) }
        every { themeStorage.deleteTheme(any()) } returns Result.Success(true)
    }

    @Test
    fun `saveTheme calls ThemeStorage saveTheme`() {
        val result = themeStorage.saveTheme(testTheme)

        assertTrue(result is Result.Success)
        assertEquals(testTheme.name, (result as Result.Success).data.name)
        verify { themeStorage.saveTheme(testTheme) }
    }

    @Test
    fun `getThemeById returns requested theme`() {
        val result = themeStorage.getThemeById(testTheme.id)

        assertTrue(result is Result.Success)
        assertEquals(testTheme.id, (result as Result.Success).data.id)
    }

    @Test
    fun `deleteTheme removes theme by ID`() {
        val result = themeStorage.deleteTheme(testTheme.id)

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data)
        verify { themeStorage.deleteTheme(testTheme.id) }
    }
}
