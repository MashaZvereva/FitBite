package com.example.fitbite.presentation.view

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitbite.BuildConfig
import com.example.fitbite.R
import com.example.fitbite.data.model.DailyReport
import com.example.fitbite.data.model.DailyReportResponse
import com.example.fitbite.data.network.RetrofitInstance.api
import com.example.fitbite.data.storage.SessionManager
import com.example.fitbite.domain.usecase.sensor.RealStepProvider
import com.example.fitbite.domain.usecase.sensor.StepProvider
import com.example.fitbite.domain.usecase.sensor.calc.WaterViewModel
import com.example.fitbite.presentation.viewmodel.AuthViewModel
import com.example.fitbite.presentation.viewmodel.DailySummaryViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.remember


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
    private lateinit var calendarScrollView: HorizontalScrollView
    private lateinit var currentDateKey: String
    private var selectedDateView: TextView? = null

    // Трекер воды
    private lateinit var waterTextView: TextView
    private lateinit var waterViewModel: WaterViewModel

    // Остаток калорий
    private lateinit var caloriesTextView: TextView
    private lateinit var dailySummaryViewModel: DailySummaryViewModel
    private lateinit var burnedCaloriesTextView: TextView

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
        calendarScrollView = findViewById(R.id.calendarScrollView)
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
            waterTextView.text = "${water} мл"
        }

        // Шаги
        stepsTextView = findViewById(R.id.stepsTextView)
       // stepProvider = if (BuildConfig.USE_MOCK_STEP_PROVIDER) MockStepProvider() else RealStepProvider(this)
        stepProvider = if (BuildConfig.USE_MOCK_STEP_PROVIDER) RealStepProvider(this) else RealStepProvider(this)

        // Остаток калорий
        caloriesTextView = findViewById(R.id.caloriesTextView)
        burnedCaloriesTextView = findViewById(R.id.caloriTextView)
        dailySummaryViewModel = ViewModelProvider(this)[DailySummaryViewModel::class.java]
        dailySummaryViewModel.summaryLiveData.observe(this) { summary ->
            Log.d("DAILY_SUMMARY", "summary: $summary")
            caloriesTextView.text = "${summary.caloriesLeft} ккал"
            burnedCaloriesTextView.text = "${summary.caloriesBurned} ккал"
        }


        findViewById<ImageButton>(R.id.navProfile).setOnClickListener {
            val intent = Intent(this, InfoUserActivity::class.java)
            intent.putExtra("report_id", reportId)
            startActivityForResult(intent, INFO_REQUEST)
        }

        findViewById<ImageButton>(R.id.navSettings).setOnClickListener {
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
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        loadDailyReport(authToken, today) { id ->
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
        calendarLayout.removeAllViews()

        val today = Calendar.getInstance()
        val todayDay = today.get(Calendar.DAY_OF_MONTH)
        val todayMonth = today.get(Calendar.MONTH)
        val todayYear = today.get(Calendar.YEAR)
        currentDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.time)

        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.add(Calendar.DAY_OF_MONTH, -14)

        repeat(21) {
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)
            val selectedCal = cal.clone() as Calendar
            val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCal.time)
            val selectedDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

            val tv = TextView(this).apply {
                tag = selectedDateKey
                text = day.toString()
                gravity = Gravity.CENTER
                setTextColor(Color.BLACK)
                setPadding(0, 24, 0, 24)

                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.calendar_day_width),
                    resources.getDimensionPixelSize(R.dimen.calendar_day_width)
                ).apply {
                    setMargins(8, 0, 8, 0)
                }

                background = when (selectedDateKey) {
                    currentDateKey -> ContextCompat.getDrawable(this@MainActivity, R.drawable.calendar_today_outline)
                    else           -> ContextCompat.getDrawable(this@MainActivity, R.drawable.calendar_day_bg)
                }

                setOnClickListener {
                    highlightSelectedDay(this)

                    val token = sessionManager.fetchAuthToken()

                    loadDailyReport(token, selectedDate) { id ->
                        if (id != null && id != -1) {
                            reportId = id
                            waterViewModel.refreshWater(reportId)
                            dailySummaryViewModel.refreshSummary(reportId)
                        } else {
                            Toast.makeText(context, "Ошибка загрузки отчёта", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            calendarLayout.addView(tv)
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        calendarScrollView.post {
            calendarScrollView.scrollTo(calendarLayout.getChildAt(14).left, 0)
        }
    }

    private fun highlightSelectedDay(selectedView: TextView) {
        for (i in 0 until calendarLayout.childCount) {
            val child = calendarLayout.getChildAt(i)
            if (child is TextView) {
                val dateKey = child.tag as? String
                child.background = when {
                    dateKey == currentDateKey -> ContextCompat.getDrawable(this, R.drawable.calendar_today_outline)
                    else -> ContextCompat.getDrawable(this, R.drawable.calendar_day_bg)
                }
            }
        }

        selectedDateView = selectedView
        val dateKey = selectedView.tag as? String
        selectedView.background = when {
            dateKey == currentDateKey -> ContextCompat.getDrawable(this, R.drawable.calendar_today_selected)
            else -> ContextCompat.getDrawable(this, R.drawable.calendar_day_selected)
        }
    }



    private fun loadDailyReport(token: String?, date: String, callback: (Int?) -> Unit) {
        if (token.isNullOrEmpty()) return callback(null)
        val authHeader = "Bearer $token"
        val report = DailyReport(date = date, user = sessionManager.getUserIdFromToken().toString())
        api.createDailyReport(authHeader, report).enqueue(object : Callback<DailyReportResponse> {
            override fun onResponse(call: Call<DailyReportResponse>, response: Response<DailyReportResponse>) {
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








