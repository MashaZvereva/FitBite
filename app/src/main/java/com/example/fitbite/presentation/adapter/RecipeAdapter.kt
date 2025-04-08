package com.example.fitbite.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitbite.R
import com.example.fitbite.data.model.Recipe

class RecipeAdapter(
    private var recipes: List<Recipe>,
    private val onRecipeClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.recipe_name)
        private val description: TextView = view.findViewById(R.id.recipe_description)
        private val calories: TextView = view.findViewById(R.id.recipe_calories)
        private val imageUrl: ImageView = view.findViewById(R.id.recipe_image)

        fun bind(recipe: Recipe) {
            name.text = recipe.name // Название рецепта
            description.text = recipe.description ?: "Описание отсутствует"
            calories.text = "Калории: ${recipe.calories ?: 0.0} ккал"

            Glide.with(itemView.context)
                .load(recipe.imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .fallback(R.drawable.placeholder)
                .into(imageUrl)

            // Обработка клика по элементу
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
