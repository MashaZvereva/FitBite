package com.example.fitbite.presentation.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitbite.domain.usecase.SaveUserDataUseCase
import com.example.fitbite.data.model.UserData
import com.example.fitbite.domain.usecase.GetUserDataUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch

class AuthViewModel(
    private val saveUserDataUseCase: SaveUserDataUseCase,
    private val getUserDataUseCase: GetUserDataUseCase,
    private val auth: FirebaseAuth // Добавляем FirebaseAuth
) : ViewModel() {

    fun saveUserData(userData: UserData) {
        viewModelScope.launch {
            saveUserDataUseCase.execute(userData)
        }
    }

    fun getUserData(callback: (UserData?) -> Unit) {
        viewModelScope.launch {
            val userData = getUserDataUseCase.execute()
            callback(userData)
        }
    }

    fun registerUser(email: String, password: String, username: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build()

                        it.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileUpdateTask ->
                                if (profileUpdateTask.isSuccessful) {
                                    onSuccess() // Успешная регистрация
                                } else {
                                    onFailure("Ошибка обновления профиля: ${profileUpdateTask.exception?.message}")
                                }
                            }
                    }
                } else {
                    onFailure("Регистрация не удалась: ${task.exception?.message}")
                }
            }
    }


    fun loginUser(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess() // Успешный вход
                } else {
                    onFailure("Вход не удался: ${task.exception?.message}")
                }
            }
    }
}