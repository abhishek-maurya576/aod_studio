package com.aodstudio.app.media

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import com.aodstudio.app.notification.service.AODNotificationListenerService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of MediaRepository using Android MediaSessionManager & MediaController APIs.
 * Universal support for active media apps (Spotify, YouTube Music, Apple Music, etc.).
 */
@Singleton
class MediaRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : MediaRepository {

    private val _mediaState = MutableStateFlow(MediaState())
    override val mediaState: StateFlow<MediaState> = _mediaState.asStateFlow()

    private var mediaSessionManager: MediaSessionManager? = null
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
            findAndAttachActiveSession()
        }
    }

    private val sessionsChangedListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        onActiveSessionsUpdated(controllers)
    }

    override fun initSessionListener() {
        try {
            if (mediaSessionManager == null) {
                mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            }
            val componentName = ComponentName(context, AODNotificationListenerService::class.java)
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionsChangedListener)
            mediaSessionManager?.addOnActiveSessionsChangedListener(sessionsChangedListener, componentName)
            
            findAndAttachActiveSession()
        } catch (e: Exception) {
            // SecurityException if Notification Listener permission is not granted
        }
    }

    private fun findAndAttachActiveSession() {
        try {
            val componentName = ComponentName(context, AODNotificationListenerService::class.java)
            val controllers = mediaSessionManager?.getActiveSessions(componentName)
            onActiveSessionsUpdated(controllers)
        } catch (e: Exception) {
            // Permission not yet granted
        }
    }

    private fun onActiveSessionsUpdated(controllers: List<MediaController>?) {
        if (controllers.isNullOrEmpty()) {
            return
        }

        // Prefer currently playing session, otherwise pick the first active media session
        val playingController = controllers.firstOrNull { 
            it.playbackState?.state == PlaybackState.STATE_PLAYING 
        } ?: controllers.firstOrNull()

        if (playingController != null && playingController != activeController) {
            attachMediaController(playingController)
        }
    }

    override fun playPause() {
        val controller = activeController ?: return
        val state = controller.playbackState?.state
        if (state == PlaybackState.STATE_PLAYING) {
            controller.transportControls.pause()
        } else {
            controller.transportControls.play()
        }
    }

    override fun skipToNext() {
        activeController?.transportControls?.skipToNext()
    }

    override fun skipToPrevious() {
        activeController?.transportControls?.skipToPrevious()
    }

    override fun seekTo(positionMs: Long) {
        activeController?.transportControls?.seekTo(positionMs.coerceAtLeast(0L))
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
     * Attaches to active MediaController provided by MediaSessionManager or notification context.
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

        val playbackStateInt = state?.state ?: PlaybackState.STATE_NONE
        if (playbackStateInt == PlaybackState.STATE_NONE ||
            playbackStateInt == PlaybackState.STATE_STOPPED ||
            playbackStateInt == PlaybackState.STATE_ERROR
        ) {
            // Auto-hide when media playback is inactive/stopped/error
            _mediaState.value = MediaState()
            return
        }

        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
        if (title.isNullOrBlank()) {
            _mediaState.value = MediaState()
            return
        }

        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
        val album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM)
        val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)

        val albumArtBitmap: Bitmap? = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
        val artworkUri: String? = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ART_URI)

        val isPlaying = playbackStateInt == PlaybackState.STATE_PLAYING ||
                playbackStateInt == PlaybackState.STATE_BUFFERING ||
                playbackStateInt == PlaybackState.STATE_FAST_FORWARDING ||
                playbackStateInt == PlaybackState.STATE_REWINDING
        val progress = state?.position ?: 0L

        _mediaState.value = MediaState(
            title = title,
            artist = artist,
            album = album,
            artworkUri = artworkUri,
            albumArtBitmap = albumArtBitmap,
            durationMs = duration,
            progressMs = progress,
            isPlaying = isPlaying,
            packageName = activeController?.packageName
        )
    }
}

