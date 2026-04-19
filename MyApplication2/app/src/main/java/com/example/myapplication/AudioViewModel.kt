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
import kotlin.random.Random

class AudioViewModel(application: Application) : AndroidViewModel(application) {
    private var mediaPlayer: MediaPlayer? = null
    private var updateJob: Job? = null
    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val rawFields = R.raw::class.java.fields
        .filter { it.type == Int::class.javaPrimitiveType }
        .sortedBy { it.name }

    private val drawableFields = R.drawable::class.java.fields
        .filter { it.type == Int::class.javaPrimitiveType }
        .associate { it.name to it.getInt(null) }

    val songIds: List<Int> = rawFields.map { it.getInt(null) }
    private val songNames: List<String> = rawFields.map { it.name }

    var currentSongIndex by mutableIntStateOf(0)
        private set

    var currentSongTitle by mutableStateOf("")
        private set

    var currentSongImage by mutableIntStateOf(R.drawable.album_cover)
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

    private val accelerometerSensorHelper = AccelerometerSensorHelper(
        context = application,
        onNext = { next() },
        onPrevious = { previous() },
        onShuffle = { playRandomSong() },
        onVolumeUp = { volumeUp() },
        onVolumeDown = { volumeDown() }
    )

    init {
        loadSong()
        proximitySensorHelper.start()
        accelerometerSensorHelper.start()
    }

    private fun loadSong() {
        mediaPlayer?.release()
        if (songIds.isNotEmpty()) {
            val rawName = songNames[currentSongIndex]
            currentSongTitle = formatTitle(rawName)
            
            val resourceId = drawableFields[rawName] ?: 0
            currentSongImage = if (resourceId != 0) resourceId else R.drawable.album_cover

            mediaPlayer = MediaPlayer.create(getApplication(), songIds[currentSongIndex])
            mediaPlayer?.let {
                totalDuration = it.duration
                it.setOnCompletionListener { next() }
            }
            if (isPlaying) {
                mediaPlayer?.start()
                startPositionUpdates()
            }
        }
    }

    private fun formatTitle(rawName: String): String {
        return rawName.replace("_", " ")
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
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

    fun playRandomSong() {
        if (songIds.size > 1) {
            var nextIndex: Int
            do {
                nextIndex = Random.nextInt(songIds.size)
            } while (nextIndex == currentSongIndex)
            
            currentSongIndex = nextIndex
            currentPosition = 0
            // If it was already playing, keep playing. If not, maybe we should start?
            // Usually shuffle starts the song.
            isPlaying = true
            loadSong()
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
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
    }

    fun volumeDown() {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
    }

    private fun startPositionUpdates() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (isPlaying) {
                mediaPlayer?.let { currentPosition = it.currentPosition }
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
        accelerometerSensorHelper.stop()
    }
}
