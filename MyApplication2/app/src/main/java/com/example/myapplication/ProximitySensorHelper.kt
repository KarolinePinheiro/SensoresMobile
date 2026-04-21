package com.example.myapplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * Classe Helper para gerir o sensor de proximidade.
 * Ajustada para Deteção Instantânea.
 */
class ProximitySensorHelper(
    context: Context,
    private val onTrigger: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val proximitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private var lastActionTime: Long = 0L
    private val COOLDOWN_MS = 1500L // Intervalo mínimo entre comandos (1.5 segundos)

    fun start() {
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
            Log.d("ProximitySensor", "Iniciado em modo de alta sensibilidade.")
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
            val distance = event.values[0]
            val maxRange = proximitySensor?.maximumRange ?: 0f

            // Lógica de Deteção Ultra Sensível:
            // Se a distância for menor que o máximo, ou menor que um limiar fixo (5cm), considera "perto".
            val isNear = distance < maxRange || distance < 5.0f
            val currentTime = System.currentTimeMillis()

            if (isNear) {
                // Verifica apenas o intervalo de segurança para não pausar/dar play repetidamente
                if (currentTime - lastActionTime >= COOLDOWN_MS) {
                    lastActionTime = currentTime
                    Log.d("ProximitySensor", "Gatilho instantâneo detetado! Distância: $distance")
                    onTrigger()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
