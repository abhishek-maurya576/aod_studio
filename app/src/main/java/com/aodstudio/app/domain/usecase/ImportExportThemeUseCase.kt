package com.aodstudio.app.domain.usecase

import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.repository.ThemeRepository
import javax.inject.Inject

/**
 * Use case to import or export theme definitions in JSON format.
 */
class ImportExportThemeUseCase @Inject constructor(
    private val repository: ThemeRepository
) {
    suspend fun importFromJson(jsonString: String): Result<AODTheme> {
        return repository.importTheme(jsonString)
    }

    suspend fun exportToJson(id: String): Result<String> {
        return repository.exportTheme(id)
    }
}
