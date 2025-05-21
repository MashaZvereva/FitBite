package com.example.fitbite.data.model

import com.google.gson.annotations.SerializedName

data class Activity(
    @SerializedName("id") val id: Int,
    @SerializedName("name_activity") val nameActivity: String?,  // Исправлено имя поля
    @SerializedName("calories_burned") val caloriesBurned: Double  // Исправлено имя поля
)

data class DailyActivityRequest(
    @SerializedName("activity_id") val activityId: Int,
    @SerializedName("duration_minutes") val durationMinutes: Int,
    @SerializedName("calories_burned") val caloriesBurned: Double
)

data class DailyActivity(
    @SerializedName("id") val id: Int,
    @SerializedName("activity_name") val activityName: String,
    @SerializedName("duration_minutes") val durationMinutes: Int,
    @SerializedName("calories_burned") val caloriesBurned: Double
)
