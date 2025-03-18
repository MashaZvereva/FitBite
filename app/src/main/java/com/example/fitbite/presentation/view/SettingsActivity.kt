package com.example.fitbite.presentation.view

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.fitbite.R
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val currentUserId = getCurrentUserId() // Получаем ID текущего пользователя

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE)

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

        //Обработка нажатия на кнопку
        logoutButton.setOnClickListener {
            // Завершаем текущую активность (выход)
            FirebaseAuth.getInstance().signOut() // Выход из Firebase

            // Переход на экран аутентификации
            navigateToAuthActivity()
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
        return FirebaseAuth.getInstance().currentUser?.uid ?: "default_user"
    }

    // Переход на AuthActivity
    private fun navigateToAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }
}
