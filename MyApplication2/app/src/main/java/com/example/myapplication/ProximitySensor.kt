package com.example.myapplication

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class ProximitySensor : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null

    // Controlo de tempo e estado
    private var lastActionTime: Long = 0L
    private val actionHandler = Handler(Looper.getMainLooper())

    // Runnable que será executado após 2 segundos de mão parada
    private val actionRunnable = Runnable {
        executeMusicAction()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        if (proximitySensor == null) {
            Log.e("Sensor", "Sensor de proximidade não disponível!")
            finish()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
            val distance = event.values[0]
            val maxRange = proximitySensor?.maximumRange ?: 0f

            // Verifica se a mão está perto
            val isNear = distance < maxRange
            val currentTime = System.currentTimeMillis()

            if (isNear) {
                // 1. Verifica se já passaram 4 segundos desde a última vez que a música alterou
                if (currentTime - lastActionTime >= 4000) {
                    // 2. Se a mão acabou de chegar, agenda a ação para daqui a 2 segundos
                    // Usamos o 'hasCallbacks' para não agendar múltiplos se o sensor repetir eventos
                    if (!actionHandler.hasMessages(0)) {
                        Log.d("Sensor", "Mão detectada. Aguardando 2 segundos...")
                        actionHandler.postDelayed(actionRunnable, 2000)
                    }
                } else {
                    Log.d("Sensor", "Aguarde o intervalo de 4 segundos.")
                }
            } else {
                // Se a mão for removida, cancelamos qualquer ação agendada
                Log.d("Sensor", "Mão removida. Operação cancelada.")
                actionHandler.removeCallbacks(actionRunnable)
            }
        }
    }

    private fun executeMusicAction() {
        // Esta função só corre se a mão ficou 2s e o intervalo de 4s foi respeitado
        lastActionTime = System.currentTimeMillis()
        toggleMusic()
    }

    private fun toggleMusic() {
        // Lógica de Play/Pause (conecta isto ao teu MediaPlayer ou Service)
        Log.d("ProximitySensor", "Ação executada: Alternando música!")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // --- Melhores Práticas de Ciclo de Vida ---

    override fun onResume() {
        super.onResume()
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Para evitar bugs, cancelamos qualquer ação pendente ao sair da app
        actionHandler.removeCallbacks(actionRunnable)
        sensorManager.unregisterListener(this)
    }

    override fun onStop() {
        super.onStop()
        sensorManager.unregisterListener(this)
    }
}