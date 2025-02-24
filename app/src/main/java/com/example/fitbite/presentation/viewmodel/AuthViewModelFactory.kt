package com.example.fitbite.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitbite.domain.usecase.SaveUserDataUseCase
import com.example.fitbite.domain.model.GetUserDataUseCase

class AuthViewModelFactory(
    private val saveUserDataUseCase: SaveUserDataUseCase,
    private val getUserDataUseCase: GetUserDataUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Здесь создаем экземпляр ViewModel, передавая зависимости
        return AuthViewModel(saveUserDataUseCase, getUserDataUseCase) as T
    }
}
