package com.example.fitbite.data.repository

import android.util.Log
import com.example.fitbite.data.model.Product
import com.example.fitbite.data.network.RetrofitInstance

class ProductRepository{

    suspend fun getProducts(): List<Product>? {
        return try {
            val response = RetrofitInstance.api.getProduct()
            Log.d("ProductRepository", "Productds fetched: $response")
            response
        } catch (e: Exception) {
            Log.e("ProductRepository", "Ошибка загрузки продуктов: ${e.message}")
            null
        }
    }
}

