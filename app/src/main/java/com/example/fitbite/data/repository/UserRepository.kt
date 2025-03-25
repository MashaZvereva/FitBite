package com.example.fitbite.data.repository

import android.util.Log
import com.example.fitbite.data.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

//class UserRepository {
//
//    private val db = FirebaseFirestore.getInstance()
//    private val auth = FirebaseAuth.getInstance()
//
//    // Сохранение данных в Firestore
//    suspend fun saveUserData(userData: UserData) {
//        val userId = auth.currentUser?.uid ?: return
//
//        try {
//            // Создаем уникальный документ для каждого пользователя по его UID
//            db.collection("users").document(userId)
//                .set(userData)
//                .await() // Ждем завершения операции
//            Log.d("Firestore", "User data saved successfully!") // Логируем успешную запись
//        } catch (e: Exception) {
//            Log.e("Firestore", "Error saving user data: ${e.message}") // Логируем ошибку
//        }
//    }
//
//    // Загрузка данных пользователя
//    suspend fun getUserData(): UserData? {
//        val userId =
//            auth.currentUser?.uid ?: return null // если нет пользователя, то возвращаем null
//
//        try {
//            // Получаем данные пользователя из Firestore
//            val doc = db.collection("users").document(userId).get().await()
//            return if (doc.exists()) {
//                doc.toObject(UserData::class.java) // Преобразуем документ в объект UserData
//            } else {
//                null // Если данных нет, возвращаем null
//            }
//        } catch (e: Exception) {
//            Log.e("Firestore", "Error fetching user data: ${e.message}")
//            return null
//        }
//    }
//}
