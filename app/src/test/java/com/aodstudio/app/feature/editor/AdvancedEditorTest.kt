package com.aodstudio.app.feature.editor

import android.content.Context
import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.usecase.GetThemesUseCase
import com.aodstudio.app.domain.usecase.SaveThemeUseCase
import com.aodstudio.app.media.MediaRepository
import com.aodstudio.app.notification.NotificationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for Advanced Editor (Undo/Redo history stack, snap-to-grid, and layer zIndex reordering).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AdvancedEditorTest {

    private val testDispatcher = StandardTestDispatcher()

    private val context = mockk<Context>(relaxed = true)
    private val getThemesUseCase = mockk<GetThemesUseCase>()
    private val saveThemeUseCase = mockk<SaveThemeUseCase>()
    private val settingsRepository = mockk<com.aodstudio.app.domain.repository.SettingsRepository>(relaxed = true)
    private val mediaRepository = mockk<MediaRepository>(relaxed = true)
    private val notificationRepository = mockk<NotificationRepository>(relaxed = true)

    private val testTheme = AODTheme.createDefaultTheme("Advanced Test Theme")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { getThemesUseCase.getById("theme_adv") } returns Result.Success(testTheme)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `position update pushes state to undo stack`() = runTest {
        val viewModel = AODEditorViewModel(context, getThemesUseCase, saveThemeUseCase, settingsRepository, mediaRepository, notificationRepository)
        viewModel.loadTheme("theme_adv")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canUndo)

        val elemId = testTheme.elements.first().id
        viewModel.updateElementPosition(elemId, 200f, 400f)

        assertTrue(viewModel.uiState.value.canUndo)
    }

    @Test
    fun `undo reverts position change`() = runTest {
        val viewModel = AODEditorViewModel(context, getThemesUseCase, saveThemeUseCase, settingsRepository, mediaRepository, notificationRepository)
        viewModel.loadTheme("theme_adv")
        testDispatcher.scheduler.advanceUntilIdle()

        val elemId = testTheme.elements.first().id
        val originalX = testTheme.elements.first().x

        viewModel.updateElementPosition(elemId, 200f, 400f)
        assertEquals(200f, viewModel.uiState.value.selectedElement?.x)

        viewModel.undo()
        assertEquals(originalX, viewModel.uiState.value.selectedElement?.x)
        assertTrue(viewModel.uiState.value.canRedo)
    }

    @Test
    fun `snapToCenter snaps coordinates within 20px to 540f`() = runTest {
        val viewModel = AODEditorViewModel(context, getThemesUseCase, saveThemeUseCase, settingsRepository, mediaRepository, notificationRepository)
        viewModel.loadTheme("theme_adv")
        testDispatcher.scheduler.advanceUntilIdle()

        val elemId = testTheme.elements.first().id
        viewModel.updateElementPosition(elemId, 535f, 1195f, snap = true)

        assertEquals(540f, viewModel.uiState.value.selectedElement?.x)
        assertEquals(1200f, viewModel.uiState.value.selectedElement?.y)
    }

    @Test
    fun `moveLayerUp increases element zIndex`() = runTest {
        val viewModel = AODEditorViewModel(context, getThemesUseCase, saveThemeUseCase, settingsRepository, mediaRepository, notificationRepository)
        viewModel.loadTheme("theme_adv")
        testDispatcher.scheduler.advanceUntilIdle()

        val elemId = testTheme.elements.first().id
        val initialZIndex = testTheme.elements.first().zIndex

        viewModel.moveLayerUp(elemId)

        val updatedZIndex = viewModel.uiState.value.theme?.elements?.find { it.id == elemId }?.zIndex
        assertEquals(initialZIndex + 1, updatedZIndex)
    }
}
