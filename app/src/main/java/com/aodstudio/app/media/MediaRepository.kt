package com.aodstudio.app.media

import kotlinx.coroutines.flow.StateFlow

/**
 * Domain repository interface for active media session observation & playback control.
 */
interface MediaRepository {

    /**
     * Observes current media playback state as a StateFlow.
     */
    val mediaState: StateFlow<MediaState>

    /**
     * Initializes listener for active Android system media sessions.
     */
    fun initSessionListener()

    /**
     * Toggles Play/Pause playback action on the active media controller.
     */
    fun playPause()

    /**
     * Skips to the next track on the active media controller.
     */
    fun skipToNext()

    /**
     * Skips to the previous track on the active media controller.
     */
    fun skipToPrevious()

    /**
     * Seeks active playback position to the target timestamp in milliseconds.
     */
    fun seekTo(positionMs: Long)

    /**
     * Updates active media playback state manually.
     */
    fun updateMediaState(state: MediaState)

    /**
     * Clears current media playback state.
     */
    fun clearMediaState()
}

