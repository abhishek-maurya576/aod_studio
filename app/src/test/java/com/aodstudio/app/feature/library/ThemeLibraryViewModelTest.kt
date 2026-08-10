package com.aodstudio.app.feature.library

import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.usecase.DeleteThemeUseCase
import com.aodstudio.app.domain.usecase.DuplicateThemeUseCase
import com.aodstudio.app.domain.usecase.GetThemesUseCase
import com.aodstudio.app.domain.usecase.ImportExportThemeUseCase
import com.aodstudio.app.domain.usecase.SaveThemeUseCase
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
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ThemeLibraryViewModel state transitions and use case invocations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ThemeLibraryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getThemesUseCase = mockk<GetThemesUseCase>()
    private val saveThemeUseCase = mockk<SaveThemeUseCase>()
    private val deleteThemeUseCase = mockk<DeleteThemeUseCase>()
    private val duplicateThemeUseCase = mockk<DuplicateThemeUseCase>()
    private val importExportThemeUseCase = mockk<ImportExportThemeUseCase>()

    private val testTheme = AODTheme.createDefaultTheme("Test Theme")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { getThemesUseCase.execute() } returns Result.Success(listOf(testTheme))
        coEvery { getThemesUseCase.getActiveTheme() } returns Result.Success(testTheme)
        coEvery { saveThemeUseCase.setActive(any()) } returns Result.Success(Unit)
        coEvery { duplicateThemeUseCase.execute(any()) } returns Result.Success(testTheme)
        coEvery { deleteThemeUseCase.execute(any()) } returns Result.Success(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadThemes updates state with fetched themes and active ID`() = runTest {
        val viewModel = ThemeLibraryViewModel(
            getThemesUseCase, saveThemeUseCase, deleteThemeUseCase, duplicateThemeUseCase, importExportThemeUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(1, state.themes.size)
        assertEquals(testTheme.id, state.activeThemeId)
    }

    @Test
    fun `selectCategory filters theme list`() = runTest {
        val viewModel = ThemeLibraryViewModel(
            getThemesUseCase, saveThemeUseCase, deleteThemeUseCase, duplicateThemeUseCase, importExportThemeUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectCategory("Minimal")
        assertEquals("Minimal", viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `activateTheme invokes saveThemeUseCase setActive`() = runTest {
        val viewModel = ThemeLibraryViewModel(
            getThemesUseCase, saveThemeUseCase, deleteThemeUseCase, duplicateThemeUseCase, importExportThemeUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.activateTheme(testTheme.id)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { saveThemeUseCase.setActive(testTheme.id) }
        assertEquals(testTheme.id, viewModel.uiState.value.activeThemeId)
    }

    @Test
    fun `deleteTheme calls deleteThemeUseCase`() = runTest {
        val viewModel = ThemeLibraryViewModel(
            getThemesUseCase, saveThemeUseCase, deleteThemeUseCase, duplicateThemeUseCase, importExportThemeUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteTheme(testTheme.id)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { deleteThemeUseCase.execute(testTheme.id) }
    }
}
