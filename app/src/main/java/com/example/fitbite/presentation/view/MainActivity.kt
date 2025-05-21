package com.example.fitbite.presentation.view

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
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
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.fitbite.data.model.DailyReport
import com.example.fitbite.data.model.DailyReportResponse
import com.example.fitbite.data.network.RetrofitInstance.api
import com.example.fitbite.data.storage.SessionManager
import com.example.fitbite.domain.usecase.sensor.MockStepProvider
import com.example.fitbite.domain.usecase.sensor.RealStepProvider
import com.example.fitbite.domain.usecase.sensor.StepProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.fitbite.BuildConfig
import com.example.fitbite.data.network.RetrofitInstance
import com.example.fitbite.domain.usecase.sensor.calc.WaterViewModel
import com.example.fitbite.presentation.viewmodel.DailySummaryViewModel
import kotlinx.coroutines.launch


// ViewModel
class MainViewModel : ViewModel() {
    private val _calories = MutableLiveData<Int>()
    val calories: LiveData<Int> get() = _calories

    fun updateCalories(newCalories: Int) {
        _calories.value = newCalories
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var mainViewModel: MainViewModel

    // Хранилище для состояния темы
    private lateinit var sharedPreferences: SharedPreferences
    private val authViewModel: AuthViewModel by viewModels()

    // Шагометр
    private lateinit var stepProvider: StepProvider
    private lateinit var stepsTextView: TextView

    // Часы
    private lateinit var clockView: ClockView
    private lateinit var messageTextView: TextView

    // Календарь
    private lateinit var calendarLayout: LinearLayout

    // Трекер воды
    private lateinit var waterTextView: TextView
    private lateinit var waterViewModel: WaterViewModel

    // Остаток калорий
    private lateinit var caloriesTextView: TextView
    private lateinit var dailySummaryViewModel: DailySummaryViewModel

    // Контейнер для фрагментов «Активность»
    private lateinit var fragmentContainer: FrameLayout

    // Локальный отчёт
    private var reportId: Int = -1

    // Сессия
    private val sessionManager by lazy { SessionManager(this) }

    // Кнопки навигации
    private val INFO_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE)

        // Проверка токена
        authViewModel.getToken { token ->
            if (token.isNullOrEmpty()) {
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
        }

        // Пользователь
        val userId = sessionManager.getUserIdFromToken()
        Log.d("USER_ID", "ID пользователя: $userId")

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // Инициализация «часы + сообщение»
        clockView       = findViewById(R.id.clockView)
        messageTextView = findViewById(R.id.messageTextView)
        startClockUpdater()

        // Календарь
        calendarLayout = findViewById(R.id.calendarLayout)
        initWeekCalendar()

        // Фрагменты и кнопки
        fragmentContainer = findViewById(R.id.fragment_container)

        // Вода
        waterTextView = findViewById(R.id.waterTextView)
        waterViewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[WaterViewModel::class.java]
        waterViewModel.waterLiveData.observe(this) { water ->
            waterTextView.text = "Вода: ${water} мл"
        }

        // Шаги
        stepsTextView = findViewById(R.id.stepsTextView)
       // stepProvider = if (BuildConfig.USE_MOCK_STEP_PROVIDER) MockStepProvider() else RealStepProvider(this)
        stepProvider = if (BuildConfig.USE_MOCK_STEP_PROVIDER) RealStepProvider(this) else RealStepProvider(this)

        // Остаток калорий
        caloriesTextView = findViewById(R.id.caloriesTextView)
        dailySummaryViewModel = ViewModelProvider(this)[DailySummaryViewModel::class.java]
        dailySummaryViewModel.summaryLiveData.observe(this) { summary ->
            caloriesTextView.text = "Осталось: ${summary.caloriesLeft} ккал"
        }


        findViewById<Button>(R.id.btnInfoUser).setOnClickListener {
            val intent = Intent(this, InfoUserActivity::class.java)
            intent.putExtra("report_id", reportId)
            startActivityForResult(intent, INFO_REQUEST)
        }

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<Button>(R.id.btnFood).setOnClickListener {
            startActivity(Intent(this, FoodActivity::class.java))
        }

        val btnBreakfast = findViewById<Button>(R.id.btnBreakfast)
        val btnLunch     = findViewById<Button>(R.id.btnLunch)
        val btnDinner    = findViewById<Button>(R.id.btnDinner)
        val btnSnack     = findViewById<Button>(R.id.btnSnack)
        val btnActivity  = findViewById<Button>(R.id.btnActivity)

        listOf(btnBreakfast, btnLunch, btnDinner, btnSnack).forEach { it.isEnabled = false }
        btnActivity.isEnabled = false
        val container = findViewById<FrameLayout>(R.id.fragment_container)

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                container.visibility = View.GONE
            }
        }

        // Создаём/загружаем отчёт
        val authToken = sessionManager.fetchAuthToken()
        loadDailyReport(authToken) { id ->
            if (id != null && id != -1) {
                reportId = id
                // теперь можно переключаться между приёмами пищи
                btnBreakfast.isEnabled = true
                btnLunch.isEnabled     = true
                btnDinner.isEnabled    = true
                btnSnack.isEnabled     = true
                btnActivity.isEnabled  = true

                // подтягиваем воду и калории
                waterViewModel.refreshWater(reportId)
                dailySummaryViewModel.refreshSummary(reportId)
            } else {
                Toast.makeText(this, "Не удалось загрузить отчёт", Toast.LENGTH_SHORT).show()
            }
        }

        btnBreakfast.setOnClickListener { openMealFragment("breakfast") }
        btnLunch    .setOnClickListener { openMealFragment("lunch") }
        btnDinner   .setOnClickListener { openMealFragment("dinner") }
        btnSnack    .setOnClickListener { openMealFragment("snack") }
        btnActivity .setOnClickListener {
            openActivityPage(fragmentContainer, reportId)
        }
    }

    private fun startClockUpdater() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                clockView.invalidate()
                updateMessage()
                handler.postDelayed(this, 60_000L)
            }
        })
    }

    private fun initWeekCalendar() {
        val cal = Calendar.getInstance()
        val today = cal.get(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        repeat(7) {
            val tv = TextView(this).apply {
                text = cal.get(Calendar.DAY_OF_MONTH).toString()
                setPadding(20,20,20,20)
                gravity = Gravity.CENTER
                setTextColor(Color.BLACK)
                setBackgroundColor(
                    if (cal.get(Calendar.DAY_OF_MONTH)==today)
                        Color.GREEN else ContextCompat.getColor(this@MainActivity, R.color.border_color)
                )
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f).apply {
                    setMargins(5,0,5,0)
                }
            }
            calendarLayout.addView(tv)
            cal.add(Calendar.DAY_OF_MONTH,1)
        }
    }


    private fun loadDailyReport(token: String?, callback: (Int?) -> Unit) {
        if (token.isNullOrEmpty()) return callback(null)
        val authHeader = "Bearer $token"
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val report = DailyReport(date=today, user=sessionManager.getUserIdFromToken().toString())
        api.createDailyReport(authHeader, report).enqueue(object : Callback<DailyReportResponse> {
            override fun onResponse(
                call: Call<DailyReportResponse>,
                response: Response<DailyReportResponse>
            ) {
                callback(response.body()?.id)
            }
            override fun onFailure(call: Call<DailyReportResponse>, t: Throwable) {
                callback(null)
            }
        })
    }

    private fun openMealFragment(mealType: String) {
        if (reportId == -1) return
        val frag = MealFragment.newInstance(mealType, reportId)
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, frag)
            .addToBackStack(null)
            .commit()
    }

    private fun openActivityPage(container: FrameLayout, reportId: Int) {
        if (reportId == -1) return
        container.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ActivityPageFragment.newInstance(reportId))
            .addToBackStack(null)
            .commit()
    }

    private fun updateMessage() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        messageTextView.text = when {
            hour in 6..8   -> "Время завтрака"
            hour in 12..14 -> "Время обеда"
            hour in 16..19 -> "Время ужина"
            hour in 20..21 -> "Пора готовиться ко сну"
            else           -> "Хочешь перекусить?"
        }
    }

    override fun onResume() {
        super.onResume()
        stepProvider.start { steps ->
            runOnUiThread { stepsTextView.text = "Шаги: $steps" }
        }
    }

    override fun onPause() {
        super.onPause()
        stepProvider.stop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == INFO_REQUEST && resultCode == Activity.RESULT_OK) {
            // Пользователь только что пересчитал — обновляем остаток калорий
            dailySummaryViewModel.refreshSummary(reportId)
        }
    }
}








