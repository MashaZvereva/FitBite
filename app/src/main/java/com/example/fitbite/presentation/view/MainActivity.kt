package com.example.fitbite.presentation.view

import android.content.Intent
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbite.R
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AppCompatDelegate
import com.example.fitbite.presentation.viewmodel.AuthViewModel

class MainActivity : AppCompatActivity() {

    // Хранилище для состояния темы
    private lateinit var sharedPreferences: SharedPreferences
    private val currentUserId = getCurrentUserId()

    // Часы
    private lateinit var clockView: ClockView
    private lateinit var messageTextView: TextView

    // Календарь
    private lateinit var calendarLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE)


        // Проверяем токен при входе
        val authViewModel: AuthViewModel by viewModels()
        authViewModel.getToken { token ->
            if (token.isNullOrEmpty()) {
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
        }

        // Загружаем тему и применяем
        val isDarkMode = loadTheme(currentUserId)
        applyTheme(isDarkMode)

       //// Устанавливаем обработчик на кнопки
       //findViewById<Button>(R.id.btnInfoUser).setOnClickListener {
       //    startActivity(Intent(this, InfoUserActivity::class.java))
       //}
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<Button>(R.id.btnFood).setOnClickListener {
            startActivity(Intent(this, FoodActivity::class.java))
        }

        // Часы
        clockView = findViewById(R.id.clockView)
        messageTextView = findViewById(R.id.messageTextView)

        // Обновление времени каждую минуту
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                clockView.invalidate() // Обновляем часы
                updateMessage() // Обновляем сообщение
                handler.postDelayed(this, 60000) // Вызываем снова через минуту
            }
        }
        handler.post(runnable)

        // Инициализация компонента календаря
        calendarLayout = findViewById(R.id.calendarLayout)

        // Получаем текущую дату и день недели
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK) // Получаем текущий день недели (1-7)
        val currentMonth = calendar.get(Calendar.MONTH) // Месяц
        val currentYear = calendar.get(Calendar.YEAR) // Год

        // Получаем число текущего дня
        val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        // Начало недели (понедельник)
        val startOfWeek = calendar.clone() as Calendar
        startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek) // Переводим на понедельник

        // Добавляем 7 дней в календарь
        for (i in 0..6) {
            val dayTextView = TextView(this)
            val dayOfMonth = startOfWeek.get(Calendar.DAY_OF_MONTH)

            // Устанавливаем день месяца для отображения
            dayTextView.text = dayOfMonth.toString()

            // Настройка стиля для кружков
            dayTextView.setPadding(20, 20, 20, 20)
            dayTextView.gravity = android.view.Gravity.CENTER
            dayTextView.setTextColor(resources.getColor(android.R.color.white))

            // Проверяем, является ли это текущим днем
            if (dayOfMonth == currentDayOfMonth) {
                dayTextView.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
            } else {
                dayTextView.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
            }

            // Добавляем TextView в LinearLayout
            val layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            layoutParams.setMargins(5, 0, 5, 0)
            dayTextView.layoutParams = layoutParams

            calendarLayout.addView(dayTextView)

            // Перемещаем на следующий день
            startOfWeek.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    // Функция для применения выбранной темы
    private fun applyTheme(isDarkMode: Boolean) {
        val mode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    // Функция для загрузки темы текущего пользователя из SharedPreferences
    private fun loadTheme(userId: String): Boolean {
        return sharedPreferences.getBoolean("theme_$userId", false) // По умолчанию светлая тема
    }

    // Функция для получения уникального ID текущего пользователя
    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "default_user"
    }

    // Функция для обновления сообщения под часами
    private fun updateMessage() {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val message = when {
            currentHour in 6..8 -> "Время завтрака"
            currentHour in 12..14 -> "Время обеда"
            currentHour in 16..19 -> "Время ужина"
            currentHour in 20..21 -> "Пора готовиться ко сну "
            currentHour in 22..23 || currentHour in 0..5 -> "Время для сна"
            else -> "Хочешь перекусить?"
        }

        messageTextView.text = message
    }
}
