package com.example.fitbite.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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

class RecipeListFragment : Fragment() {

    private val recipeRepository = RecipeRepository() // Репозиторий для получения данных о рецептах
    private lateinit var recipeAdapter: RecipeAdapter
    private var allRecipes: List<Recipe> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipe, container, false) // Используем новый layout для фрагмента

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация RecyclerView и строки поиска
        val recyclerView = view.findViewById<RecyclerView>(R.id.recipesRecycler)
        val searchEditText = view.findViewById<EditText>(R.id.searchRecipeEditText)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recipeAdapter = RecipeAdapter(allRecipes, { recipe ->
            // Обработчик клика на рецепт
        }, isRecipeListFragment = true) // Указываем, что это не фрагмент избранных рецептов
        recyclerView.adapter = recipeAdapter

        // Загружаем данные о рецептах
        viewLifecycleOwner.lifecycleScope.launch {
            val recipes = recipeRepository.getRecipes()
            if (recipes != null) {
                allRecipes = recipes
                recipeAdapter.updateData(allRecipes)
            }
        }

        // Фильтрация по строке поиска
        searchEditText.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            val filteredRecipes = allRecipes.filter { recipe ->
                recipe.name.contains(query, ignoreCase = true)
            }
            recipeAdapter.updateData(filteredRecipes)
        }
    }
}
