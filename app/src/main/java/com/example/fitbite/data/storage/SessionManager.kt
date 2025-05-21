package com.example.fitbite.data.storage

import android.content.Context
import android.util.Base64
import org.json.JSONObject

class SessionManager(private val context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    // Сохранение токена
    fun saveAuthToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString("auth_token", token)
        editor.apply()
    }

    // Получение токена
    fun fetchAuthToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    // Очистка токена
    fun clearAuthToken() {
        val editor = sharedPreferences.edit()
        editor.remove("auth_token")
        editor.apply()
    }

    // Извлечение user_id из токена JWT
    fun getUserIdFromToken(): Int? {
        val token = fetchAuthToken() ?: return null

        return try {
            // Разделяем токен на части
            val parts = token.split(".")
            if (parts.size < 2) return null

            // Декодируем payload токена из Base64
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))

            // Извлекаем user_id из JSON
            val json = JSONObject(payload)
            json.getInt("user_id")  // Предположим, что в payload есть поле user_id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fetchCurrentReportId(): Int? {
        val idStr = sharedPreferences.getString("current_report_id", null)
        return idStr?.toIntOrNull()
    }

    fun saveCurrentReportId(id: Int) {
        sharedPreferences.edit().putString("current_report_id", id.toString()).apply()
    }
}
