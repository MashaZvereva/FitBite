package com.example.fitbite.data.model

data class UserData(
    val weight: Float,
    val height: Float,
    val age: Int,
    val gender: String,
    val activity: String,
    val result: String,
    val calories: Int,
    val bmi: Float
) {
    // Пустой конструктор для Firebase Firestore
    constructor() : this(0f, 0f, 0, "", "", "", 0, 0f)
}
