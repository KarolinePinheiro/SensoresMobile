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

    // Configurações de Inclinação (Tilt)
    private val TILT_THRESHOLD = 7.5f
    private val TILT_COOLDOWN_MS = 1500L
    private var lastTiltTime = 0L

    // Configurações de Volume
    private val VOLUME_TILT_THRESHOLD = 7.5f
    private val VOLUME_COOLDOWN_MS = 500L
    private var lastVolumeTime = 0L

    // Configurações de Abanar (Shake)
    private val SHAKE_THRESHOLD = 12.0f // Aumentado significativamente para detetar "picos" de força G
    private val SHAKE_SLOP_TIME_MS = 250L
    private val SHAKE_RESET_TIME_MS = 1500L
    private val REQUIRED_SHAKES = 3

    private var lastShakeTimestamp = 0L
    private var shakeCount = 0
    private var lastHighAccelerationTime = 0L // Para bloquear o Tilt durante o movimento

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var initialized = false

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) // DELAY_UI é mais rápido para SHAKE
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

        if (!initialized) {
            lastX = x; lastY = y; lastZ = z
            initialized = true
            return
        }

        // 1. CÁLCULO DE ACELERAÇÃO (FORÇA G)
        // Usamos a diferença para detetar o "impacto" do movimento
        val deltaX = abs(x - lastX)
        val deltaY = abs(y - lastY)
        val deltaZ = abs(z - lastZ)
        val totalDelta = deltaX + deltaY + deltaZ

        lastX = x; lastY = y; lastZ = z

        // Se houver qualquer movimento minimamente brusco, marcamos o tempo
        // Isso ajuda a ignorar o Tilt enquanto o telemóvel está a ser mexido
        if (totalDelta > 4.0f) {
            lastHighAccelerationTime = currentTime
        }

        // 2. LÓGICA DO SHAKE (SHUFFLE)
        if (currentTime - lastShakeTimestamp > SHAKE_RESET_TIME_MS) {
            shakeCount = 0
        }

        if (totalDelta > SHAKE_THRESHOLD) {
            if (currentTime - lastShakeTimestamp > SHAKE_SLOP_TIME_MS) {
                lastShakeTimestamp = currentTime
                shakeCount++

                if (shakeCount >= REQUIRED_SHAKES) {
                    shakeCount = 0
                    onShuffle()
                    lastTiltTime = currentTime + 1000 // Bloqueia tilt por 1s extra após shuffle
                    return
                }
            }
        }

        // 3. LÓGICA DO TILT (NEXT/PREV/VOLUME)
        // Só processa inclinação se o telemóvel estiver "estável" (sem aceleração brusca recente)
        val isDeviceStable = (currentTime - lastHighAccelerationTime) > 300

        if (isDeviceStable) {
            // NEXT / PREVIOUS (Eixo X)
            if (currentTime - lastTiltTime >= TILT_COOLDOWN_MS) {
                if (x < -TILT_THRESHOLD) {
                    onNext()
                    lastTiltTime = currentTime
                } else if (x > TILT_THRESHOLD) {
                    onPrevious()
                    lastTiltTime = currentTime
                }
            }

            // VOLUME (Eixo Y)
            if (currentTime - lastVolumeTime > VOLUME_COOLDOWN_MS) {
                if (y > VOLUME_TILT_THRESHOLD) {
                    onVolumeUp()
                    lastVolumeTime = currentTime
                } else if (y < -VOLUME_TILT_THRESHOLD) {
                    onVolumeDown()
                    lastVolumeTime = currentTime
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
