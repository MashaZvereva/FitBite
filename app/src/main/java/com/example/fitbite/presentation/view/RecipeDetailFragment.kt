package com.example.fitbite.presentation.view

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.fitbite.R
import com.example.fitbite.data.model.Recipe

class RecipeDetailFragment : DialogFragment() {

    companion object {
        private const val ARG_RECIPE = "arg_recipe"

        fun newInstance(recipe: Recipe): RecipeDetailFragment {
            val fragment = RecipeDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_RECIPE, recipe)
            fragment.arguments = args
            return fragment
        }
    }

    private var recipe: Recipe? = null
    private var portionCount = 1

    private lateinit var ingredientsContainer: ViewGroup
    private lateinit var caloriesTextView: TextView
    private lateinit var portionCountText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
        recipe = arguments?.getParcelable(ARG_RECIPE)


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
    ): View {
        return inflater.inflate(R.layout.fragment_recipe_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = view.findViewById<TextView>(R.id.recipeTitle)
        val description = view.findViewById<TextView>(R.id.recipeDescription)
        val instruction = view.findViewById<TextView>(R.id.recipeInstruction)
        val cookingTime = view.findViewById<TextView>(R.id.recipeCookingTime)
        val imageView = view.findViewById<ImageView>(R.id.recipeImage)


        caloriesTextView = view.findViewById(R.id.recipeCalories)
        ingredientsContainer = view.findViewById(R.id.ingredientsContainer)
        portionCountText = view.findViewById(R.id.portionCountText)
        val btnIncrease = view.findViewById<Button>(R.id.btnIncreasePortion)
        val btnDecrease = view.findViewById<Button>(R.id.btnDecreasePortion)

        recipe?.let {
            title.text = it.name
            description.text = it.description ?: "Описание отсутствует"
            instruction.text = it.instruction ?: "Инструкция отсутствует"
            cookingTime.text = "Время приготовления: ${it.cooking_time ?: 0} мин"

            Glide.with(requireContext())
                .load(it.imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(imageView)

            // Проверяем, есть ли продукты
            it.products?.forEach { product ->
                val textView = TextView(requireContext())
                textView.text = "${product.product_name}: ${product.amount} ${product.metric}"
                textView.textSize = 16f
                textView.setPadding(0, 8, 0, 8)
                ingredientsContainer.addView(textView)
            }

            updateUI()

            btnIncrease.setOnClickListener {
                portionCount++
                updateUI()
            }

            btnDecrease.setOnClickListener {
                if (portionCount > 1) {
                    portionCount--
                    updateUI()
                }
            }
        }
    }

    private fun updateUI() {
        recipe?.let { r ->
            portionCountText.text = "$portionCount порц${getPortionEnding(portionCount)}"

            // Обновляем калории
            val totalCalories = (r.calories ?: 0.0) * portionCount
            caloriesTextView.text = "Калорийность: %.1f ккал".format(totalCalories)

            // Обновляем ингредиенты
            ingredientsContainer.removeAllViews()
            r.products?.forEach { product ->
                val textView = TextView(requireContext())
                val totalAmount = product.amount * portionCount
                val translatedMetric = translateMetricToRussian(product.metric) // используем перевод
                textView.text = "${product.product_name}: $totalAmount $translatedMetric"
                textView.textSize = 16f
                textView.setPadding(0, 8, 0, 8)
                ingredientsContainer.addView(textView)
            }
        }
    }

    private fun getPortionEnding(count: Int): String {
        return when {
            count % 10 == 1 && count % 100 != 11 -> "ия"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "ии"
            else -> "ий"
        }
    }

    fun translateMetricToRussian(metric: String): String {
        return when (metric) {
            "pcs" -> "шт"
            "g" -> "г"
            "ml" -> "мл"
            else -> metric
        }
    }
}

