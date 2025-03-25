package com.example.fitbite.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.fitbite.data.model.*
import com.example.fitbite.data.network.RetrofitInstance
import com.example.fitbite.utils.dataStore
import kotlinx.coroutines.flow.first

class AuthRepository(private val context: Context) {

    suspend fun login(username: String, password: String): String? {
        return try {
            val request = LoginRequest(username, password)
            Log.d("AuthRepository", "Login request: $request")
            val response = RetrofitInstance.api.login(request)
            Log.d("AuthRepository", "Login response: $response")
            saveToken(response.access)
            response.access
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login failed: ${e.message}")
            null
        }
    }

    suspend fun register(username: String, email: String, password: String): String? {
        return try {
            val request = RegisterRequest(username, email, password)
            Log.d("AuthRepository", "Register request: $request")
            val response = RetrofitInstance.api.register(request)
            Log.d("AuthRepository", "Register response: $response")
            saveToken(response.access)
            response.access
        } catch (e: Exception) {
            Log.e("AuthRepository", "Register failed: ${e.message}")
            null
        }
    }


    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey("access_token")] = token
            Log.d("AuthRepository", "Token saved: $token")
        }
    }

    suspend fun getToken(): String? {
        val prefs = context.dataStore.data.first()
        val token = prefs[stringPreferencesKey("access_token")]
        Log.d("AuthRepository", "Token fetched: $token")
        return token
    }
}
