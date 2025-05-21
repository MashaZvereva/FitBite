package com.example.fitbite.presentation.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContentProviderCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.R
import com.example.fitbite.data.model.Recipe
import com.example.fitbite.data.repository.RecipeRepository
import com.example.fitbite.presentation.adapter.RecipeAdapter
import kotlinx.coroutines.launch
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.example.fitbite.data.model.MealCompositionRequest
import com.example.fitbite.data.network.RetrofitInstance
import com.example.fitbite.data.storage.SessionManager
import com.example.fitbite.presentation.viewmodel.DailySummaryViewModel

class RecipeListFragment : Fragment() {

    private val recipeRepository = RecipeRepository() // Репозиторий для получения данных о рецептах
    private lateinit var recipeAdapter: RecipeAdapter
    private var allRecipes: List<Recipe> = emptyList()

    private val sessionManager: SessionManager by lazy { SessionManager(requireContext()) }

    private var reportId: Int = -1
    private var mealType: String = "Неизвестно"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            reportId = it.getInt(ARG_REPORT_ID, -1)
            mealType = it.getString(ARG_MEAL_TYPE) ?: "Неизвестно"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipe, container, false) // Используем новый layout для фрагмента
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recipesRecycler)
        val searchEditText = view.findViewById<EditText>(R.id.searchRecipeEditText)
        val dailySummaryViewModel = ViewModelProvider(requireActivity())[DailySummaryViewModel::class.java]
        recipeAdapter = RecipeAdapter(
            emptyList(),
            onRecipeClick = {},
            onAddRecipeToMeal = { recipe, portion ->
                Log.d("RecipeListFragment", "ID рецепта: ${recipe.id}")
                Log.d("RecipeListFragment", "Отправка: recipeId=${recipe.id}, portion=$portion, mealType=${mapMealTypeToServer(mealType)}")

                if (portion == 0) {
                    Log.e("RecipeListFragment", "Ошибка: размер порции не может быть 0.")
                    return@RecipeAdapter
                }

                val request = MealCompositionRequest(
                    product_id = null,
                    recipe_id = recipe.id,
                    portion_size = portion.toDouble(),
                    meal_type = mapMealTypeToServer(mealType)
                )

                val token = sessionManager.fetchAuthToken()
                if (token != null && reportId != -1) {
                    lifecycleScope.launch {
                        try {
                            val response = RetrofitInstance.api.addMealComposition("Bearer $token", reportId, request)
                            if (response.isSuccessful) {
                                parentFragmentManager.setFragmentResult("refreshMealComposition", Bundle().apply {
                                    putBoolean("shouldRefresh", true)
                                })
                                parentFragmentManager.popBackStack()
                                val meal = response.body()
                                Log.d("RecipeListFragment", "Рецепт добавлен: $meal")

                                // 💡 Вызовем сразу обновление сводки:
                                dailySummaryViewModel.refreshSummary(reportId)
                            } else {
                                Log.e("RecipeListFragment", "Ошибка при добавлении рецепта: ${response.errorBody()?.string()}")
                            }
                        } catch (e: Exception) {
                            Log.e("RecipeListFragment", "Ошибка сети или сервера: ${e.message}")
                        }
                    }
                } else {
                    Log.e("RecipeListFragment", "token или reportId невалидны")
                }
            },
            onRefreshSummary = { dailySummaryViewModel.refreshSummary(reportId) }, // 💡 передаем
            isRecipeListFragment = true
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = recipeAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            val recipes = recipeRepository.getRecipes()
            if (recipes != null) {
                allRecipes = recipes
                recipeAdapter.updateData(allRecipes)
            } else {
                Log.e("RecipeListFragment", "Не удалось загрузить рецепты")
            }
        }

        searchEditText.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            val filteredRecipes = allRecipes.filter { recipe ->
                recipe.name.contains(query, ignoreCase = true)
            }
            recipeAdapter.updateData(filteredRecipes)
        }
    }

    fun mapMealTypeToServer(value: String): String {
        return when (value.lowercase()) {
            "завтрак", "breakfast" -> "breakfast"
            "обед", "lunch" -> "lunch"
            "ужин", "dinner" -> "dinner"
            "перекус", "snack" -> "snack"
            else -> {
                Log.e("ProductListFragment", "Неверный тип приема пищи: $value, по умолчанию snack")
                "snack"
            }
        }
    }

    companion object {
        private const val ARG_REPORT_ID = "report_id"
        private const val ARG_MEAL_TYPE = "meal_type"

        fun newInstance(reportId: Int, mealType: String): RecipeListFragment {
            val fragment = RecipeListFragment()
            val args = Bundle()
            args.putInt(ARG_REPORT_ID, reportId)
            args.putString(ARG_MEAL_TYPE, mealType)
            fragment.arguments = args
            return fragment
        }
    }
}
