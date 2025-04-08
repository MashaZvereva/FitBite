package com.example.fitbite.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Модель для рецепта
@Parcelize
data class Recipe(
    val id: Int,
    val name: String,
    val description: String?,
    val instruction: String?,
    val cooking_time: Int?,
    val calories: Double?,
    val image: String?, // Переименовали для хранения относительного пути
    val products: List<RecipeProduct>?
) : Parcelable {
    val imageUrl: String?
        get() = image?.let { "http://10.0.2.2:8000/api$it" } // Формируем полный URL
}

// Модель для продуктов, связанных с рецептом
@Parcelize
data class RecipeProduct(
    val id: Int,
    val recipeId: Int, // внешний ключ на рецепт
    val product_name: String,
    val amount: Int,
    val metric: String
) : Parcelable
