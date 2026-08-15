package com.aodstudio.app.feature.library

import android.content.Context
import com.aodstudio.app.battery.BatteryRepository
import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.template.TemplateRegistry
import com.aodstudio.app.domain.usecase.DeleteThemeUseCase
import com.aodstudio.app.domain.usecase.GetThemesUseCase
import com.aodstudio.app.domain.usecase.ImportExportThemeUseCase
import com.aodstudio.app.domain.usecase.ResetThemeUseCase
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
 * Unit tests for ThemeLibraryViewModel dynamic categories, filtering, activation, and reset.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ThemeLibraryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val context = mockk<Context>(relaxed = true)
    private val getThemesUseCase = mockk<GetThemesUseCase>()
    private val saveThemeUseCase = mockk<SaveThemeUseCase>()
    private val deleteThemeUseCase = mockk<DeleteThemeUseCase>()
    private val resetThemeUseCase = mockk<ResetThemeUseCase>()
    private val importExportThemeUseCase = mockk<ImportExportThemeUseCase>()
    private val settingsRepository = mockk<com.aodstudio.app.domain.repository.SettingsRepository>(relaxed = true)
    private val templateRegistry = TemplateRegistry()
    private val batteryRepository = mockk<BatteryRepository>(relaxed = true)
    private val notificationRepository = mockk<NotificationRepository>(relaxed = true)
    private val mediaRepository = mockk<MediaRepository>(relaxed = true)

    private val testTheme = AODTheme.createDefaultTheme("Test Theme")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { getThemesUseCase.execute() } returns Result.Success(listOf(testTheme))
        coEvery { getThemesUseCase.getActiveTheme() } returns Result.Success(testTheme)
        coEvery { saveThemeUseCase.setActive(any()) } returns Result.Success(Unit)
        coEvery { resetThemeUseCase.execute(any()) } returns Result.Success(testTheme)
        coEvery { deleteThemeUseCase.execute(any()) } returns Result.Success(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadThemes updates state with fetched themes and dynamic categories`() = runTest {
        val viewModel = ThemeLibraryViewModel(
            context,
            getThemesUseCase,
            saveThemeUseCase,
            deleteThemeUseCase,
            resetThemeUseCase,
            importExportThemeUseCase,
            settingsRepository,
            templateRegistry,
            batteryRepository,
            notificationRepository,
            mediaRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(1, state.themes.size)
        assertEquals(testTheme.id, state.activeThemeId)
        assertTrue(state.categories.contains("All"))
        assertTrue(state.categories.contains("Minimal"))
        assertTrue(state.categories.contains("Digital"))
    }

    @Test
    fun `selectCategory filters theme list`() = runTest {
        val viewModel = ThemeLibraryViewModel(
            context,
            getThemesUseCase,
            saveThemeUseCase,
            deleteThemeUseCase,
            resetThemeUseCase,
            importExportThemeUseCase,
            settingsRepository,
            templateRegistry,
            batteryRepository,
            notificationRepository,
            mediaRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectCategory("Minimal")
        assertEquals("Minimal", viewModel.uiState.value.selectedCategory)
        assertEquals(1, viewModel.uiState.value.filteredThemes.size)

        viewModel.selectCategory("Analog")
        assertEquals(0, viewModel.uiState.value.filteredThemes.size)
    }

    @Test
    fun `activateTheme invokes saveThemeUseCase setActive`() = runTest {
        val viewModel = ThemeLibraryViewModel(
            context,
            getThemesUseCase,
            saveThemeUseCase,
            deleteThemeUseCase,
            resetThemeUseCase,
            importExportThemeUseCase,
            settingsRepository,
            templateRegistry,
            batteryRepository,
            notificationRepository,
            mediaRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.activateTheme(testTheme.id)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { saveThemeUseCase.setActive(testTheme.id) }
        assertEquals(testTheme.id, viewModel.uiState.value.activeThemeId)
    }

    @Test
    fun `resetThemeToDefault calls resetThemeUseCase`() = runTest {
        val viewModel = ThemeLibraryViewModel(
            context,
            getThemesUseCase,
            saveThemeUseCase,
            deleteThemeUseCase,
            resetThemeUseCase,
            importExportThemeUseCase,
            settingsRepository,
            templateRegistry,
            batteryRepository,
            notificationRepository,
            mediaRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.resetThemeToDefault(testTheme.id)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { resetThemeUseCase.execute(testTheme.id) }
    }

    @Test
    fun `deleteTheme calls deleteThemeUseCase`() = runTest {
        val viewModel = ThemeLibraryViewModel(
            context,
            getThemesUseCase,
            saveThemeUseCase,
            deleteThemeUseCase,
            resetThemeUseCase,
            importExportThemeUseCase,
            settingsRepository,
            templateRegistry,
            batteryRepository,
            notificationRepository,
            mediaRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteTheme(testTheme.id)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { deleteThemeUseCase.execute(testTheme.id) }
    }
}
