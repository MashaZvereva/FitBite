package com.example.fitbite.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitbite.R
import com.example.fitbite.data.model.FavoriteRecipe
import com.example.fitbite.databinding.ItemRecipeBinding

class FavoriteRecipeAdapter(
    private val favoriteRecipes: List<FavoriteRecipe>,
    private val onRemoveClick: (FavoriteRecipe) -> Unit,
    private val onRecipeClick: (FavoriteRecipe) -> Unit,
    private val isFavoriteFragment: Boolean = false // <--- Добавили флаг
) : RecyclerView.Adapter<FavoriteRecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(private val binding: ItemRecipeBinding) :
        RecyclerView.ViewHolder(binding.root) {

            val addButton = binding.addButton
            val removeFavoriteButton = binding.removeFavoriteButton


            fun bind(favoriteRecipe: FavoriteRecipe) {
            binding.recipeName.text = favoriteRecipe.recipe.name
            binding.recipeDescription.text = favoriteRecipe.recipe.description
            binding.recipeCalories.text = "Калории: ${favoriteRecipe.recipe.calories}"

            Glide.with(binding.root.context)
                .load(favoriteRecipe.recipe.imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(binding.recipeImage)

            // Устанавливаем видимость кнопок в зависимости от фрагмента
            if (isFavoriteFragment) {
                addButton.visibility = View.GONE
                removeFavoriteButton.visibility = View.VISIBLE
            } else {
                addButton.visibility = View.GONE
                removeFavoriteButton.visibility = View.GONE
            }

            binding.removeFavoriteButton.setOnClickListener {
                onRemoveClick(favoriteRecipe)
            }

            binding.root.setOnClickListener {
                onRecipeClick(favoriteRecipe)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(favoriteRecipes[position])
    }

    override fun getItemCount(): Int = favoriteRecipes.size
}

