// ActivityRepository.kt
package com.example.fitbite.data.repository

import android.util.Log
import com.example.fitbite.data.model.Activity
import com.example.fitbite.data.model.DailyActivity
import com.example.fitbite.data.model.DailyActivityRequest
import com.example.fitbite.data.network.ApiService
import com.example.fitbite.data.network.RetrofitInstance
import retrofit2.HttpException

class ActivityRepository(private val api: ApiService = RetrofitInstance.api) {

    suspend fun getActivities(): List<Activity>? =
        try { api.getActivities() } catch (e: Exception) { null }

    suspend fun getDailyActivities(token: String, reportId: Int): List<DailyActivity>? =
        try { api.getDailyActivities("Bearer $token", reportId) }
        catch (e: Exception) {
            Log.e("ActivityRepo", "getDailyActivities failed", e)
            null
        }

    suspend fun addDailyActivity(
        token: String,
        reportId: Int,
        activityId: Int,
        durationMinutes: Int,
        caloriesBurned: Double
    ): Boolean {
        return try {
            api.addDailyActivity(
                auth     = "Bearer $token",
                reportId = reportId,
                request  = DailyActivityRequest(
                    activityId      = activityId,
                    durationMinutes = durationMinutes,
                    caloriesBurned  = caloriesBurned
                )
            )
            true
        } catch (e: HttpException) {
            Log.e("ActivityRepo", "addDailyActivity failed", e)
            false
        }
    }

    /** Новый метод: получить одну активность из списка по id */
    suspend fun getActivity(activityId: Int): Activity? {
        return try {
            api.getActivities().firstOrNull { it.id == activityId }
        } catch (e: Exception) {
            Log.e("ActivityRepo", "getActivity failed", e)
            null
        }
    }

    suspend fun removeDailyActivity(token: String, id: Int): Boolean {
        return try {
            val resp = api.deleteDailyActivity("Bearer $token", id)
            resp.isSuccessful
        } catch (e: Exception) {
            Log.e("ActivityRepo", "removeDailyActivity failed", e)
            false
        }
    }
}
