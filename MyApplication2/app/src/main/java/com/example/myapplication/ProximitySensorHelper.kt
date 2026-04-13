package com.example.myapplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * Classe Helper para gerir o sensor de proximidade sem ser uma Activity.
 */
class ProximitySensorHelper(
    context: Context,
    private val onTrigger: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val proximitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    // Controlo de tempo e estado
    private var lastActionTime: Long = 0L
    private val actionHandler = Handler(Looper.getMainLooper())

    // Runnable que será executado após 2 segundos de mão parada
    private val actionRunnable = Runnable {
        lastActionTime = System.currentTimeMillis()
        onTrigger()
    }

    /**
     * Inicia a monitorização do sensor (Chamar no onResume da Activity)
     */
    fun start() {
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("ProximitySensor", "Sensor de proximidade iniciado.")
        } ?: Log.e("ProximitySensor", "Sensor de proximidade não disponível!")
    }

    /**
     * Para a monitorização (Chamar no onPause/onStop da Activity)
     */
    fun stop() {
        actionHandler.removeCallbacks(actionRunnable)
        sensorManager.unregisterListener(this)
        Log.d("ProximitySensor", "Sensor de proximidade parado.")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
            val distance = event.values[0]
            val maxRange = proximitySensor?.maximumRange ?: 0f

            // Verifica se a mão está perto
            val isNear = distance < maxRange
            val currentTime = System.currentTimeMillis()

            if (isNear) {
                // 1. Verifica se já passaram 4 segundos desde a última ação
                if (currentTime - lastActionTime >= 4000) {
                    // 2. Agenda a ação para daqui a 2 segundos
                    if (!actionHandler.hasMessages(0)) {
                        Log.d("ProximitySensor", "Mão detectada. Aguardando 2 segundos...")
                        actionHandler.postDelayed(actionRunnable, 2000)
                    }
                } else {
                    Log.d("ProximitySensor", "Aguarde o intervalo de 4 segundos.")
                }
            } else {
                // Se a mão for removida, cancelamos qualquer ação agendada
                actionHandler.removeCallbacks(actionRunnable)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
