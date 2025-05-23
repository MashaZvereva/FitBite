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


    @GET("api/daily_report/")
    suspend fun getDailyReports(): List<DailyReport>

    @POST("api/daily_report/")
    fun createDailyReport(
        @Header("Authorization") token: String,
        @Body report: DailyReport
    ): Call<DailyReportResponse>


    @POST("api/meal_compositions/{reportId}/")
    suspend fun addMealComposition(
        @Header("Authorization") token: String,
        @Path("reportId") reportId: Int,
        @Body mealComposition: MealCompositionRequest
    ): Response<MealCompositionResponse>


    @DELETE("api/meal_composition/{id}/")
    suspend fun removeMealComposition(
        @Header("Authorization") token: String,
        @Path("id") id: Int): Response<Unit>


    @GET("api/meal_compositions/{reportId}/")
    suspend fun getMealComposition(
        @Header("Authorization") token: String,
        @Path("reportId") reportId: Int
    ): Response<List<MealCompositionResponse>>


    @GET("api/activity/")
    suspend fun getActivities(): List<Activity>

    @GET("api/daily_activities/{report_id}/")
    suspend fun getDailyActivities(
        @Header("Authorization") token: String,
        @Path("report_id") reportId: Int
    ): List<DailyActivity>

    @POST("api/daily_activities/{report_id}/")
    suspend fun addDailyActivity(
        @Header("Authorization") auth: String,
        @Path("report_id") reportId: Int,
        @Body request: DailyActivityRequest
    ): DailyActivity

    @DELETE("api/daily_activity/{id}/")
    suspend fun deleteDailyActivity(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    @GET("api/daily_report/{report_id}/water/")
    suspend fun getDailyWater(
        @Header("Authorization") token: String,
        @Path("report_id") reportId: Int
    ): Response<Map<String, Any>>

    @GET("api/daily_report/{report_id}/summary/")
    suspend fun getDailySummary(
        @Header("Authorization") token: String,
        @Path("report_id") reportId: Int
    ): Response<DailySummaryResponse>

    @DELETE("api/delete-account/")
    suspend fun deleteAccount(
        @Header("Authorization") token: String
    ): Response<Unit>

}

