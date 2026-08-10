package com.aodstudio.app.media

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of MediaRepository using Android MediaSessionManager & MediaController APIs.
 * Directly integrates with system media sessions rather than parsing notification text strings.
 */
@Singleton
class MediaRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : MediaRepository {

    private val _mediaState = MutableStateFlow(MediaState())
    override val mediaState: StateFlow<MediaState> = _mediaState.asStateFlow()

    private var activeController: MediaController? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateFromMetadataAndState(metadata, activeController?.playbackState)
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateFromMetadataAndState(activeController?.metadata, state)
        }

        override fun onSessionDestroyed() {
            clearMediaState()
        }
    }

    override fun updateMediaState(state: MediaState) {
        _mediaState.value = state
    }

    override fun clearMediaState() {
        activeController?.unregisterCallback(controllerCallback)
        activeController = null
        _mediaState.value = MediaState()
    }

    /**
     * Attaches to active MediaControllers provided by notification listener context.
     */
    fun attachMediaController(controller: MediaController) {
        activeController?.unregisterCallback(controllerCallback)
        activeController = controller
        controller.registerCallback(controllerCallback)
        updateFromMetadataAndState(controller.metadata, controller.playbackState)
    }

    private fun updateFromMetadataAndState(metadata: MediaMetadata?, state: PlaybackState?) {
        if (metadata == null && state == null) {
            clearMediaState()
            return
        }

        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
        val album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM)
        val duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L

        val isPlaying = state?.state == PlaybackState.STATE_PLAYING
        val progress = state?.position ?: 0L

        _mediaState.value = MediaState(
            title = title,
            artist = artist,
            album = album,
            durationMs = duration,
            progressMs = progress,
            isPlaying = isPlaying,
            packageName = activeController?.packageName
        )
    }
}
