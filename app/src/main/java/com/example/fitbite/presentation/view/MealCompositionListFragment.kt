package com.example.fitbite.presentation.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.R
import com.example.fitbite.data.model.MealCompositionResponse
import com.example.fitbite.data.network.RetrofitInstance
import com.example.fitbite.data.storage.SessionManager
import com.example.fitbite.domain.usecase.sensor.calc.WaterViewModel
import com.example.fitbite.presentation.adapter.MealCompositionAdapter
import com.example.fitbite.presentation.viewmodel.DailySummaryViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MealCompositionListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MealCompositionAdapter
    private lateinit var caloriesTextView: TextView
    private lateinit var mealTypeSwitch: Switch
    private lateinit var waterViewModel: WaterViewModel


    private var reportId: Int = -1
    private var mealType: String = "Все"
    private val sessionManager: SessionManager by lazy { SessionManager(requireContext()) }
    private var allMealCompositions = listOf<MealCompositionResponse>()
    private val dailySummaryViewModel by activityViewModels<DailySummaryViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            reportId = it.getInt("report_id", -1)
            val rawType = it.getString("meal_type") ?: "Все"
            mealType = translateMealTypeToRussian(rawType)
        }
        Log.d(TAG, "reportId received: $reportId, mealType: $mealType")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_meal_composition, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Правильная инициализация здесь один раз
        waterViewModel = ViewModelProvider(requireActivity()).get(WaterViewModel::class.java)

        caloriesTextView = view.findViewById(R.id.totalCaloriesText)
        recyclerView = view.findViewById(R.id.meal_compositionRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = MealCompositionAdapter(::removeMealComposition)
        recyclerView.adapter = adapter

        mealTypeSwitch = view.findViewById(R.id.mealTypeSwitch)
        mealTypeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) showAllMeals() else showFilteredMeals()
        }

        if (reportId != -1) fetchMealComposition()
        else Log.e(TAG, "Invalid reportId: $reportId")

        parentFragmentManager.setFragmentResultListener(
            RESULT_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean("shouldRefresh", false)) fetchMealComposition()
        }
    }

    private fun fetchMealComposition() {
        val token = sessionManager.fetchAuthToken().orEmpty()
        if (token.isBlank()) {
            Log.e(TAG, "Token is null or blank")
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getMealComposition("Bearer $token", reportId)
                if (response.isSuccessful) {
                    allMealCompositions = response.body().orEmpty().map { comp ->
                        comp.copy(meal_type = translateMealTypeToRussian(comp.meal_type))
                    }
                    if (mealTypeSwitch.isChecked) showAllMeals() else showFilteredMeals()

                    // ВАЖНО: обновляем воду после загрузки списка
                    if (reportId != -1) {
                        Log.d(TAG, "fetchMealComposition завершено, обновляем воду reportId=$reportId")
                        waterViewModel.refreshWater(reportId)
                    }
                } else {
                    Log.e(TAG, "Error loading compositions: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error: ${e.message}")
            }
        }
    }

    private fun showFilteredMeals() {
        val filtered = allMealCompositions.filter { it.meal_type.equals(mealType, ignoreCase = true) }
        adapter.submitList(filtered)
        updateCaloriesText(filtered)
    }

    private fun showAllMeals() {
        adapter.submitList(allMealCompositions)
        updateCaloriesText(allMealCompositions)
    }

    private fun updateCaloriesText(list: List<MealCompositionResponse>) {
        val totalCalories = list.sumOf { comp ->
            when {
                comp.product != null -> {
                    val product = comp.product
                    val factor = when (product.metric) {
                        "g", "ml" -> comp.portion_size / 100.0
                        "pcs" -> comp.portion_size
                        else -> comp.portion_size
                    }
                    (product.calories ?: 0.0) * factor
                }
                comp.recipe != null -> {
                    // Убедись, что приходят калории за 1 порцию и только потом умножай
                    (comp.calories ?: 0.0) * comp.portion_size
                }
                else -> 0.0
            }
        }
        caloriesTextView.text = "Употреблено: ${totalCalories.roundToInt()} ккал"
    }


    private fun removeMealComposition(item: MealCompositionResponse) {
        val token = sessionManager.fetchAuthToken().orEmpty()
        if (token.isBlank()) {
            Log.e(TAG, "Token is null or blank")
            return
        }

        lifecycleScope.launch {
            try {
                val resp = RetrofitInstance.api.removeMealComposition("Bearer $token", item.id)
                if (resp.isSuccessful) {
                    fetchMealComposition() // обновляем список и калории локально

                    // Обновляем summary (остаток калорий)
                    dailySummaryViewModel.refreshSummary(reportId)

                    // Обновляем воду для надежности
                    if (reportId != -1) {
                        Log.d(TAG, "removeMealComposition завершено, обновляем воду reportId=$reportId")
                        waterViewModel.refreshWater(reportId)
                    }
                } else {
                    Log.e(TAG, "Remove error: ${resp.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "MealCompositionFrag"
        private const val RESULT_KEY = "refreshMealComposition"

        fun newInstance(reportId: Int, mealType: String) = MealCompositionListFragment().apply {
            arguments = Bundle().apply {
                putInt("report_id", reportId)
                putString("meal_type", mealType)
            }
        }

        private fun translateMealTypeToRussian(mealType: String) = when (mealType.lowercase()) {
            "breakfast" -> "Завтрак"
            "lunch" -> "Обед"
            "dinner" -> "Ужин"
            "snack" -> "Перекус"
            else -> mealType
        }
    }
}

