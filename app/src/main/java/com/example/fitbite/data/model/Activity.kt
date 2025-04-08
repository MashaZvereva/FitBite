package com.example.fitbite.data.model

import com.google.gson.annotations.SerializedName

data class Activity(
    @SerializedName("id") val id: Int,
    @SerializedName("name_activity") val nameActivity: String?,  // Исправлено имя поля
    @SerializedName("calories_burned") val caloriesBurned: Double  // Исправлено имя поля
)

