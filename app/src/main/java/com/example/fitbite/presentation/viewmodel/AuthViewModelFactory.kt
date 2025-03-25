package com.example.fitbite.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth

//class AuthViewModelFactory(
//    private val saveUserDataUseCase: SaveUserDataUseCase,
//    private val getUserDataUseCase: GetUserDataUseCase,
//    private val auth: FirebaseAuth
//) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        return if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
//            AuthViewModel(saveUserDataUseCase, getUserDataUseCase, auth) as T
//        } else {
//            throw IllegalArgumentException("Unknown ViewModel class")
//        }
//    }
//}
//