package com.example.fitbite.presentation.view

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.R
import com.example.fitbite.data.repository.ActivityRepository
import com.example.fitbite.presentation.adapter.ActivityAdapter
import kotlinx.coroutines.launch

class ActivityDialogFragment : DialogFragment() {

    private lateinit var activityAdapter: ActivityAdapter
    private val activityRepository = ActivityRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Применяем текущую тему в зависимости от глобальной настройки
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // Темная тема
            setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        } else {
            // Светлая тема
            setStyle(STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar_Fullscreen)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflating layout для фрагмента
        return inflater.inflate(R.layout.fragment_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.activityRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        activityAdapter = ActivityAdapter(emptyList())
        recyclerView.adapter = activityAdapter

        loadActivities()
    }

    private fun loadActivities() {
        lifecycleScope.launch {
            val activities = activityRepository.getActivities()
            if (activities != null) {
                activityAdapter.updateData(activities)  // Обновляем данные в адаптере
            } else {
                Log.e("ActivityDialogFragment", "Ошибка загрузки активностей")
            }
        }
    }

    // Этот метод позволяет фрагменту выглядеть как диалоговое окно
    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }
}
