package com.example.fitbite.presentation.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.R
import com.example.fitbite.data.model.DailyActivity
import com.example.fitbite.data.repository.ActivityRepository
import com.example.fitbite.data.storage.SessionManager
import com.example.fitbite.presentation.adapter.ActivityAdapter
import com.example.fitbite.presentation.adapter.DailyActivityAdapter
import com.example.fitbite.presentation.viewmodel.DailySummaryViewModel
import kotlinx.coroutines.launch

class DailyActivityFragment : Fragment() {
    private var reportId = -1
    private val dailySummaryViewModel by activityViewModels<DailySummaryViewModel>()
    private lateinit var adapter: DailyActivityAdapter
    private val repo = ActivityRepository()
    private lateinit var totalCaloriesText: TextView

    companion object {
        private const val ARG_REPORT_ID = "report_id"
        fun newInstance(reportId: Int) = DailyActivityFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_REPORT_ID, reportId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reportId = arguments?.getInt(ARG_REPORT_ID) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Здесь указываем тот layout, в котором у вас RecyclerView с id dailyActivityRecyclerView
        return inflater.inflate(R.layout.fragment_daily_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.dailyActivityRecyclerView)
        totalCaloriesText = view.findViewById(R.id.totalCaloriesText)
        rv.layoutManager = LinearLayoutManager(requireContext())

        adapter = DailyActivityAdapter(emptyList()) { item ->
            removeActivity(item)
        }
        rv.adapter = adapter

        loadHistory()

        // Подписка на обновление при добавлении
        parentFragmentManager.setFragmentResultListener(
            "refresh_daily_activity",
            viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean("shouldRefresh", false)) {
                loadHistory()
            }
        }
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            val token = SessionManager(requireContext()).fetchAuthToken() ?: return@launch
            val list = repo.getDailyActivities(token, reportId) ?: emptyList()
            adapter.updateData(list)

            // Суммируем калории и обновляем TextView
            val totalCalories = list.sumOf { it.caloriesBurned }
            totalCaloriesText.text = "Потрачено: ${totalCalories.toInt()} ккал"
        }
    }

    private fun removeActivity(item: DailyActivity) {
        val token = SessionManager(requireContext()).fetchAuthToken() ?: return
        lifecycleScope.launch {
            val success = repo.removeDailyActivity(token, item.id)
            if (success) {
                Toast.makeText(requireContext(), "Активность удалена", Toast.LENGTH_SHORT).show()
                loadHistory()  // обновляем список
                dailySummaryViewModel.refreshSummary(reportId)
            } else {
                Toast.makeText(requireContext(), "Не удалось удалить активность", Toast.LENGTH_SHORT).show()
            }
        }
    }


}

