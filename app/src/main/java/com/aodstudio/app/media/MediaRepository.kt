package com.aodstudio.app.media

import kotlinx.coroutines.flow.StateFlow

/**
 * Domain repository interface for active media session observation.
 */
interface MediaRepository {

    /**
     * Observes current media playback state as a StateFlow.
     */
    val mediaState: StateFlow<MediaState>

    /**
     * Updates active media playback state.
     */
    fun updateMediaState(state: MediaState)

    /**
     * Clears current media playback state.
     */
    fun clearMediaState()
}
