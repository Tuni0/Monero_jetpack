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

    // Instancja ExoPlayer
    private var _exoPlayer: ExoPlayer? = null

    val exoPlayer: ExoPlayer?
        get() = _exoPlayer

    // Pozycja startowa
    var playbackPosition by mutableStateOf(0L)
        private set

    // Odtwarzaj wideo jak player się naczyta
    var playWhenReady by mutableStateOf(false)
        private set

    // Buforowanie
    var isBuffering by mutableStateOf(true)
        private set

    // Tryb fullscren i czy odtwarzać wideo
    var isFullscreen by mutableStateOf(false)
    var shouldPlayVideo by mutableStateOf(false)

    // Inicjalizacja odtwarzacza
    fun initializePlayer(context: Context, videoUri: String) {
        if (_exoPlayer == null) {
            _exoPlayer = ExoPlayer.Builder(context).build().apply {

                // źródło , przygotowanie, ustawienie pozycji i odtwarzania, dodanie listenera buforowania
                setMediaItem(MediaItem.fromUri(videoUri))

                prepare()

                seekTo(playbackPosition)

                playWhenReady = this@VideoPlayerViewModel.playWhenReady

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        isBuffering = (state == Player.STATE_BUFFERING)
                    }
                })
            }
        }
    }

    // Zwalnianie odtwarzacza
    fun releasePlayer() {
        _exoPlayer?.run {

            playbackPosition = currentPosition

            playWhenReady = this.playWhenReady

            release()
        }
        _exoPlayer = null
    }

    // Stop i reset
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