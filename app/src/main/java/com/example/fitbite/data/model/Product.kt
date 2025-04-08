package com.example.fitbite.data.model

data class Product(
    val id: Int,
    val name: String,
    val calories: Double?,
    val proteins: Double?,
    val fats: Double?,
    val carbohydrates: Double?,
    val water_content: Double?,
    val metric: String
)
