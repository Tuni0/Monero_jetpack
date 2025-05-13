package com.example.monero_jetpack

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class VideoPlayerViewModel : ViewModel() {

    // ExoPlayer instance (private to prevent external modification)
    private var _exoPlayer: ExoPlayer? = null

    // Public getter for ExoPlayer
    val exoPlayer: ExoPlayer?
        get() = _exoPlayer

    // Playback position (in milliseconds)
    var playbackPosition by mutableStateOf(0L)
        private set // Only the ViewModel can modify this

    // Whether playback should start automatically
    var playWhenReady by mutableStateOf(false)
        private set

    // Whether the player is currently buffering
    var isBuffering by mutableStateOf(true)
        private set

    var isFullscreen by mutableStateOf(false)
    var shouldPlayVideo by mutableStateOf(false)

    fun initializePlayer(context: Context, videoUri: String) {
        if (_exoPlayer == null) {
            _exoPlayer = ExoPlayer.Builder(context).build().apply {
                // Set the media item (video URI)
                setMediaItem(MediaItem.fromUri(videoUri))

                // Prepare the player to start playback
                prepare()

                // Seek to the saved playback position
                seekTo(playbackPosition)

                // Set whether playback should start automatically
                playWhenReady = this@VideoPlayerViewModel.playWhenReady

                // Add a listener to track buffering state
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        isBuffering = (state == Player.STATE_BUFFERING)
                    }
                })
            }
        }
    }
    /**
     * Release the player, saving any playback position or state.
     */
    fun releasePlayer() {
        _exoPlayer?.run {
            // Save the current playback position
            playbackPosition = currentPosition

            // Save whether playback should start automatically
            playWhenReady = this.playWhenReady

            // Release the player to free resources
            release()
        }
        _exoPlayer = null
    }
    fun stopPlayer() {
        exoPlayer?.pause()
    }

    fun resetPlayer() {
        exoPlayer?.seekTo(0)
        exoPlayer?.play()
    }
    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }

}