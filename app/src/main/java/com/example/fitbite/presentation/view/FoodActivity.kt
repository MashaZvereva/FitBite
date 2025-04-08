package com.example.fitbite.presentation.view

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.R
import com.example.fitbite.data.model.Recipe
import com.example.fitbite.data.repository.RecipeRepository
import com.example.fitbite.presentation.adapter.RecipeAdapter
import kotlinx.coroutines.launch

class FoodActivity : AppCompatActivity() {

    private lateinit var recipeAdapter: RecipeAdapter
    private val recipeRepository = RecipeRepository()

    private var maxCalories: Double = 1000.0
    private var maxTime: Int = 60
    private var searchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food)

        val recyclerView = findViewById<RecyclerView>(R.id.recipesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // рецепт адаптор
        recipeAdapter = RecipeAdapter(emptyList()) { recipe ->
            val fragment = RecipeDetailFragment.newInstance(recipe)
            fragment.show(supportFragmentManager, "RecipeDetail")
        }
        recyclerView.adapter = recipeAdapter


        // Поиск по названию рецепта
        findViewById<EditText>(R.id.searchEditText).addTextChangedListener {
            searchQuery = it.toString()
            loadRecipes()
        }

        // Настраиваем фильтры
        setupSeekBar(R.id.caloriesSeekBar, R.id.caloriesValue, " ккал") { maxCalories = it.toDouble() }
        setupSeekBar(R.id.timeSeekBar, R.id.timeValue, " мин") { maxTime = it }

        // Загружаем рецепты после инициализации адаптера
        loadRecipes()
    }

    private fun setupSeekBar(seekBarId: Int, textViewId: Int, unit: String, updateValue: (Int) -> Unit) {
        val seekBar = findViewById<SeekBar>(seekBarId)
        val textView = findViewById<TextView>(textViewId)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textView.text = "Выбрано: $progress$unit"
                updateValue(progress)
                loadRecipes()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun loadRecipes() {
        lifecycleScope.launch {
            val recipes = recipeRepository.getRecipes()
            if (recipes != null) {
                recipeAdapter.updateData(filterRecipes(recipes))
            } else {
                Log.e("FoodActivity", "Ошибка загрузки рецептов")
            }
        }
    }

    private fun filterRecipes(recipes: List<Recipe>): List<Recipe> {
        return recipes.filter {
            val title = it.name ?: ""  // Если title равно null, заменим на пустую строку
            title.contains(searchQuery, ignoreCase = true) &&
                    (it.calories ?: 0.0) <= maxCalories &&
                    (it.cooking_time ?: 0) <= maxTime
        }
    }


}
