package com.example.fitbite.presentation.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbite.R
import com.example.fitbite.data.model.UserData
import com.example.fitbite.data.repository.UserRepository
import com.example.fitbite.domain.usecase.GetUserDataUseCase
import com.example.fitbite.domain.usecase.SaveUserDataUseCase
import com.example.fitbite.presentation.viewmodel.AuthViewModel
import com.example.fitbite.presentation.viewmodel.AuthViewModelFactory
import com.example.fitbite.presentation.viewmodel.InfoUserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class InfoUserActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth


    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            SaveUserDataUseCase(UserRepository()),
            GetUserDataUseCase(UserRepository()),
            auth // передаем FirebaseAuth
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_user)

        auth = FirebaseAuth.getInstance()

        // Загрузка данных пользователя при старте
        authViewModel.getUserData { userData ->
            if (userData != null) {
                // Используйте userData для отображения
                updateUI(auth.currentUser)
                displayUserData(userData)
            } else {
                // Покажите поля для ввода данных, если данные не найдены
                showInputFields()
            }
        }

       //Инициазация полей
       val welcomeTextView: TextView = findViewById(R.id.welcomeTextView)
       val weightEditText: EditText = findViewById(R.id.weightEditText)
       val heightEditText: EditText = findViewById(R.id.heightEditText)
       val ageEditText: EditText = findViewById(R.id.ageEditText)
       val genderSpinner: Spinner = findViewById(R.id.genderSpinner)
       val activitySpinner: Spinner = findViewById(R.id.activitySpinner)
       val resultSpinner: Spinner = findViewById(R.id.resultSpinner)
       val calculateButton: Button = findViewById(R.id.calculateButton)
       val caloriesResultTextView: TextView = findViewById(R.id.caloriesResultTextView)
       val bmiResultTextView: TextView = findViewById(R.id.bmiResultTextView)
        val waterIntakeTextView: TextView = findViewById(R.id.waterIntakeTextView)

        //? Проверка, авторизован ли пользователь
        if (auth.currentUser != null) {
            updateUI(auth.currentUser)
        }

        //? Подключаем адаптеры для спиннеров
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

        // Обработчик нажатия кнопки для расчета
        calculateButton.setOnClickListener {
            val weight = weightEditText.text.toString().toFloatOrNull()
            val height = heightEditText.text.toString().toFloatOrNull()
            val age = ageEditText.text.toString().toIntOrNull()
            val gender = genderSpinner.selectedItem.toString()
            val activity = activitySpinner.selectedItem.toString()
            val result = resultSpinner.selectedItem.toString()



            if (weight == null || height == null || age == null) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val infoUserViewModel = InfoUserViewModel()

            // Рассчитываем норму воды
            val waterIntake = infoUserViewModel.calculateWaterIntake(weight, age, gender, activity, result)

            // Отображаем результат нормы воды
            waterIntakeTextView.text = "Норма воды: %.2f литра".format(waterIntake)

            // Рассчитываем норму калорий в зависимости от выбранных параметров
            val calories = infoUserViewModel.calculateCalories(weight, height, age, gender, activity, result)

            // Отображаем результат
            caloriesResultTextView.text = "Ваша норма калорий: $calories ккал"


            // Рассчитываем индекс массы тела
            val bmi = infoUserViewModel.calculateBMI(weight, height)

            // Определяем категорию ИМТ и выводим соответствующее сообщение
            val bmiCategory = when {
                bmi < 18.5 -> {
                    val requiredWeightToGain = infoUserViewModel.calculateWeightToGain(bmi, weight, height)
                    "ИМТ: %.2f Недостаточный вес. \n\nДля нормализации нужно набрать минимум %.2f кг.".format(bmi, requiredWeightToGain)
                }
                bmi in 18.5..24.9 -> {
                    "ИМТ: %.2f Нормальный вес.".format(bmi)
                }
                bmi in 25.0..29.9 -> {
                    val requiredWeightToLose = infoUserViewModel.calculateWeightToLose(bmi, weight, height)
                    "ИМТ: %.2f Избыточный вес. \n\nДля нормализации нужно сбросить минимум %.2f кг.".format(bmi, requiredWeightToLose)
                }
                else -> {
                    val requiredWeightToLose = infoUserViewModel.calculateWeightToLose(bmi, weight, height)
                    "ИМТ: %.2f Ожирение. \n\nДля нормализации нужно сбросить минимум %.2f кг.".format(bmi, requiredWeightToLose)
                }
            }
            // Отображаем результат ИМТ
            bmiResultTextView.text = bmiCategory


            // Проверка, что все поля заполнены
            if ( weight != null && height != null && age != null) {
                // Создаем объект UserData с введенными данными
                val userData = UserData(
                    weight = weight,
                    height = height,
                    age = age,
                    gender = gender,
                    activity = activity,
                    result = result,
                    calories = calories, // Пример значения, вы можете изменить
                    bmi = bmi // Пример значения, вы можете изменить
                )

                // Сохраняем данные в Firebase через ViewModel
                authViewModel.saveUserData(userData)
            }
        }
    }


    override fun onStart() {
        super.onStart()

        // Проверка авторизации и обновление UI при старте активности
        val user = auth.currentUser
        if (user != null) {
            // Загружаем данные из Firestore после входа
            authViewModel.getUserData { userData ->
                if (userData != null) {
                    displayUserData(userData)
                    // После загрузки данных обновляем UI с именем или email
                    updateUI(user)
                }
            }
        } else {
            updateUI(null)  // Если нет авторизованного пользователя, показываем дефолтный текст
        }
    }


    //Обновляет видимость элементов в зависимости от того, авторизован ли пользователь
    private fun updateUI(user: FirebaseUser?) {
        val welcomeTextView: TextView = findViewById(R.id.welcomeTextView)

        // Очистить поля перед обновлением данных
        clearFields()

        if (user != null) {
            // Логирование для проверки данных пользователя
            Log.d("InfoUserActivity", "User displayName: ${user.displayName}, User email: ${user.email}")

            // Отображаем имя пользователя или email, если displayName не доступно
            val displayNameOrEmail = user.displayName ?: user.email
            if (!displayNameOrEmail.isNullOrEmpty()) {
                welcomeTextView.text = "Добро пожаловать, $displayNameOrEmail!"
            } else {
                welcomeTextView.text = "Добро пожаловать!"
            }

            // Загрузите данные нового пользователя из Firestore
            loadUserDataFromFirestore()
        } else {
            // Если пользователь не авторизован, отображаем дефолтное приветствие
            welcomeTextView.text = "Добро пожаловать!"
        }
    }


    // Загрузить данные из Firestore
    private fun loadUserDataFromFirestore() {
        authViewModel.getUserData { userData ->
            if (userData != null) {
                // Обновите UI с данными
                displayUserData(userData)
            } else {
                // Если данных нет, покажите поля для ввода
                showInputFields()
            }
        }
    }

    // Метод для отображения данных в UI
    private fun displayUserData(userData: UserData) {

        findViewById<EditText>(R.id.weightEditText).setText(userData.weight.toString())
        findViewById<EditText>(R.id.heightEditText).setText(userData.height.toString())
        findViewById<EditText>(R.id.ageEditText).setText(userData.age.toString())
        findViewById<Spinner>(R.id.genderSpinner).setSelection(getGenderIndex(userData.gender))
        findViewById<Spinner>(R.id.activitySpinner).setSelection(getActivityIndex(userData.activity))
        findViewById<Spinner>(R.id.resultSpinner).setSelection(getResultIndex(userData.result))

        // Программно вызываем кнопку для расчета
        val calculateButton: Button = findViewById(R.id.calculateButton)
        calculateButton.performClick()
    }

    // Метод для отображения полей для ввода данных, если данных нет
    private fun showInputFields() {
        findViewById<EditText>(R.id.weightEditText).visibility = View.VISIBLE
        findViewById<EditText>(R.id.heightEditText).visibility = View.VISIBLE
        findViewById<EditText>(R.id.ageEditText).visibility = View.VISIBLE
        findViewById<Spinner>(R.id.genderSpinner).visibility = View.VISIBLE
        findViewById<Spinner>(R.id.activitySpinner).visibility = View.VISIBLE
        findViewById<Spinner>(R.id.resultSpinner).visibility = View.VISIBLE
    }

    // Функции для получения индексов из Spinner
    private fun getGenderIndex(gender: String): Int {
        val genders = resources.getStringArray(R.array.genders)
        return genders.indexOf(gender)
    }

    private fun getActivityIndex(activity: String): Int {
        val activities = resources.getStringArray(R.array.activities)
        return activities.indexOf(activity)
    }

    private fun getResultIndex(result: String): Int {
        val results = resources.getStringArray(R.array.results)
        return results.indexOf(result)
    }

    // Очистить все поля, включая рассчитанные данные (ИМТ, калории)
    private fun clearFields() {
        // Очистить данные ввода
        findViewById<EditText>(R.id.weightEditText).text.clear()
        findViewById<EditText>(R.id.heightEditText).text.clear()
        findViewById<EditText>(R.id.ageEditText).text.clear()
        findViewById<Spinner>(R.id.genderSpinner).setSelection(0)  // сброс значения
        findViewById<Spinner>(R.id.activitySpinner).setSelection(0)  // сброс значения
        findViewById<Spinner>(R.id.resultSpinner).setSelection(0)    // сброс значения

        // Очистить рассчитанные значения ИМТ и калории
        findViewById<TextView>(R.id.bmiResultTextView).text = ""
        findViewById<TextView>(R.id.caloriesResultTextView).text = ""
        findViewById<TextView>(R.id.waterIntakeTextView).text = ""
    }

}