package com.example.myapplication

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AudioViewModel(application: Application) : AndroidViewModel(application) {
    private var mediaPlayer: MediaPlayer? = null
    private var updateJob: Job? = null
    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val songIds: List<Int> = R.raw::class.java.fields
        .filter { it.type == Int::class.javaPrimitiveType }
        .sortedBy { it.name }
        .map { it.getInt(null) }

    var currentSongIndex by mutableIntStateOf(0)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var currentPosition by mutableIntStateOf(0)
        private set

    var totalDuration by mutableIntStateOf(0)
        private set

    private val proximitySensorHelper = ProximitySensorHelper(application) {
        togglePlayPause()
    }

    init {
        loadSong()
        proximitySensorHelper.start()
    }

    private fun loadSong() {
        mediaPlayer?.release()
        if (songIds.isNotEmpty()) {
            mediaPlayer = MediaPlayer.create(getApplication(), songIds[currentSongIndex])
            mediaPlayer?.let {
                totalDuration = it.duration
                it.setOnCompletionListener {
                    next()
                }
            }
            if (isPlaying) {
                mediaPlayer?.start()
                startPositionUpdates()
            }
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPlaying = false
                stopPositionUpdates()
            } else {
                it.start()
                isPlaying = true
                startPositionUpdates()
            }
        }
    }

    fun stop() {
        mediaPlayer?.let {
            it.pause()
            it.seekTo(0)
            currentPosition = 0
            isPlaying = false
            stopPositionUpdates()
        }
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        currentPosition = position
    }

    fun next() {
        if (songIds.isEmpty()) return
        currentSongIndex = (currentSongIndex + 1) % songIds.size
        currentPosition = 0
        loadSong()
    }

    fun previous() {
        if (songIds.isEmpty()) return
        currentSongIndex = if (currentSongIndex > 0) currentSongIndex - 1 else songIds.size - 1
        currentPosition = 0
        loadSong()
    }

    fun volumeUp() {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_RAISE,
            AudioManager.FLAG_SHOW_UI
        )
    }

    fun volumeDown() {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI
        )
    }

    private fun startPositionUpdates() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (isPlaying) {
                mediaPlayer?.let {
                    try {
                        currentPosition = it.currentPosition
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                delay(1000)
            }
        }
    }

    private fun stopPositionUpdates() {
        updateJob?.cancel()
        updateJob = null
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        proximitySensorHelper.stop()
    }
}
