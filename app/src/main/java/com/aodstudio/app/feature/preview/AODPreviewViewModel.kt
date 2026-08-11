package com.aodstudio.app.feature.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.usecase.GetThemesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the AOD Preview screen.
 * Loads a theme by ID for full-screen preview rendering.
 */
@HiltViewModel
class AODPreviewViewModel @Inject constructor(
    private val getThemesUseCase: GetThemesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AODPreviewUiState())
    val uiState: StateFlow<AODPreviewUiState> = _uiState.asStateFlow()

    fun loadTheme(themeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getThemesUseCase.getById(themeId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, theme = result.data)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                else -> {}
            }
        }
    }
}

data class AODPreviewUiState(
    val isLoading: Boolean = true,
    val theme: AODTheme? = null,
    val errorMessage: String? = null
)
