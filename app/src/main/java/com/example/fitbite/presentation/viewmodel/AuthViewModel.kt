package com.example.fitbite.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitbite.domain.usecase.SaveUserDataUseCase
import com.example.fitbite.data.model.UserData
import com.example.fitbite.domain.model.GetUserDataUseCase
import kotlinx.coroutines.launch

class AuthViewModel(
    private val saveUserDataUseCase: SaveUserDataUseCase,
    private val getUserDataUseCase: GetUserDataUseCase
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
}