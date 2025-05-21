package com.example.fitbite.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitbite.R
import com.example.fitbite.data.model.Recipe


class RecipeAdapter(
    private var recipes: List<Recipe>,
    private val onRecipeClick: (Recipe) -> Unit,
    private val onAddRecipeToMeal: (Recipe, Int) -> Unit,
    private val onRefreshSummary: () -> Unit,  // добавлено
    private val isRecipeListFragment: Boolean = false,
    private val isFoodActivity: Boolean = false
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.recipe_name)
        private val description: TextView = view.findViewById(R.id.recipe_description)
        private val calories: TextView = view.findViewById(R.id.recipe_calories)
        private val imageUrl: ImageView = view.findViewById(R.id.recipe_image)
        val addButton: Button = view.findViewById(R.id.add_button)
        val removeFavoriteButton: Button = view.findViewById(R.id.removeFavoriteButton)
        val portionInput: EditText = view.findViewById(R.id.portion_input_recipe)

        fun bind(recipe: Recipe) {
            name.text = recipe.name
            description.text = recipe.description ?: "Описание отсутствует"
            calories.text = "Калории: ${recipe.calories ?: 0.0} ккал"

            Glide.with(itemView.context)
                .load(recipe.imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .fallback(R.drawable.placeholder)
                .into(imageUrl)

            if (isRecipeListFragment) {
                addButton.visibility = View.VISIBLE
                portionInput.visibility = View.VISIBLE
                removeFavoriteButton.visibility = View.GONE
            } else if (isFoodActivity) {
                addButton.visibility = View.GONE
                portionInput.visibility = View.GONE
                removeFavoriteButton.visibility = View.GONE
            } else {
                addButton.visibility = View.GONE
                portionInput.visibility = View.GONE
                removeFavoriteButton.visibility = View.GONE
            }

            addButton.isEnabled = false

            portionInput.addTextChangedListener {
                val portion = it.toString().toIntOrNull()
                addButton.isEnabled = portion != null && portion in 1..1000
                portionInput.error = if (portion == null || portion !in 1..1000)
                    "Введите от 1 до 1000"
                else null
            }

            addButton.setOnClickListener {
                val portion = portionInput.text.toString().toIntOrNull()
                if (portion != null && portion in 1..1000) {
                    onAddRecipeToMeal(recipe, portion)
                    onRefreshSummary()  // теперь вызываем переданную функцию
                } else {
                    portionInput.error = "Введите от 1 до 1000"
                }
            }

            itemView.setOnClickListener {
                onRecipeClick(recipe)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    fun updateData(newRecipes: List<Recipe>) {
        this.recipes = newRecipes
        notifyDataSetChanged()
    }
}

