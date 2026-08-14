package com.aodstudio.app.domain.usecase

import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.repository.ThemeRepository
import javax.inject.Inject

/**
 * Use case to reset a template back to its factory default definition.
 */
class ResetThemeUseCase @Inject constructor(
    private val repository: ThemeRepository
) {
    suspend fun execute(id: String): Result<AODTheme> {
        return repository.resetThemeToDefault(id)
    }
}
