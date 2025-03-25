package com.example.fitbite.data.model

data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(val username: String, val email: String, val password: String)
data class TokenResponse(val access: String, val refresh: String)
