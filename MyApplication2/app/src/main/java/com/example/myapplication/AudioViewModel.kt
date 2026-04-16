package com.example.myapplication

import android.app.Application
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
                    nextSong()
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

    private fun nextSong() {
        currentSongIndex = (currentSongIndex + 1) % songIds.size
        currentPosition = 0
        loadSong()
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
