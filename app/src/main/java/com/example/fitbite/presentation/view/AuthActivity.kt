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
import com.example.fitbite.data.storage.SessionManager // Добавь этот импорт
import com.example.fitbite.presentation.viewmodel.AuthViewModel

class AuthActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val showRegister = intent.getBooleanExtra("showRegister", false)

        authViewModel.getToken { token ->
            if (!token.isNullOrEmpty()) {
                val userId = getCurrentUserId()
                val isDarkMode = loadTheme(userId)
                applyTheme(isDarkMode)
                navigateToMainActivity()
            } else {
                setContentView(R.layout.activity_auth)
                setupUI(showRegister) // <-- Передаём флаг
            }
        }
    }

    private fun setupUI(startWithRegister: Boolean = false) {

        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val actionButton: Button = findViewById(R.id.actionButton)
        val switchTextView: TextView = findViewById(R.id.switchTextView)

        var isLoginMode = !startWithRegister

        fun updateUI() {
            emailEditText.visibility = if (isLoginMode) View.GONE else View.VISIBLE
            usernameEditText.visibility = View.VISIBLE
            actionButton.text = if (isLoginMode) "Вход" else "Регистрация"
            switchTextView.text =
                if (isLoginMode) "У Вас нет аккаунта? Зарегистрируйтесь" else "Уже есть аккаунт? Войдите"
        }

        updateUI()

        actionButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            showProgressDialog()

            if (isLoginMode) {
                authViewModel.login(username, password, {
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
                    hideProgressDialog()
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                })
            } else {
                val email = emailEditText.text.toString()
                authViewModel.register(username, email, password, {
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
                    hideProgressDialog()
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                })
            }
        }

        switchTextView.setOnClickListener {
            isLoginMode = !isLoginMode
            updateUI()
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

    private fun navigateToRegisterActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("showRegister", true)
        startActivity(intent)
        finish()
    }

}
