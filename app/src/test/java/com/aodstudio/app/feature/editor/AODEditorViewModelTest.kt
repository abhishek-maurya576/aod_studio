package com.aodstudio.app.feature.editor

import android.content.Context
import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.usecase.GetThemesUseCase
import com.aodstudio.app.domain.usecase.SaveThemeUseCase
import com.aodstudio.app.media.MediaRepository
import com.aodstudio.app.notification.NotificationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AODEditorViewModel state transitions and editing operations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AODEditorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val context = mockk<Context>(relaxed = true)
    private val getThemesUseCase = mockk<GetThemesUseCase>()
    private val saveThemeUseCase = mockk<SaveThemeUseCase>()
    private val settingsRepository = mockk<com.aodstudio.app.domain.repository.SettingsRepository>(relaxed = true)
    private val mediaRepository = mockk<MediaRepository>(relaxed = true)
    private val notificationRepository = mockk<NotificationRepository>(relaxed = true)

    private val testTheme = AODTheme.createDefaultTheme("Editor Test Theme")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { getThemesUseCase.getById("theme123") } returns Result.Success(testTheme)
        coEvery { saveThemeUseCase.execute(any()) } returns Result.Success(testTheme)
        coEvery { saveThemeUseCase.setActive(any()) } returns Result.Success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadTheme with null ID creates default theme state`() = runTest {
        val viewModel = AODEditorViewModel(context, getThemesUseCase, saveThemeUseCase, settingsRepository, mediaRepository, notificationRepository)
        viewModel.loadTheme(null)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertNotNull(state.theme)
        assertTrue(state.isDirty)
    }

    @Test
    fun `loadTheme with valid ID loads existing theme`() = runTest {
        val viewModel = AODEditorViewModel(context, getThemesUseCase, saveThemeUseCase, settingsRepository, mediaRepository, notificationRepository)
        viewModel.loadTheme("theme123")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("Editor Test Theme", state.theme?.name)
        assertEquals(false, state.isDirty)
    }

    @Test
    fun `addElement appends new element and marks dirty`() = runTest {
        val viewModel = AODEditorViewModel(context, getThemesUseCase, saveThemeUseCase, settingsRepository, mediaRepository, notificationRepository)
        viewModel.loadTheme("theme123")
        testDispatcher.scheduler.advanceUntilIdle()

        val initialCount = viewModel.uiState.value.theme?.elements?.size ?: 0
        viewModel.addElement(AODElementType.BATTERY)

        val updatedCount = viewModel.uiState.value.theme?.elements?.size ?: 0
        assertEquals(initialCount + 1, updatedCount)
        assertTrue(viewModel.uiState.value.isDirty)
    }

    @Test
    fun `saveTheme calls saveThemeUseCase execute`() = runTest {
        val viewModel = AODEditorViewModel(context, getThemesUseCase, saveThemeUseCase, settingsRepository, mediaRepository, notificationRepository)
        viewModel.loadTheme("theme123")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveTheme()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { saveThemeUseCase.execute(any()) }
        assertEquals(false, viewModel.uiState.value.isDirty)
    }
}
