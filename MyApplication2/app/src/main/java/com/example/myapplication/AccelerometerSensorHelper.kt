package com.example.myapplication

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import java.util.Random

class AccelerometerSensorHelper(
    private val sensorManager: SensorManager?,
    private val accelerometer: Sensor?,
    private val audioManager: AudioManager?,
    private val songs: IntArray,
    private val random: Random,
    private var currentSongIndex: Int,
    private val loadSong: (Int, Boolean) -> Unit
) : SensorEventListener {

    private val TILT_THRESHOLD = 6.0f
    private val TILT_COOLDOWN_MS = 1500L
    private var lastTiltTime = 0L

    private val VOLUME_TILT_THRESHOLD = 6.0f
    private val VOLUME_COOLDOWN_MS = 500L
    private var lastVolumeTime = 0L

    private val SHAKE_THRESHOLD = 2.7f
    private val SHAKE_SLOP_TIME_MS = 500
    private val SHAKE_RESET_TIME_MS = 3000

    private var lastShakeTimestamp = 0L
    private var shakeCount = 0

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var shakeInitialized = false

    fun start() {
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val currentTime = System.currentTimeMillis()

        // =========================
        // TILT (NEXT / PREVIOUS)
        // =========================
        if (currentTime - lastTiltTime >= TILT_COOLDOWN_MS) {

            if (x < -TILT_THRESHOLD) {
                lastTiltTime = currentTime

                currentSongIndex++
                if (currentSongIndex >= songs.size) currentSongIndex = 0

                loadSong(currentSongIndex, true)

            } else if (x > TILT_THRESHOLD) {
                lastTiltTime = currentTime

                currentSongIndex--
                if (currentSongIndex < 0) currentSongIndex = songs.size - 1

                loadSong(currentSongIndex, true)
            }
        }

        // =========================
        // VOLUME
        // =========================
        val nowVolume = System.currentTimeMillis()

        if (nowVolume - lastVolumeTime > VOLUME_COOLDOWN_MS) {
            if (audioManager != null) {

                if (y > VOLUME_TILT_THRESHOLD) {
                    audioManager.adjustVolume(
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_SHOW_UI
                    )
                    lastVolumeTime = nowVolume

                } else if (y < -VOLUME_TILT_THRESHOLD) {
                    audioManager.adjustVolume(
                        AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_SHOW_UI
                    )
                    lastVolumeTime = nowVolume
                }
            }
        }

        // =========================
        // SHAKE (SHUFFLE)
        // =========================
        if (!shakeInitialized) {
            lastX = x
            lastY = y
            lastZ = z
            shakeInitialized = true
            return
        }

        val deltaX = x - lastX
        val deltaY = y - lastY
        val deltaZ = z - lastZ

        lastX = x
        lastY = y
        lastZ = z

        val acceleration =
            Math.abs(deltaX) +
                    Math.abs(deltaY) +
                    Math.abs(deltaZ)

        val now = System.currentTimeMillis()
        val REQUIRED_SHAKES = 3

        if (lastShakeTimestamp + SHAKE_RESET_TIME_MS < now) {
            shakeCount = 0
        }

        if (acceleration > SHAKE_THRESHOLD) {

            if (lastShakeTimestamp + SHAKE_SLOP_TIME_MS > now) return

            lastShakeTimestamp = now
            shakeCount++

            if (shakeCount >= REQUIRED_SHAKES) {

                shakeCount = 0

                var newIndex: Int

                do {
                    newIndex = random.nextInt(songs.size)
                } while (newIndex == currentSongIndex && songs.size > 1)

                currentSongIndex = newIndex

                loadSong(currentSongIndex, true)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}