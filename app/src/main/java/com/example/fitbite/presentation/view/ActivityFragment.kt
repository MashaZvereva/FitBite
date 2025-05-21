package com.example.fitbite.presentation.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.R
import com.example.fitbite.data.repository.ActivityRepository
import com.example.fitbite.data.storage.SessionManager
import com.example.fitbite.presentation.adapter.ActivityAdapter
import com.example.fitbite.presentation.viewmodel.DailySummaryViewModel
import kotlinx.coroutines.launch
import kotlin.math.round

class ActivityFragment : Fragment() {

    private lateinit var adapter: ActivityAdapter
    private val repo = ActivityRepository()
    private var reportId: Int = -1
    private val dailySummaryViewModel by activityViewModels<DailySummaryViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Получаем reportId из аргументов
        reportId = arguments?.getInt("report_id") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Инфлейтим обычный фрагмент
        return inflater.inflate(R.layout.fragment_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настраиваем RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.activityRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Инициализируем адаптер с коллбэком на добавление
        adapter = ActivityAdapter(emptyList()) { activityId, mins ->
            addActivity(activityId, mins)
        }
        recyclerView.adapter = adapter

        // Загружаем список активностей
        loadActivities()
    }

    private fun loadActivities() {
        lifecycleScope.launch {
            val activities = repo.getActivities()
            if (activities != null) {
                adapter.updateData(activities)
            } else {
                Toast.makeText(requireContext(), "Ошибка загрузки активностей", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addActivity(activityId: Int, minutes: Int) {
        val token = SessionManager(requireContext()).fetchAuthToken() ?: return

        lifecycleScope.launch {
            val act = repo.getActivity(activityId) ?: return@launch

            // килокалорий в минуту
            val calPerMin = act.caloriesBurned / 60.0

            // умножаем на минуты и сразу округляем до 2 знаков
            val raw = calPerMin * minutes
            val totalCalories = (round(raw * 100) / 100.0)

            Log.d("Activity", "sending calories_burned=$totalCalories for minutes=$minutes")

            val success = repo.addDailyActivity(
                token           = token,
                reportId        = reportId,
                activityId      = activityId,
                durationMinutes = minutes,
                caloriesBurned  = totalCalories
            )

            if (success) {
                Toast.makeText(requireContext(), "Активность добавлена", Toast.LENGTH_SHORT).show()

                // Сообщаем через FragmentResultListener
                parentFragmentManager.setFragmentResult("refresh_daily_activity", Bundle().apply {
                    putBoolean("shouldRefresh", true)
                })
                dailySummaryViewModel.refreshSummary(reportId)
            } else {
                Toast.makeText(requireContext(), "Ошибка при добавлении", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        /** Создание фрагмента с передачей reportId */
        fun newInstance(reportId: Int): ActivityFragment {
            return ActivityFragment().apply {
                arguments = Bundle().apply {
                    putInt("report_id", reportId)
                }
            }
        }
    }
}
