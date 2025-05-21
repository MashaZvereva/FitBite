package com.example.fitbite.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fitbite.data.model.DailySummaryResponse
import com.example.fitbite.data.network.RetrofitInstance
import com.example.fitbite.data.storage.SessionManager
import kotlinx.coroutines.launch

class DailySummaryViewModel(application: Application) : AndroidViewModel(application) {

    private val _summaryLiveData = MutableLiveData<DailySummaryResponse>()
    val summaryLiveData: LiveData<DailySummaryResponse> = _summaryLiveData

    private val sessionManager = SessionManager(application)

    fun refreshSummary(reportId: Int) {
        val token = sessionManager.fetchAuthToken().orEmpty()
        if (token.isBlank()) return

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getDailySummary("Bearer $token", reportId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _summaryLiveData.postValue(it)
                    }
                }
            } catch (e: Exception) {
                Log.e("DailySummaryViewModel", "Ошибка запроса: ${e.message}")
            }
        }
    }
}
