// ActivityRepository.kt
package com.example.fitbite.data.repository

import android.util.Log
import com.example.fitbite.data.model.Activity
import com.example.fitbite.data.network.RetrofitInstance

class ActivityRepository {

    suspend fun getActivities(): List<Activity>? {
        return try {
            val response = RetrofitInstance.api.getActivities()
            Log.d("ActivityRepository", "Activities fetched: $response")
            response
        } catch (e: Exception) {
            Log.e("ActivityRepository", "Ошибка загрузки активностей: ${e.message}")
            null
        }
    }
}

