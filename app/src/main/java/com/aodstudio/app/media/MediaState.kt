package com.aodstudio.app.media

import android.graphics.Bitmap

/**
 * Data class representing current active media session playback state.
 * Contains live media metadata, playback state, and album artwork.
 */
data class MediaState(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val artworkUri: String? = null,
    val albumArtBitmap: Bitmap? = null,
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val isPlaying: Boolean = false,
    val packageName: String? = null
) {
    val hasActiveMedia: Boolean
        get() = !title.isNullOrBlank()
}

