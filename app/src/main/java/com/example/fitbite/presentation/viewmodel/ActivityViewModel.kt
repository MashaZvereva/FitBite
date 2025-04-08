// ActivityViewModel.kt
package com.example.fitbite.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitbite.data.model.Activity
import com.example.fitbite.data.repository.ActivityRepository
import kotlinx.coroutines.launch

class ActivityViewModel : ViewModel() {

    private val activityRepository = ActivityRepository()

    private val _activities = MutableLiveData<List<Activity>>()
    val activities: LiveData<List<Activity>> get() = _activities

    fun loadActivities() {
        viewModelScope.launch {
            val activitiesList = activityRepository.getActivities() ?: emptyList()
            _activities.postValue(activitiesList)
        }
    }
}
