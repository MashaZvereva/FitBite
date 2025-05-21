package com.example.fitbite.domain.usecase.sensor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.fitbite.R
import com.example.fitbite.domain.usecase.sensor.RealStepProvider

class StepCounterForegroundService : Service() {

    private lateinit var stepProvider: RealStepProvider
    private var savedSteps: Int = 0

    companion object {
        private const val PREFS_NAME = "step_prefs"
        private const val KEY_SAVED_STEPS = "saved_steps"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification(0))

        // Загружаем сохранённые шаги
        savedSteps = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_SAVED_STEPS, 0)

        stepProvider = RealStepProvider(this)
        stepProvider.start { sessionSteps ->
            val totalSteps = savedSteps + sessionSteps
            updateNotification(totalSteps)
        }
    }

    override fun onDestroy() {
        saveSteps()
        stepProvider.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun saveSteps() {
        // Сохраняем накопленные шаги (сессия + предыдущие)
        val stepsNow = stepProvider.getCurrentRawSteps() // реализуем метод
        if (stepsNow != -1) {
            val totalSteps = savedSteps + (stepsNow - stepProvider.getStartRawSteps())
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_SAVED_STEPS, totalSteps)
                .apply()
        }
    }

    private fun createNotification(steps: Int): Notification {
        return NotificationCompat.Builder(this, "step_channel")
            .setContentTitle("Шагомер")
            .setContentText("Шаги: $steps")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(steps: Int) {
        val notification = createNotification(steps)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "step_channel",
                "Шагомер",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
