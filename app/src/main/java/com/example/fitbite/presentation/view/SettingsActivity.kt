package com.example.fitbite.presentation.view

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.app.AlertDialog
import android.app.VoiceInteractor
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.fitbite.R
import com.example.fitbite.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currentUserId: String
    private val authRepository: AuthRepository by lazy { AuthRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Инициализация SharedPreferences
        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE)

        // Получаем ID текущего пользователя
        currentUserId = getCurrentUserId()

        val themeSwitch: Switch = findViewById(R.id.themeSwitch)
        val termsButton: Button = findViewById(R.id.termsButton)
        val policyButton: Button = findViewById(R.id.policyButton)
        val feedbackButton: Button = findViewById(R.id.feedbackButton)
        val logoutButton: Button = findViewById(R.id.logout_button)

        // Загружаем тему и применяем
        val isDarkMode = loadTheme(currentUserId)
        themeSwitch.isChecked = isDarkMode
        applyTheme(isDarkMode)

        // Обработчик переключения темы
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveThemePreferenceForUser(currentUserId, isChecked)
            applyTheme(isChecked)
        }

        // Обработчик кнопки условий использования
        termsButton.setOnClickListener { showTermsDialog() }

        // Обработчик кнопки политика конфиденциальности
        policyButton.setOnClickListener { showPolicyDialog() }

        // Обработчик кнопки отзыва разработчику
        feedbackButton.setOnClickListener { showFeedbackDialog() }

        // Обработчик кнопки выхода
        logoutButton.setOnClickListener {
            // Удаляем токен из DataStore
            CoroutineScope(Dispatchers.IO).launch {
                authRepository.deleteToken()  // Удаляем токен через AuthRepository
                logoutFromServer()  // Выход с сервера

                // Переход на экран аутентификации
                navigateToAuthActivity()
            }
        }
    }

    // Функция для применения выбранной темы
    private fun applyTheme(isDarkMode: Boolean) {
        val mode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)

    }

    // Функция для сохранения темы для текущего пользователя
    private fun saveThemePreferenceForUser(userId: String, isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean("theme_$userId", isDarkMode).apply()
    }


    // Функция для загрузки темы текущего пользователя из SharedPreferences
    private fun loadTheme(userId: String): Boolean {
        return sharedPreferences.getBoolean("theme_$userId", false) // По умолчанию светлая тема
    }

    // Функция для отображения всплывающего окна с политикой конфиденциальности
    private fun showPolicyDialog() {
        val webView = WebView(this)
        webView.loadUrl("file:///android_asset/privacy_policy.txt")

        AlertDialog.Builder(this)
            .setTitle("Политика конфиденциальности")
            .setView(webView)
            .setPositiveButton("Закрыть") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    // Функция для отображения всплывающего окна с условиями использования
    private fun showTermsDialog() {
        val webView = WebView(this)
        webView.loadUrl("file:///android_asset/terms_conditions.txt")

        AlertDialog.Builder(this)
            .setTitle("Условия использования")
            .setView(webView)
            .setPositiveButton("Закрыть") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    // Функция для отображения всплывающего окна с выбором способа связи
    private fun showFeedbackDialog() {
        val feedbackOptions = arrayOf("Telegram", "Почта")
        AlertDialog.Builder(this)
            .setTitle("Выберите способ связи")
            .setItems(feedbackOptions) { _, which ->
                when (which) {
                    0 -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/maniazvereva")))
                    1 -> startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:mashazvereva2003@gmail.com")))
                }
            }
            .create()
            .show()
    }

    // Функция для получения уникального ID текущего пользователя
    private fun getCurrentUserId(): String {
        // Возвращаем ID текущего пользователя из SharedPreferences или токен сессии, если используется
        val token = sharedPreferences.getString("auth_token", null)
        return token ?: "default_user"
    }

    // Функция для выхода с сервера (отправка запроса на сервер для выхода пользователя)
    private fun logoutFromServer() {
        val apiUrl = "http://your-django-api-url/logout/" // Замените на ваш URL для выхода
        val token = sharedPreferences.getString("auth_token", null)

        if (token != null) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer $token") // Отправка токена на сервер
                .post(RequestBody.create(null, ""))
                .build()

            client.newCall(request).execute()
        }
    }

    // Переход на AuthActivity
    private fun navigateToAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Это очистит стек активностей, чтобы не было возврата на предыдущие экраны
        startActivity(intent)
        finish() // Завершаем текущую активность
    }
}

