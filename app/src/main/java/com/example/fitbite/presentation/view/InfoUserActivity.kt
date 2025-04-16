package com.example.fitbite.presentation.view

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fitbite.R
import com.example.fitbite.data.model.UserParameters
import com.example.fitbite.data.network.RetrofitInstance
import com.example.fitbite.presentation.viewmodel.AuthViewModel
import com.example.fitbite.presentation.viewmodel.InfoUserViewModel
import kotlinx.coroutines.launch
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

class InfoUserActivity : AppCompatActivity() {

    private val apiService = RetrofitInstance.api
    private lateinit var sharedPreferences: SharedPreferences
    val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_user)

        // Инициализируем SharedPreferences
        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE)

        // Получаем токен через ViewModel
        authViewModel.getToken { token ->
            token?.let {
                getUserData(it)
            }
        }

        // Инициализация полей
        val weightEditText: EditText = findViewById(R.id.weightEditText)
        val targetweightEditText: EditText = findViewById(R.id.targetweightEditText)
        val heightEditText: EditText = findViewById(R.id.heightEditText)
        val ageEditText: EditText = findViewById(R.id.ageEditText)
        val genderSpinner: Spinner = findViewById(R.id.genderSpinner)
        val activitySpinner: Spinner = findViewById(R.id.activitySpinner)
        val resultSpinner: Spinner = findViewById(R.id.resultSpinner)
        val calculateButton: Button = findViewById(R.id.calculateButton)
        val caloriesResultTextView: TextView = findViewById(R.id.caloriesResultTextView)
        val bmiResultTextView: TextView = findViewById(R.id.bmiResultTextView)
        val waterIntakeTextView: TextView = findViewById(R.id.waterIntakeTextView)
        val chart = findViewById<LineChart>(R.id.weightLineChart)
        val entries = ArrayList<com.github.mikephil.charting.data.Entry>()


        targetweightEditText.visibility = EditText.GONE

        // Подключаем адаптеры для спиннеров
        val genderAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.genders,
            android.R.layout.simple_spinner_item
        )
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        val activityAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.activities,
            android.R.layout.simple_spinner_item
        )
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        activitySpinner.adapter = activityAdapter

        val resultAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.results,
            android.R.layout.simple_spinner_item
        )
        resultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        resultSpinner.adapter = resultAdapter


        resultSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val selectedGoal = resultSpinner.selectedItem.toString()
                // Показываем поле, если цель НЕ "Поддержание веса"
                if (selectedGoal == "Поддержание веса") {
                    targetweightEditText.visibility = EditText.GONE
                } else {
                    targetweightEditText.visibility = EditText.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Обработчик нажатия кнопки для расчета
        calculateButton.setOnClickListener {
            val weight = weightEditText.text.toString().toFloatOrNull()
            val target_weight = targetweightEditText.text.toString().toFloatOrNull()
            val height = heightEditText.text.toString().toIntOrNull()
            val age = ageEditText.text.toString().toIntOrNull()
            val gender = genderSpinner.selectedItem.toString()
            val activityLevel = activitySpinner.selectedItem.toString()
            val goal = resultSpinner.selectedItem.toString()

            if (weight == null || height == null || age == null) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (target_weight == null) {
                Toast.makeText(this, "Введите желаемый вес", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (goal == "Набор массы" && target_weight < weight) {
                Toast.makeText(
                    this,
                    "Целевой вес должен быть больше текущего для набора массы",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            if ((goal == "Быстрое похудение" || goal == "Умеренное похудение") && target_weight > weight) {
                Toast.makeText(
                    this,
                    "Целевой вес должен быть меньше текущего для похудения",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val infoUserViewModel = InfoUserViewModel()

            // Обновление параметров пользователя
            val userParameters =
                UserParameters(height, age, weight, target_weight, gender, activityLevel, goal)

            // Рассчитываем норму воды
            val waterIntake =
                infoUserViewModel.calculateWaterIntake(weight, age, gender, activityLevel, goal)

            // Отображаем результат нормы воды
            waterIntakeTextView.text = "Норма воды: %.2f литра".format(waterIntake)


            // Получаем норму калорий (целевую) и поддержку веса
            val (targetCalories) = infoUserViewModel.calculateCaloriesAndWeightChange(
                weight,
                height,
                age,
                gender,
                activityLevel,
                goal
            )

            // Отображаем результат
            caloriesResultTextView.text =
                "Ваша норма калорий: $targetCalories ккал"

            // Расчет времени похудения
            val timeToReach = calculateTimeToReachTargetWeight(
                weight,
                target_weight,
                height,
                age,
                gender,
                activityLevel,
                goal
            )

            val timeDescription = if (timeToReach != null) {
                "Ориентировочно вы достигнете цели за ${timeToReach.first} дней (~${timeToReach.second} недель)."
            } else {
                "Цель уже достигнута или темп изменения слишком мал."
            }

            // Добавим это к TextView с калориями:
            caloriesResultTextView.text =
                "Ваша норма калорий: $targetCalories ккал\n$timeDescription"


            // Рассчитываем индекс массы тела
            val bmi = infoUserViewModel.calculateBMI(weight, height)
            // Определяем категорию ИМТ и выводим соответствующее сообщение
            val bmiCategory = when {
                bmi < 18.5 -> {
                    val requiredWeightToGain =
                        infoUserViewModel.calculateWeightToGain(bmi, weight, height)
                    "ИМТ: %.2f Недостаточный вес. \n\nДля нормализации нужно набрать минимум %.2f кг.".format(
                        bmi,
                        requiredWeightToGain
                    )
                }

                bmi in 18.5..24.9 -> {
                    "ИМТ: %.2f Нормальный вес.".format(bmi)
                }

                bmi in 25.0..29.9 -> {
                    val requiredWeightToLose =
                        infoUserViewModel.calculateWeightToLose(bmi, weight, height)
                    "ИМТ: %.2f Избыточный вес. \n\nДля нормализации нужно сбросить минимум %.2f кг.".format(
                        bmi,
                        requiredWeightToLose
                    )
                }

                else -> {
                    val requiredWeightToLose =
                        infoUserViewModel.calculateWeightToLose(bmi, weight, height)
                    "ИМТ: %.2f Ожирение. \n\nДля нормализации нужно сбросить минимум %.2f кг.".format(
                        bmi,
                        requiredWeightToLose
                    )
                }
            }
            bmiResultTextView.text = bmiCategory

            // Получаем токен через ViewModel
            authViewModel.getToken { token ->
                token?.let {
                    // Обновляем данные пользователя на сервере
                    updateUserData(it, userParameters)
                } ?: run {
                    Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
                }
            }

            // Рассчитываем разницу между текущим и целевым весом
            if (target_weight != null && weight != null) {
                entries.clear()
                // Рассчитываем время, необходимое для достижения целевого веса
                val daysNeeded = calculateTimeToReachTargetWeight(weight, target_weight, height, age, gender, activityLevel, goal)?.first

                // Проверка на null для daysNeeded
                if (daysNeeded != null) {
                    // Промежуточные даты
                    val startDate = Calendar.getInstance() // Текущая дата
                    val midDate1 = Calendar.getInstance()
                    val midDate2 = Calendar.getInstance()
                    val midDate3 = Calendar.getInstance()
                    val targetDate = Calendar.getInstance()

                    // Добавляем дни до промежуточных и целевой дат
                    midDate1.add(Calendar.DAY_OF_YEAR, (daysNeeded * 0.25).toInt()) // 25% от времени
                    midDate2.add(Calendar.DAY_OF_YEAR, (daysNeeded * 0.5).toInt()) // 50% от времени
                    midDate3.add(Calendar.DAY_OF_YEAR, (daysNeeded * 0.75).toInt()) // 75% от времени
                    targetDate.add(Calendar.DAY_OF_YEAR, daysNeeded) // Целевая дата

                    // Промежуточные веса (можно сделать по-разному, например, линейно интерполировать)
                    val weightMid1 = weight + (target_weight - weight) * 0.25f
                    val weightMid2 = weight + (target_weight - weight) * 0.5f
                    val weightMid3 = weight + (target_weight - weight) * 0.75f

                    // Добавляем точки на график
                    entries.add(Entry(0f, weight)) // Начальный вес
                    entries.add(Entry(0.25f, weightMid1)) // Промежуточный вес (25%)
                    entries.add(Entry(0.5f, weightMid2)) // Промежуточный вес (50%)
                    entries.add(Entry(0.75f, weightMid3)) // Промежуточный вес (75%)
                    entries.add(Entry(1f, target_weight)) // Целевой вес

                    // Настройка графика
                    val dataSet = LineDataSet(entries, "").apply {
                        color = resources.getColor(R.color.green, null)
                        valueTextSize = 14f
                        lineWidth = 3f
                        setCircleColor(resources.getColor(R.color.green, null))
                        circleRadius = 5f
                    }

                    val lineData = LineData(dataSet)
                    chart.data = lineData
                    chart.axisRight.isEnabled = false
                    chart.axisLeft.axisMinimum = 0f
                    chart.description.isEnabled = false
                    chart.legend.isEnabled = false
                    chart.xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        granularity = 0.25f // Уменьшаем шаг, чтобы отобразить промежуточные даты
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return when (value) {
                                    0f -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(startDate.time) // Сегодняшняя дата
                                    0.25f -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(midDate1.time) // Промежуточная дата 25%
                                    0.5f -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(midDate2.time) // Промежуточная дата 50%
                                    0.75f -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(midDate3.time) // Промежуточная дата 75%
                                    1f -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(targetDate.time) // Целевая дата
                                    else -> ""
                                }
                            }
                        }
                    }
                    // Зеленый цвет для текста всех элементов графика
                    val greenColor = resources.getColor(R.color.green, null)
                    chart.xAxis.textColor = greenColor
                    chart.axisLeft.textColor = greenColor
                    chart.axisRight.textColor = greenColor
                    chart.legend.textColor = greenColor
                    chart.description.textColor = greenColor
                    dataSet.setCircleColor(greenColor)
                    dataSet.valueTextColor = greenColor

                    // Устанавливаем размер текста для осей
                    chart.xAxis.textSize = 12f
                    chart.axisLeft.textSize = 12f

                    // Разрешаем рисовать не только 0 и 1 на оси X, добавляем промежуточные значения
                    chart.xAxis.apply {
                        axisMinimum = 0f
                        axisMaximum = 1f
                        setLabelCount(5, false) // Устанавливаем 5 меток на оси X
                    }

                    chart.invalidate() // Обновление графика
                }
                if (goal == "Поддержание веса") {
                    // Только начальная и конечная точки с одинаковым весом
                    entries.add(Entry(0f, weight))
                    entries.add(Entry(1f, weight))

                    chart.invalidate() // Обновление графика
                }
            }
        }
    }


            private fun displayUserData(userData: UserParameters) {
        findViewById<EditText>(R.id.weightEditText).setText(userData.weight?.toString() ?: "")
        findViewById<EditText>(R.id.targetweightEditText).setText(userData.target_weight?.toString() ?: "")
        findViewById<EditText>(R.id.heightEditText).setText(userData.height?.toString() ?: "")
        findViewById<EditText>(R.id.ageEditText).setText(userData.age?.toString() ?: "")

        findViewById<Spinner>(R.id.genderSpinner).setSelection(getGenderIndex(userData.gender ?: ""))
        findViewById<Spinner>(R.id.activitySpinner).setSelection(getActivityIndex(userData.activity_level ?: ""))
        findViewById<Spinner>(R.id.resultSpinner).setSelection(getResultIndex(userData.goal ?: ""))

        findViewById<Button>(R.id.calculateButton).performClick()
    }

    private fun getGenderIndex(gender: String): Int {
        val genders = resources.getStringArray(R.array.genders)
        return genders.indexOf(gender ?: "")
    }

    private fun getActivityIndex(activity: String): Int {
        val activities = resources.getStringArray(R.array.activities)
        return activities.indexOf(activity ?: "")
    }

    private fun getResultIndex(result: String): Int {
        val results = resources.getStringArray(R.array.results)
        return results.indexOf(result ?: "")
    }

    private fun getUserData(token: String) {
        lifecycleScope.launch {
            try {
                val response = apiService.getUserParameters("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        val updatedUserData = convertUserDataToRussian(it)
                        displayUserData(updatedUserData)
                    }
                } else {
                    Log.e("InfoUserActivity", "Error: ${response.code()} - Unauthorized")
                    finish()
                }
            } catch (e: Exception) {
                Log.e("InfoUserActivity", "Error: ${e.message}")
            }
        }
    }

    private fun updateUserData(token: String, userData: UserParameters) {
        lifecycleScope.launch {
            val updatedUserData = convertUserDataToEnglish(userData)
            try {
                val response = apiService.updateUserParameters("Bearer $token", updatedUserData)
                if (response.isSuccessful) {
                    Log.d("InfoUserActivity", "User data updated successfully")
                } else {
                    Log.e("InfoUserActivity", "Error: ${response.code()} - Unauthorized")
                }
            } catch (e: Exception) {
                Log.e("InfoUserActivity", "Error: ${e.message}")
            }
        }
    }

// Функция для преобразования данных с английского на русский
    fun convertUserDataToRussian(userData: UserParameters): UserParameters {
        val gender = when (userData.gender) {
            "male" -> "Мужской"
            "female" -> "Женский"
            else -> "Женский"
        }

        val activityLevel = when (userData.activity_level) {
            "none" -> "Отсутствие физической активности"
            "low" -> "Легкая активность"
            "moderate" -> "Средняя активность"
            "high" -> "Высокая активность"
            "very_high" -> "Очень высокая активность"
            else -> "Средняя активность"
        }

        val goal = when (userData.goal) {
            "fast_lose_weight" -> "Быстрое похудение"
            "lose_weight" -> "Умеренное похудение"
            "maintain_weight" -> "Поддержание веса"
            "gain_weight" -> "Набор массы"
            else -> "Поддержание веса"
        }

        return userData.copy(
            gender = gender,
            activity_level = activityLevel,
            goal = goal
        )
    }

    // Функция для преобразования данных с русского на английский
    fun convertUserDataToEnglish(userData: UserParameters): UserParameters {
        val gender = when (userData.gender) {
            "Мужской" -> "male"
            "Женский" -> "female"
            else -> "female"
        }

        val activityLevel = when (userData.activity_level) {
            "Отсутствие физической активности" -> "none"
            "Легкая активность" -> "low"
            "Средняя активность" -> "moderate"
            "Высокая активность" -> "high"
            "Очень высокая активность" -> "very_high"
            else -> "moderate"
        }

        val goal = when (userData.goal) {
            "Быстрое похудение" -> "fast_lose_weight"
            "Умеренное похудение" -> "lose_weight"
            "Поддержание веса" -> "maintain_weight"
            "Набор массы" -> "gain_weight"
            else -> "maintain_weight"
        }

        return userData.copy(
            gender = gender,
            activity_level = activityLevel,
            goal = goal
        )
    }

    private fun calculateTimeToReachTargetWeight(
        currentWeight: Float,
        targetWeight: Float,
        height: Int,
        age: Int,
        gender: String,
        activityLevel: String,
        goal: String
    ): Pair<Int, Int>? {
        // Создаем временный ViewModel
        val viewModel = InfoUserViewModel()

        // Получаем ориентировочную норму калорий и изменение веса
        val (_, weightChangePerMonth) = viewModel.calculateCaloriesAndWeightChange(
            currentWeight,
            height,
            age,
            gender,
            activityLevel,
            goal
        )

        val weightDiff = abs(currentWeight - targetWeight).toDouble()

        // Проверка: цель уже достигнута или слишком маленькое изменение веса
        if (weightDiff < 0.1 || weightChangePerMonth == 0.0f) return null

        // Применяем поправочный коэффициент (например, вес уходит быстрее на 30%)
        val adjustmentFactor = 1.5f
        val adjustedWeightChange = weightChangePerMonth * adjustmentFactor

        val monthsNeeded = weightDiff / abs(adjustedWeightChange)
        val daysNeeded = (monthsNeeded * 30).toInt()
        val weeksNeeded = daysNeeded / 7

        return Pair(daysNeeded, weeksNeeded)
    }
}

