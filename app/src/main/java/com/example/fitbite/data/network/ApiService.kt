package com.example.fitbite.data.network

import com.example.fitbite.data.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
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

    @PUT("/api/parameters/")
    suspend fun updateUserParameters(
        @Header("Authorization") token: String,
        @Body userData: UserParameters
    ): Response<UserParameters>
}

