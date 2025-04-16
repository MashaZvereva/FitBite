package com.example.fitbite.data.network

import com.example.fitbite.data.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("api/token/")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @POST("api/register/")
    suspend fun register(@Body request: RegisterRequest): TokenResponse

    @GET("api/recipes/")
    suspend fun getRecipes(): List<Recipe>

    @GET("api/products/")
    suspend fun getProduct(): List<Product>

    @GET("api/activity/")
    suspend fun getActivities(): List<Activity>

    @GET("recipes/{id}/")
    fun getRecipeById(@Path("id") recipeId: Int): Call<Recipe>

    @GET("api/parameters/")
    suspend fun getUserParameters(@Header("Authorization") token: String): Response<UserParameters>

    @PUT("api/parameters/")
    suspend fun updateUserParameters(
        @Header("Authorization") token: String,
        @Body userData: UserParameters
    ): Response<UserParameters>

   @GET("api/favorite/")
   suspend fun getFavoriteRecipes(
       @Header("Authorization") token: String
   ): Response<List<FavoriteRecipe>>

    @POST("api/recipes/{id}/favorite/")
    suspend fun addFavoriteRecipe
                (@Header("Authorization") token: String,
                 @Path("id") recipeId: Int): Response<Unit>

    @DELETE("api/recipes/{id}/favorite/")
    suspend fun removeFavoriteRecipe
                (@Header("Authorization") token: String,
                 @Path("id") recipeId: Int): Response<Unit>

    @GET("daily_report/")
    suspend fun getDailyReports(): List<DailyReport>

    @GET("meal/{report_id}/")
    suspend fun getMeals(@Path("report_id") reportId: Int): List<Meal>

    @GET("meal_composition/{meal_id}/")
    suspend fun getMealCompositions(@Path("meal_id") mealId: Int): List<MealComposition>

    @GET("daily_activity/{report_id}/")
    suspend fun getDailyActivities(@Path("report_id") reportId: Int): List<DailyActivity>
}

