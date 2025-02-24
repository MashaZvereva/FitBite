package com.example.fitbite.presentation.view

import android.os.Bundle
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
import com.example.fitbite.domain.model.GetUserDataUseCase
import com.example.fitbite.domain.usecase.SaveUserDataUseCase
import com.example.fitbite.presentation.viewmodel.AuthViewModel
import com.example.fitbite.presentation.viewmodel.AuthViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest


class AuthActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth


    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            SaveUserDataUseCase(UserRepository()),
            GetUserDataUseCase(UserRepository())
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()

        // Загрузка данных пользователя при старте
        authViewModel.getUserData { userData ->
            if (userData != null) {
                // Используйте userData для отображения
                displayUserData(userData)
            } else {
                // Покажите поля для ввода данных, если данные не найдены
                showInputFields()
            }
        }

        // Скрыть все элементы сразу после создания
        hideAllFields()

        //? Проверка авторизации и обновление UI при старте активности
        updateUI(auth.currentUser)

        //Инициазация полей
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val actionButton: Button = findViewById(R.id.actionButton)
        val switchTextView: TextView = findViewById(R.id.switchTextView)
        val welcomeTextView: TextView = findViewById(R.id.welcomeTextView)
        val logoutButton: Button = findViewById(R.id.logout_button)
        val weightEditText: EditText = findViewById(R.id.weightEditText)
        val heightEditText: EditText = findViewById(R.id.heightEditText)
        val ageEditText: EditText = findViewById(R.id.ageEditText)
        val genderSpinner: Spinner = findViewById(R.id.genderSpinner)
        val activitySpinner: Spinner = findViewById(R.id.activitySpinner)
        val resultSpinner: Spinner = findViewById(R.id.resultSpinner)
        val calculateButton: Button = findViewById(R.id.calculateButton)
        val caloriesResultTextView: TextView = findViewById(R.id.caloriesResultTextView)
        val bmiResultTextView: TextView = findViewById(R.id.bmiResultTextView)

        var isLoginMode = true

        //? Проверка, авторизован ли пользователь
        if (auth.currentUser != null) {
            updateUI(auth.currentUser)
        }

        // Обработчик для входа и регистрации
        actionButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val username = usernameEditText.text.toString()

            if (isLoginMode) {
                loginUser(email, password)
            } else {
                registerUser(email, password, username)
            }
        }

        // Переключение между режимами входа и регистрации
        switchTextView.setOnClickListener {
            isLoginMode = !isLoginMode
            if (isLoginMode) {
                actionButton.text = "Вход"
                switchTextView.text = "У Вас еще нет аккаунта? Зарегистрируйтесь"
                usernameEditText.visibility = EditText.GONE

                // Скрыть поля для расчета, когда не авторизован
                weightEditText.visibility = EditText.GONE
                heightEditText.visibility = EditText.GONE
                ageEditText.visibility = EditText.GONE
                genderSpinner.visibility = Spinner.GONE
                activitySpinner.visibility = Spinner.GONE
                resultSpinner.visibility = Spinner.GONE
                calculateButton.visibility = Button.GONE
                bmiResultTextView.visibility = TextView.GONE
                caloriesResultTextView.visibility = TextView.GONE
            } else {
                actionButton.text = "Регистрация"
                switchTextView.text = "У Вас уже есть аккаунт? Войдите"
                usernameEditText.visibility = EditText.VISIBLE
            }
        }

        // Обработчик для выхода
        logoutButton.setOnClickListener {
            auth.signOut()
            updateUI(null)
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

            // Рассчитываем норму калорий в зависимости от выбранных параметров
            val calories = calculateCalories(weight, height, age, gender, activity, result)

            // Отображаем результат
            caloriesResultTextView.text = "Ваша норма калорий: $calories ккал"


            // Рассчитываем индекс массы тела
            val bmi = calculateBMI(weight, height)

            // Определяем категорию ИМТ и выводим соответствующее сообщение
            val bmiCategory = when {
                bmi < 18.5 -> {
                    val requiredWeightToGain = calculateWeightToGain(bmi, weight, height)
                    "ИМТ: %.2f Недостаточный вес. \nДля нормализации нужно набрать минимум %.2f кг.".format(bmi, requiredWeightToGain)
                }
                bmi in 18.5..24.9 -> {
                    "ИМТ: %.2f Нормальный вес.".format(bmi)
                }
                bmi in 25.0..29.9 -> {
                    val requiredWeightToLose = calculateWeightToLose(bmi, weight, height)
                    "ИМТ: %.2f Избыточный вес. \nДля нормализации нужно сбросить минимум %.2f кг.".format(bmi, requiredWeightToLose)
                }
                else -> {
                    val requiredWeightToLose = calculateWeightToLose(bmi, weight, height)
                    "ИМТ: %.2f Ожирение. \nДля нормализации нужно сбросить минимум %.2f кг.".format(bmi, requiredWeightToLose)
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
        // Проверка авторизации
        val user = auth.currentUser
        if (user != null) {
            // Загружаем данные из Firestore после входа
            authViewModel.getUserData { userData ->
                if (userData != null) {
                    displayUserData(userData)
                }
            }
        } else {
            updateUI(null)
        }
    }

    private fun registerUser(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Сохраняем имя пользователя в Firebase
                    val user = auth.currentUser
                    user?.let {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build()

                        it.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileUpdateTask ->
                                if (profileUpdateTask.isSuccessful) {
                                    Toast.makeText(this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show()
                                    updateUI(auth.currentUser)
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Ошибка обновления профиля: ${profileUpdateTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Регистрация не удалась: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    updateUI(auth.currentUser)
                } else {
                    Toast.makeText(
                        this,
                        "Вход не удался: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    //Обновляет видимость элементов в зависимости от того, авторизован ли пользователь
    private fun updateUI(user: FirebaseUser?) {
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        val actionButton: Button = findViewById(R.id.actionButton)
        val switchTextView: TextView = findViewById(R.id.switchTextView)
        val welcomeTextView: TextView = findViewById(R.id.welcomeTextView)
        val logoutButton: Button = findViewById(R.id.logout_button)
        val weightEditText: EditText = findViewById(R.id.weightEditText)
        val heightEditText: EditText = findViewById(R.id.heightEditText)
        val ageEditText: EditText = findViewById(R.id.ageEditText)
        val genderSpinner: Spinner = findViewById(R.id.genderSpinner)
        val activitySpinner: Spinner = findViewById(R.id.activitySpinner)
        val resultSpinner: Spinner = findViewById(R.id.resultSpinner)
        val calculateButton: Button = findViewById(R.id.calculateButton)
        val caloriesResultTextView: TextView = findViewById(R.id.caloriesResultTextView)
        val bmiResultTextView: TextView = findViewById(R.id.bmiResultTextView)

        // Очистить поля перед обновлением данных
        clearFields()

        if (user != null) {
            // Показываем поля для расчета
            weightEditText.visibility = EditText.VISIBLE
            heightEditText.visibility = EditText.VISIBLE
            ageEditText.visibility = EditText.VISIBLE
            genderSpinner.visibility = Spinner.VISIBLE
            activitySpinner.visibility = Spinner.VISIBLE
            resultSpinner.visibility = Spinner.VISIBLE
            calculateButton.visibility = Button.VISIBLE
            bmiResultTextView.visibility = TextView.VISIBLE
            caloriesResultTextView.visibility = TextView.VISIBLE

            // Прячем поля для входа и регистрации
            emailEditText.visibility = EditText.GONE
            passwordEditText.visibility = EditText.GONE
            usernameEditText.visibility = EditText.GONE
            actionButton.visibility = Button.GONE
            switchTextView.visibility = TextView.GONE
            welcomeTextView.visibility = TextView.VISIBLE
            logoutButton.visibility = Button.VISIBLE

            welcomeTextView.text = "Добро пожаловать, ${user.displayName ?: user.email}!"

            // Загрузите данные нового пользователя из Firestore
            loadUserDataFromFirestore()

        } else {
            // Показываем только поля для входа и регистрации
            emailEditText.visibility = EditText.VISIBLE
            passwordEditText.visibility = EditText.VISIBLE
            actionButton.visibility = Button.VISIBLE
            switchTextView.visibility = TextView.VISIBLE
            welcomeTextView.visibility = TextView.GONE
            logoutButton.visibility = Button.GONE

            // Скрываем все поля для расчета
            weightEditText.visibility = EditText.GONE
            heightEditText.visibility = EditText.GONE
            ageEditText.visibility = EditText.GONE
            genderSpinner.visibility = Spinner.GONE
            activitySpinner.visibility = Spinner.GONE
            resultSpinner.visibility = Spinner.GONE
            calculateButton.visibility = Button.GONE
            bmiResultTextView.visibility = TextView.GONE
            caloriesResultTextView.visibility = TextView.GONE
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

    // Рассчитываем норму калорий
    private fun calculateCalories(
        weight: Float,
        height: Float,
        age: Int,
        gender: String,
        activity: String,
        result: String
    ): Int {
        // Пример расчета для женского пола
        val bmr = if (gender == "Женский") {
            10 * weight + 6.25 * height - 5 * age - 161
        } else {
            10 * weight + 6.25 * height - 5 * age + 5
        }

        // Уровень активности
        val activityFactor = when (activity) {
            "Отсутствие физической активности" -> 1.2
            "Легкая активность" -> 1.375
            "Средняя активность" -> 1.55
            "Высокая активность" -> 1.725
            "Очень высокая активность" -> 1.9
            else -> 1.2
        }

        val tdee = bmr * activityFactor

        // Желаемый результат
        return when (result) {
            "Быстрое похудение" -> (tdee - 500).toInt()
            "Умеренное похудение" -> (tdee - 300).toInt()
            "Поддержание веса" -> tdee.toInt()
            "Набор массы" -> (tdee + 300).toInt()
            else -> tdee.toInt()
        }
    }


    // Функция для расчета ИМТ
    private fun calculateBMI(weight: Float, height: Float): Float {
        val heightInMeters = height / 100
        return weight / (heightInMeters * heightInMeters)
    }

    // Функция для расчета минимального веса для набора
    private fun calculateWeightToGain(bmi: Float, weight: Float, height: Float): Float {
        val targetBmi = 18.5f
        val targetWeight = targetBmi * (height / 100) * (height / 100)
        return targetWeight - weight
    }

    // Функция для расчета минимального веса для сброса
    private fun calculateWeightToLose(bmi: Float, weight: Float, height: Float): Float {
        val targetBmi = 24.9f
        val targetWeight = targetBmi * (height / 100) * (height / 100)
        return weight - targetWeight
    }

    //Скрывает все поля на старте, прежде чем будет выполнена проверка авторизации
    private fun hideAllFields() {
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val actionButton: Button = findViewById(R.id.actionButton)
        val switchTextView: TextView = findViewById(R.id.switchTextView)
        val welcomeTextView: TextView = findViewById(R.id.welcomeTextView)
        val logoutButton: Button = findViewById(R.id.logout_button)
        val weightEditText: EditText = findViewById(R.id.weightEditText)
        val heightEditText: EditText = findViewById(R.id.heightEditText)
        val ageEditText: EditText = findViewById(R.id.ageEditText)
        val genderSpinner: Spinner = findViewById(R.id.genderSpinner)
        val activitySpinner: Spinner = findViewById(R.id.activitySpinner)
        val resultSpinner: Spinner = findViewById(R.id.resultSpinner)
        val calculateButton: Button = findViewById(R.id.calculateButton)
        val caloriesResultTextView: TextView = findViewById(R.id.caloriesResultTextView)
        val bmiResultTextView: TextView = findViewById(R.id.bmiResultTextView)

        emailEditText.visibility = EditText.GONE
        passwordEditText.visibility = EditText.GONE
        usernameEditText.visibility = EditText.GONE
        actionButton.visibility = Button.GONE
        switchTextView.visibility = TextView.GONE
        welcomeTextView.visibility = TextView.GONE
        logoutButton.visibility = Button.GONE

        // Скрыть поля для расчета ИМТ и калорий
        weightEditText.visibility = EditText.GONE
        heightEditText.visibility = EditText.GONE
        ageEditText.visibility = EditText.GONE
        genderSpinner.visibility = Spinner.GONE
        activitySpinner.visibility = Spinner.GONE
        resultSpinner.visibility = Spinner.GONE
        calculateButton.visibility = Button.GONE
        bmiResultTextView.visibility = TextView.GONE
        caloriesResultTextView.visibility = TextView.GONE
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
    }



}



