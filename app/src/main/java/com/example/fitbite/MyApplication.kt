package com.example.fitbite

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
//import com.google.firebase.auth.FirebaseAuth

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Получаем ID текущего пользователя

        // Загружаем тему для текущего пользователя

    }

    private fun loadUserTheme(userId: String) {
        val sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("theme_$userId", false)

        // Применяем соответствующую тему
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
