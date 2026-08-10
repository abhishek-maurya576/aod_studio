package com.aodstudio.app.media

/**
 * Data class representing current active media session playback state.
 * Display-only initially (title, artist, album, artworkUri, progress, duration, playing state).
 */
data class MediaState(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val artworkUri: String? = null,
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val isPlaying: Boolean = false,
    val packageName: String? = null
) {
    val hasActiveMedia: Boolean
        get() = !title.isNullOrBlank()
}
