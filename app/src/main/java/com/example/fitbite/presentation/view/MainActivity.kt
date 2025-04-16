package com.example.fitbite.presentation.view

import android.content.Context
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
import androidx.appcompat.app.AppCompatDelegate
import com.example.fitbite.R
import com.example.fitbite.presentation.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.fitbite.presentation.view.MealFragment


class MainActivity : AppCompatActivity(), SensorEventListener {

    // Хранилище для состояния темы
    private lateinit var sharedPreferences: SharedPreferences
    private val currentUserId = getCurrentUserId()
    private val authViewModel: AuthViewModel by viewModels()

    // Часы
    private lateinit var clockView: ClockView
    private lateinit var messageTextView: TextView

    // Календарь
    private lateinit var calendarLayout: LinearLayout

    // Шагометр
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var stepsAtStart = -1
    private lateinit var stepsTextView: TextView

    // Трекер воды
    private lateinit var waterTextView: TextView
    private var waterIntake: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE)

        // Проверка токена
        authViewModel.getToken { token ->
            if (token.isNullOrEmpty()) {
                // Токен не найден — переходим в AuthActivity
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                finish() // Закрываем MainActivity
            } else {
                // Токен есть — продолжаем загрузку интерфейса
                setContentView(R.layout.activity_main)
            }
        }

        // Загружаем тему и применяем
        val isDarkMode = loadTheme(currentUserId)
        applyTheme(isDarkMode)

        // Устанавливаем обработчик на кнопки
        findViewById<Button>(R.id.btnInfoUser).setOnClickListener {
            startActivity(Intent(this, InfoUserActivity::class.java))
        }
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<Button>(R.id.btnFood).setOnClickListener {
            startActivity(Intent(this, FoodActivity::class.java))
        }
       // findViewById<Button>(R.id.btnActivity).setOnClickListener {
       //     // Создаем новый экземпляр ActivityDialogFragment
       //     val fragment = ActivityDialogFragment()
//
       //     // Отображаем фрагмент как диалог
       //     fragment.show(supportFragmentManager, fragment.tag)
       // }
       // findViewById<Button>(R.id.btnProduct).setOnClickListener {
       //     val fragment = ProductDialogFragment()
       //     fragment.show(supportFragmentManager, fragment.tag)
       // }

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
        startOfWeek.set(
            Calendar.DAY_OF_WEEK,
            startOfWeek.firstDayOfWeek
        ) // Переводим на понедельник

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
            val layoutParams =
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            layoutParams.setMargins(5, 0, 5, 0)
            dayTextView.layoutParams = layoutParams

            calendarLayout.addView(dayTextView)

            // Перемещаем на следующий день
            startOfWeek.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Шагометр
        stepsTextView = findViewById(R.id.stepsTextView)
        setupStepSensor()


        // Кнопки для перехода на фрагменты
        findViewById<Button>(R.id.btnBreakfast).setOnClickListener {
            openMealFragment("Завтрак")
            Log.d("MainActivity", "Кнопка Завтрак нажата")
            openMealFragment("Завтрак")
        }
        findViewById<Button>(R.id.btnLunch).setOnClickListener {
            openMealFragment("Обед")
        }
        findViewById<Button>(R.id.btnDinner).setOnClickListener {
            openMealFragment("Ужин")
        }
        findViewById<Button>(R.id.btnSnack).setOnClickListener {
            openMealFragment("Перекус")
        }
    }


    private fun openMealFragment(mealType: String) {
        val fragment = MealFragment.newInstance(mealType)

        // Начинаем транзакцию
        val transaction = supportFragmentManager.beginTransaction()

        // Просто добавляем фрагмент в активити (не обязательно использовать контейнер)
        transaction.replace(android.R.id.content, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }


    // Функция для применения выбранной темы
    private fun applyTheme(isDarkMode: Boolean) {
        val mode =
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
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

    private fun setupStepSensor() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor != null) {
            Log.d("SensorCheck", "Шагомер найден!")
            // Подключай слушатель
        } else {
            Log.e("SensorCheck", "Датчик шагов не найден!")
        }
    }

        override fun onResume() {
        super.onResume()
        stepSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val steps = event.values[0].toInt()
            if (stepsAtStart == -1) {
                stepsAtStart = steps
            }
            val currentSteps = steps - stepsAtStart
            stepsTextView.text = "Шаги сегодня: $currentSteps"
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Метод обязателен, даже если не используется
    }

}






