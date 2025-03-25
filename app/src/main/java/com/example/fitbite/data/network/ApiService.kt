package com.example.fitbite.data.network

import com.example.fitbite.data.model.*
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/token/")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @POST("api/register/")
    suspend fun register(@Body request: RegisterRequest): TokenResponse

}
