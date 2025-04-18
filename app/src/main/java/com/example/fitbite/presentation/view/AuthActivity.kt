package com.example.fitbite.presentation.view

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.fitbite.R
import com.example.fitbite.data.storage.SessionManager
import com.example.fitbite.presentation.viewmodel.AuthViewModel

class AuthActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем наличие токена перед загрузкой интерфейса
        authViewModel.getToken { token ->
            if (!token.isNullOrEmpty()) {
                // Если токен найден, загружаем тему и переходим в MainActivity
                val userId = getCurrentUserId() // Получаем уникальный ID пользователя
                val isDarkMode = loadTheme(userId) // Загружаем тему
                applyTheme(isDarkMode) // Применяем тему

                // Переход в MainActivity
                navigateToMainActivity()
            } else {
                // Если токен не найден, показываем интерфейс входа/регистрации
                setContentView(R.layout.activity_auth)
                setupUI()
            }
        }
    }

    private fun setupUI() {
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val actionButton: Button = findViewById(R.id.actionButton)
        val switchTextView: TextView = findViewById(R.id.switchTextView)

        var isLoginMode = true

        actionButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            showProgressDialog()

            if (isLoginMode) {
                authViewModel.login(username, password, {
                    // Коллбэк при успешном входе
                    authViewModel.getToken { token ->
                        if (!token.isNullOrEmpty()) {
                            val sessionManager = SessionManager(this)
                            sessionManager.saveAuthToken(token)
                            hideProgressDialog()
                            navigateToMainActivity()
                        } else {
                            hideProgressDialog()
                            Toast.makeText(this, "Не удалось получить токен", Toast.LENGTH_SHORT).show()
                        }
                    }
                }, { errorMessage ->
                    // Ошибка при входе
                    hideProgressDialog()
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                })
            } else {
                val email = emailEditText.text.toString()
                authViewModel.register(username, email, password, {
                    // Коллбэк при успешной регистрации
                    authViewModel.getToken { token ->
                        if (!token.isNullOrEmpty()) {
                            val sessionManager = SessionManager(this)
                            sessionManager.saveAuthToken(token)
                            hideProgressDialog()
                            navigateToMainActivity()
                        } else {
                            hideProgressDialog()
                            Toast.makeText(this, "Не удалось получить токен", Toast.LENGTH_SHORT).show()
                        }
                    }
                }, { errorMessage ->
                    // Ошибка при регистрации
                    hideProgressDialog()
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                })
            }
        }

        switchTextView.setOnClickListener {
            isLoginMode = !isLoginMode
            emailEditText.visibility = if (isLoginMode) View.GONE else View.VISIBLE
            usernameEditText.visibility = View.VISIBLE
            actionButton.text = if (isLoginMode) "Вход" else "Регистрация"
            switchTextView.text =
                if (isLoginMode) "У Вас нет аккаунта? Зарегистрируйтесь" else "Уже есть аккаунт? Войдите"
        }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    // Функция для получения уникального ID текущего пользователя
    private fun getCurrentUserId(): String {
        // Здесь можно получить ID пользователя из токена или другого источника
        return "example_user_id" // Например, заглушка для демонстрации
    }

    // Функция для загрузки темы пользователя из SharedPreferences
    private fun loadTheme(userId: String): Boolean {
        val sharedPreferences = getSharedPreferences("user_settings", MODE_PRIVATE)
        return sharedPreferences.getBoolean(
            "theme_$userId",
            false
        ) // false - светлая тема по умолчанию
    }

    // Функция для применения темы
    private fun applyTheme(isDarkMode: Boolean) {
        val mode =
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun showProgressDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Загрузка...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Убедитесь, что все диалоги или окна закрыты
        progressDialog?.dismiss()
    }
}