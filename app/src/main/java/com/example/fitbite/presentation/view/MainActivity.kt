package com.example.fitbite.presentation.view

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.fitbite.R
import com.example.fitbite.presentation.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import java.text.Format
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.Field


class MainActivity : AppCompatActivity() {

    // Хранилище для состояния темы
    private lateinit var sharedPreferences: SharedPreferences
    private val currentUserId = getCurrentUserId()
    private val authViewModel: AuthViewModel by viewModels()

    // Часы
    private lateinit var clockView: ClockView
    private lateinit var messageTextView: TextView

    // Календарь
    private lateinit var calendarLayout: LinearLayout

    ////Fit Google
    //private lateinit var stepsTextView: TextView
    //private lateinit var permissionLauncher: ActivityResultLauncher<IntentSenderRequest>
    //private lateinit var fitnessOptions: FitnessOptions
    //private val GOOGLE_FIT_REQUEST_CODE = 1001




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        findViewById<Button>(R.id.btnActivity).setOnClickListener {
            // Создаем новый экземпляр ActivityDialogFragment
            val fragment = ActivityDialogFragment()

            // Отображаем фрагмент как диалог
            fragment.show(supportFragmentManager, fragment.tag)
        }
        findViewById<Button>(R.id.btnProduct).setOnClickListener {
            val fragment = ProductDialogFragment()
            fragment.show(supportFragmentManager, fragment.tag)
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


       // stepsTextView = findViewById(R.id.stepsTextView)
       // setupGoogleFit()
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

   // private fun setupGoogleFit() {
   //     fitnessOptions = FitnessOptions.builder()
   //         .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
   //         .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_READ)
   //         .build()
//
   //     permissionLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
   //         if (result.resultCode == RESULT_OK) {
   //             val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
   //             fetchSteps(account)
   //         } else {
   //             Toast.makeText(this, "Google Fit разрешение не предоставлено", Toast.LENGTH_SHORT).show()
   //         }
   //     }
//
   //     checkGoogleFitPermission()
   // }
//
//
   // private fun checkGoogleFitPermission() {
   //     val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
//
   //     if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
   //         GoogleSignIn.requestPermissions(
   //             this,
   //             1, // requestCode (обрабатывается в onActivityResult, если нужно)
   //             account,
   //             fitnessOptions
   //         )
   //     } else {
   //         fetchSteps(account)
   //     }
   // }
//
//
   // private fun fetchSteps(account: GoogleSignInAccount) {
   //     Fitness.getHistoryClient(this, account)
   //         .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
   //         .addOnSuccessListener { dataSet ->
   //             val totalSteps = if (dataSet.isEmpty) 0 else {
   //                 val dataPoint = dataSet.dataPoints[0]
   //                 dataPoint.getValue(Field.FIELD_STEPS).asInt()
   //             }
//
   //             stepsTextView.text = "Шаги сегодня: $totalSteps"
   //         }
   //         .addOnFailureListener {
   //             Toast.makeText(this, "Ошибка при получении шагов", Toast.LENGTH_SHORT).show()
   //         }
   // }

}





