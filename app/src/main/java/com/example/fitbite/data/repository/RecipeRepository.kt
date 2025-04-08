package com.example.fitbite.data.repository

import android.util.Log
import com.example.fitbite.data.model.Recipe
import com.example.fitbite.data.network.RetrofitInstance

class RecipeRepository {

    suspend fun getRecipes(): List<Recipe>? {
        return try {
            val response = RetrofitInstance.api.getRecipes()
            Log.d("RecipeRepository", "Recipes fetched: $response")
            response
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Failed to fetch recipes: ${e.message}")
            null
        }
    }
}
