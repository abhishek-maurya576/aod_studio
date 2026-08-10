package com.aodstudio.app.domain.usecase

import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.repository.ThemeRepository
import javax.inject.Inject

/**
 * Use case to delete an AOD theme by ID.
 */
class DeleteThemeUseCase @Inject constructor(
    private val repository: ThemeRepository
) {
    suspend fun execute(id: String): Result<Boolean> {
        return repository.deleteTheme(id)
    }
}
