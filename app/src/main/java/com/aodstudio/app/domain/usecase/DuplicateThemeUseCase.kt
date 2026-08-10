package com.aodstudio.app.domain.usecase

import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.repository.ThemeRepository
import javax.inject.Inject

/**
 * Use case to duplicate an existing AOD theme.
 */
class DuplicateThemeUseCase @Inject constructor(
    private val repository: ThemeRepository
) {
    suspend fun execute(id: String, newName: String? = null): Result<AODTheme> {
        return repository.duplicateTheme(id, newName)
    }
}
