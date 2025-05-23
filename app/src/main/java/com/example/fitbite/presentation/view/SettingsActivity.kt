package com.example.fitbite.presentation.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import com.example.fitbite.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.example.fitbite.presentation.view.compose.SettingsScreenFull


class SettingsActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currentUserId: String
    private val authRepository: AuthRepository by lazy { AuthRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE)
        currentUserId = getCurrentUserId()
        val initialTheme = loadTheme(currentUserId)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            var isDarkTheme by remember { mutableStateOf(initialTheme) }

            MaterialTheme(
                colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    SettingsScreenFull(
                        isDarkTheme = isDarkTheme,
                        onThemeChange = { isDark ->
                            isDarkTheme = isDark
                            saveThemePreferenceForUser(currentUserId, isDark)
                            applyTheme(isDark)
                            recreate()
                        },
                        onLogout = {
                            CoroutineScope(Dispatchers.IO).launch {
                                authRepository.deleteToken()
                                logoutFromServer()
                                runOnUiThread { navigateToAuthActivity() }
                            }
                        },
                        onDeleteAccount = {
                            CoroutineScope(Dispatchers.IO).launch {
                                // Получаем токен через suspend-функцию
                                val token = authRepository.getToken()
                                if (token != null) {
                                    val success = authRepository.deleteAccount(token)
                                    if (success) {
                                        authRepository.deleteToken()
                                        runOnUiThread { navigateToAuthActivity() }
                                    } else {
                                        // Покажи Toast или Snackbar об ошибке
                                    }
                                } else {
                                    // Нет токена — тоже покажи ошибку
                                }
                            }
                }
                    )
                }

                    // Вот здесь SideEffect, а не внутри Surface и не как параметр
                val window = (this as Activity).window
                val color = MaterialTheme.colorScheme.background.toArgb()
                SideEffect {
                    window.statusBarColor = color
                    window.navigationBarColor = color
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.isAppearanceLightStatusBars = !isDarkTheme
                    insetsController.isAppearanceLightNavigationBars = !isDarkTheme
                }
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
        intent.putExtra("showRegister", true)
        startActivity(intent)
        finish() // Завершаем текущую активность
    }
}
