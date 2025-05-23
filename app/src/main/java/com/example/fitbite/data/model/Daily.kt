package com.example.fitbite.data.model

import com.google.gson.annotations.SerializedName

data class DailyReport(
    //val user_id: String,
    val date: String,
    val user: String
)

data class DailyReportResponse(
    val id: Int,
    val user: Int,
    val date: String
)

data class MealCompositionRequest(
    val product_id: Int?,    // ← тут product — ID продукта
    val recipe_id: Int?,       // ← если передаёшь рецепт
    val portion_size: Double,
    @SerializedName("meal_type")
    val meal_type: String,

)

data class MealCompositionResponse(
    val id: Int,
    val meal_type: String,
    val product: Product?,
    val recipe: Recipe?,
    val portion_size: Double,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)

data class DailySummaryResponse(
    @SerializedName("target_calories") val targetCalories: Int,
    @SerializedName("eaten_calories") val caloriesEaten: Int,
    @SerializedName("burned_calories") val caloriesBurned: Int,
    @SerializedName("calories_left") val caloriesLeft: Int
)





