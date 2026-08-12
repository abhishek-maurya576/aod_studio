package com.aodstudio.app.feature.onboarding

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aodstudio.app.aod.compatibility.VivoAdapter
import com.aodstudio.app.config.AppConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Extension property to create DataStore on the application Context.
private val Context.onboardingDataStore by preferencesDataStore(
    name = AppConfig.Storage.PREFERENCES_NAME
)

data class OnboardingUiState(
    /** True once all 3 onboarding steps have been completed (persisted in DataStore). */
    val hasCompletedOnboarding: Boolean = false,
    /** Currently displayed step index: 0=Battery, 1=Autostart, 2=RecentsLock */
    val currentStep: Int = 0,
    /** True if the device is Vivo — onboarding only shown for Vivo/OriginOS. */
    val isVivoDevice: Boolean = false,
    /** Snackbar message to display (null = no message). */
    val userMessage: String? = null
)

/**
 * OnboardingViewModel — manages the Vivo OriginOS 3-step survival onboarding flow.
 *
 * Persists completion state in DataStore so the wizard only shows once.
 * Exposes OEM deep-link intents via [VivoAdapter] for each step.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val vivoAdapter: VivoAdapter
) : ViewModel() {

    companion object {
        private const val TAG = "OnboardingViewModel"
        private val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey(
            AppConfig.Service.PREF_ONBOARDING_COMPLETE
        )
        const val TOTAL_STEPS = 3
    }

    private val _uiState = MutableStateFlow(
        OnboardingUiState(isVivoDevice = vivoAdapter.isVivoDevice)
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        loadOnboardingState()
    }

    // ──────────────────────────────────────────────────────────────────────────
    // State loading
    // ──────────────────────────────────────────────────────────────────────────

    private fun loadOnboardingState() {
        viewModelScope.launch {
            val completed = context.onboardingDataStore.data
                .map { prefs -> prefs[KEY_ONBOARDING_COMPLETE] ?: false }
                .first()
            _uiState.update { it.copy(hasCompletedOnboarding = completed) }
            Log.d(TAG, "Onboarding completed: $completed, isVivo: ${vivoAdapter.isVivoDevice}")
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Step navigation
    // ──────────────────────────────────────────────────────────────────────────

    fun nextStep() {
        val next = _uiState.value.currentStep + 1
        if (next >= TOTAL_STEPS) {
            markOnboardingComplete()
        } else {
            _uiState.update { it.copy(currentStep = next) }
        }
    }

    fun previousStep() {
        val prev = (_uiState.value.currentStep - 1).coerceAtLeast(0)
        _uiState.update { it.copy(currentStep = prev) }
    }

    /** Marks onboarding complete without requiring the user to go through every step. */
    fun skipOnboarding() {
        _uiState.update { it.copy(userMessage = "You can revisit these settings anytime under Settings > Vivo Setup") }
        markOnboardingComplete()
    }

    private fun markOnboardingComplete() {
        viewModelScope.launch {
            context.onboardingDataStore.edit { prefs ->
                prefs[KEY_ONBOARDING_COMPLETE] = true
            }
            _uiState.update { it.copy(hasCompletedOnboarding = true) }
            Log.i(TAG, "Onboarding marked complete")
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // OEM deep-link intents (one per step)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Step 0: Battery > High Background Power Consumption whitelist.
     * UNVERIFIED — see [VivoAdapter.getBatteryHighBackgroundIntent].
     */
    fun getBatteryHighBackgroundIntent(): Intent? =
        vivoAdapter.getBatteryHighBackgroundIntent(context)

    /**
     * Step 1: iManager > Autostart Manager (enable autostart for this app).
     * UNVERIFIED — see [VivoAdapter.getAutostartIntent].
     */
    fun getAutostartIntent(): Intent? =
        vivoAdapter.getAutostartIntent(context)

    /**
     * Step 2: No deep-link available for "lock app in recents" — this is a
     * manual gesture (padlock icon in the app card in the Recents screen).
     * This step provides on-screen instructions only.
     */
    fun getRecentsLockIntent(): Intent? = null

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
