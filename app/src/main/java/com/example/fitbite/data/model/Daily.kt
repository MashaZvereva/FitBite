package com.example.fitbite.data.model

data class DailyReport(
    val id: Int,
    val user: String,
    val date: String
)

data class Meal(
    val id: Int,
    val mealType: String,
    val portionSize: Double
)

data class MealComposition(
    val id: Int,
    val productName: String?,
    val recipeName: String?,
    val portionSize: Double
)

data class DailyActivity(
    val id: Int,
    val activityName: String,
    val durationMinutes: Int,
    val caloriesBurned: Double
)
