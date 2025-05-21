package com.example.fitbite.domain.usecase.sensor.calc

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fitbite.data.network.RetrofitInstance
import com.example.fitbite.data.storage.SessionManager
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class WaterViewModel(application: Application) : AndroidViewModel(application) {

    private val _waterLiveData = MutableLiveData<Int>()
    val waterLiveData: LiveData<Int> = _waterLiveData

    private val sessionManager = SessionManager(application)

    fun refreshWater(reportId: Int) {
        val token = sessionManager.fetchAuthToken().orEmpty()
        if (token.isBlank()) {
            Log.e("WaterViewModel", "Token is blank — отмена refreshWater")
            return
        }

        if (reportId <= 0) {
            Log.e("WaterViewModel", "reportId некорректный ($reportId) — отмена refreshWater")
            return
        }

        Log.d("WaterViewModel", "refreshWater: Загружаем для reportId=$reportId")

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getDailyWater("Bearer $token", reportId)
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("WaterViewModel", "Ответ сервера: $body")

                    val waterMl = response.body()?.get("water_ml")?.toString()?.toDoubleOrNull()?.roundToInt() ?: 0
                    Log.d("WaterViewModel", "Обновляем LiveData с водой: $waterMl мл")
                    _waterLiveData.postValue(waterMl)
                } else {
                    Log.e("WaterViewModel", "Ошибка ответа: ${response.code()} - ${response.errorBody()?.string()}")
                    _waterLiveData.postValue(0)
                }
            } catch (e: Exception) {
                Log.e("WaterViewModel", "Ошибка сети: ${e.message}")
                _waterLiveData.postValue(0)
            }
        }
    }

}
