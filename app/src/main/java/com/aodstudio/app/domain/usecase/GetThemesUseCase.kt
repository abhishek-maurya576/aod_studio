package com.aodstudio.app.domain.usecase

import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to fetch or observe all themes.
 */
class GetThemesUseCase @Inject constructor(
    private val repository: ThemeRepository
) {

    fun observe(): Flow<Result<List<AODTheme>>> {
        return repository.observeAllThemes()
    }

    suspend fun execute(): Result<List<AODTheme>> {
        return repository.getAllThemes()
    }

    suspend fun getById(id: String): Result<AODTheme> {
        return repository.getThemeById(id)
    }

    suspend fun getActiveTheme(): Result<AODTheme> {
        return when (val activeIdResult = repository.getActiveThemeId()) {
            is Result.Success -> repository.getThemeById(activeIdResult.data)
            is Result.Error -> activeIdResult
            else -> Result.Error("Error getting active theme")
        }
    }
}
