package com.example.fitbite.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.databinding.ItemMealCompositionBinding
import com.example.fitbite.data.model.MealCompositionResponse
import com.example.fitbite.data.model.Product
import com.example.fitbite.utils.translateMealTypeToRussian
import com.example.fitbite.utils.translateMetricToRussian
import kotlin.math.roundToInt

class MealCompositionAdapter(
    private val onRemoveClick: (MealCompositionResponse) -> Unit
) : RecyclerView.Adapter<MealCompositionAdapter.MealCompositionViewHolder>() {

    private var mealCompositions: MutableList<MealCompositionResponse> = mutableListOf()

    fun submitList(newList: List<MealCompositionResponse>) {
        mealCompositions.clear()
        mealCompositions.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealCompositionViewHolder {
        val binding = ItemMealCompositionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealCompositionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealCompositionViewHolder, position: Int) {
        holder.bind(mealCompositions[position])
    }

    override fun getItemCount(): Int = mealCompositions.size

    private fun formatPortions(size: Double): String {
        val count = size.roundToInt()
        val word = when {
            count % 10 == 1 && count % 100 != 11 -> "порция"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "порции"
            else -> "порций"
        }
        return "$count $word"
    }

    private fun calcProductNutrition(product: Product, quantity: Double) =
        with(product) {
            val factor = when (metric) {
                "g", "ml" -> quantity / 100.0
                "pcs" -> quantity
                else -> quantity
            }
            Nutrition(
                calories = (calories ?: 0.0) * factor,
                proteins = (proteins ?: 0.0) * factor,
                fats = (fats ?: 0.0) * factor,
                carbs = (carbohydrates ?: 0.0) * factor
            )
        }

    private data class Nutrition(
        val calories: Double,
        val proteins: Double,
        val fats: Double,
        val carbs: Double
    )

    inner class MealCompositionViewHolder(
        private val binding: ItemMealCompositionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mealComposition: MealCompositionResponse) {
            binding.mealType.text = translateMealTypeToRussian(mealComposition.meal_type)
            if (mealComposition.product != null) {
                // Рассчёт макросов для продукта
                val nutrition = calcProductNutrition(
                    mealComposition.product,
                    mealComposition.portion_size
                )
                binding.calories.text = "${nutrition.calories.roundToInt()} ккал"
                binding.proteins.text = "Б: ${"%.1f".format(nutrition.proteins)} г"
                binding.fats.text = "Ж: ${"%.1f".format(nutrition.fats)} г"
                binding.carbs.text = "У: ${"%.1f".format(nutrition.carbs)} г"

                binding.itemName.text = mealComposition.product.name
                val count = mealComposition.portion_size.roundToInt()
                val unit = translateMetricToRussian(mealComposition.product.metric)
                binding.portionSize.text = "$count $unit"

            } else if (mealComposition.recipe != null) {
                // Макросы для рецепта приходят из API (на 1 порцию)
                binding.calories.text = "${mealComposition.calories.roundToInt()} ккал"
                binding.proteins.text = "Б: ${"%.1f".format(mealComposition.protein)} г"
                binding.fats.text = "Ж: ${"%.1f".format(mealComposition.fat)} г"
                binding.carbs.text = "У: ${"%.1f".format(mealComposition.carbs)} г"

                binding.itemName.text = mealComposition.recipe.name
                binding.portionSize.text = formatPortions(mealComposition.portion_size)
            }

            binding.removeButton.setOnClickListener {
                onRemoveClick(mealComposition)
            }
        }
    }
}