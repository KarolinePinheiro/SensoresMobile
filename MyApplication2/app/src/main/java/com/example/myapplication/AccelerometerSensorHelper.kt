package com.example.myapplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

class AccelerometerSensorHelper(
    context: Context,
    private val onNext: () -> Unit,
    private val onPrevious: () -> Unit,
    private val onShuffle: () -> Unit,
    private val onVolumeUp: () -> Unit,
    private val onVolumeDown: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

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
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val currentTime = System.currentTimeMillis()

        // TILT (NEXT / PREVIOUS)
        if (currentTime - lastTiltTime >= TILT_COOLDOWN_MS) {
            if (x < -TILT_THRESHOLD) {
                lastTiltTime = currentTime
                onNext()
            } else if (x > TILT_THRESHOLD) {
                lastTiltTime = currentTime
                onPrevious()
            }
        }

        // VOLUME
        if (currentTime - lastVolumeTime > VOLUME_COOLDOWN_MS) {
            if (y > VOLUME_TILT_THRESHOLD) {
                onVolumeUp()
                lastVolumeTime = currentTime
            } else if (y < -VOLUME_TILT_THRESHOLD) {
                onVolumeDown()
                lastVolumeTime = currentTime
            }
        }

        // SHAKE (SHUFFLE)
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

        val acceleration = abs(deltaX) + abs(deltaY) + abs(deltaZ)

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
                onShuffle()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
