package com.example.fitbite.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitbite.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)

    fun login(username: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val token = authRepository.login(username, password)
            if (!token.isNullOrEmpty()) {
                authRepository.saveToken(token) // Сохраняем токен
                onSuccess()
            } else {
                onError("Ошибка входа")
            }
        }
    }

    fun register(username: String, email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val token = authRepository.register(username, email, password)
            if (!token.isNullOrEmpty()) {
                authRepository.saveToken(token) // Сохраняем токен
                onSuccess()
            } else {
                onError("Ошибка регистрации")
            }
        }
    }

    fun getToken(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val token = authRepository.getToken()
            onResult(token)
        }
    }
}
