package com.aodstudio.app.domain.usecase

import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.repository.ThemeRepository
import javax.inject.Inject

/**
 * Use case to save or update an AOD theme.
 */
class SaveThemeUseCase @Inject constructor(
    private val repository: ThemeRepository
) {
    suspend fun execute(theme: AODTheme): Result<AODTheme> {
        return repository.saveTheme(theme)
    }

    suspend fun setActive(id: String): Result<Unit> {
        return repository.setActiveThemeId(id)
    }
}
