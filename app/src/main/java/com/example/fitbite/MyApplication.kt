package com.example.fitbite

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Получаем ID текущего пользователя
        val currentUserId = getCurrentUserId()

        // Загружаем тему для текущего пользователя
        loadUserTheme(currentUserId)
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

    private fun getCurrentUserId(): String {
        // Получаем текущего пользователя из Firebase
        val user = FirebaseAuth.getInstance().currentUser
        return user?.uid ?: "default_user" // Если пользователь не авторизован, используем дефолтный ID
    }
}
