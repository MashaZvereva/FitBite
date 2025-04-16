package com.example.fitbite.data.repository

import android.util.Log
import com.example.fitbite.data.model.FavoriteRecipe
import com.example.fitbite.data.model.Recipe
import com.example.fitbite.data.network.RetrofitInstance
import retrofit2.Response

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

    suspend fun addRecipeToFavorites(token: String, recipeId: Int): Boolean {
        return try {
            val response = RetrofitInstance.api.addFavoriteRecipe("Bearer $token", recipeId)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Failed to add recipe to favorites: ${e.message}")
            false
        }
    }


    suspend fun getFavoriteRecipes(token: String): List<FavoriteRecipe> {
        return try {
            val response = RetrofitInstance.api.getFavoriteRecipes("Bearer $token")
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("RecipeRepository", "Error: ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Failed to fetch favorite recipes: ${e.message}")
            emptyList()
        }
    }


    suspend fun deleteFavoriteRecipes(recipeId: Int, token: String): Boolean {
        return try {
            val response = RetrofitInstance.api.removeFavoriteRecipe("Bearer $token", recipeId)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Failed to remove recipe from favorites: ${e.message}")
            false
        }
    }
}
