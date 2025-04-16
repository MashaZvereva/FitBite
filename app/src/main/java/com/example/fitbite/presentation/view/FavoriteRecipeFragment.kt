package com.example.fitbite.presentation.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitbite.data.model.FavoriteRecipe
import com.example.fitbite.data.repository.RecipeRepository
import com.example.fitbite.data.storage.SessionManager
import com.example.fitbite.databinding.FragmentFavoriteRecipeBinding
import com.example.fitbite.presentation.adapter.FavoriteRecipeAdapter
import kotlinx.coroutines.launch

class FavoriteRecipeFragment : Fragment() {

    private var _binding: FragmentFavoriteRecipeBinding? = null
    private val binding get() = _binding!!
    private val recipeRepository = RecipeRepository()

    private lateinit var sessionManager: SessionManager
    private var token: String? = null

    private var favoriteRecipes = mutableListOf<FavoriteRecipe>()

    private val adapter = FavoriteRecipeAdapter(
        favoriteRecipes,
        onRemoveClick = { recipe -> removeFromFavorites(recipe) },
        onRecipeClick = { recipe ->
            val recipeDialog = RecipeDetailFragment.newInstance(recipe.recipe)
            recipeDialog.show(parentFragmentManager, "recipe_detail")
        },
        isFavoriteFragment = true // <--- указываем, что мы в FavoriteFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        token = sessionManager.fetchAuthToken()

        binding.favoriteRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.favoriteRecyclerView.adapter = adapter

        if (token == null) {
            Toast.makeText(requireContext(), "Пожалуйста, войдите в аккаунт", Toast.LENGTH_SHORT).show()
            val loginIntent = Intent(requireContext(), AuthActivity::class.java)
            startActivity(loginIntent)
            return
        }

        loadFavoriteRecipes()
    }

    private fun loadFavoriteRecipes() {
        lifecycleScope.launch {
            val recipes = recipeRepository.getFavoriteRecipes(token!!)
            favoriteRecipes.clear()
            favoriteRecipes.addAll(recipes)
            adapter.notifyDataSetChanged()
        }
    }

    private fun removeFromFavorites(recipe: FavoriteRecipe) {
        lifecycleScope.launch {
            val success = recipeRepository.deleteFavoriteRecipes(recipe.recipe.id, token!!)
            if (success) {
                loadFavoriteRecipes()
            } else {
                Toast.makeText(requireContext(), "Не удалось удалить из избранного", Toast.LENGTH_SHORT).show()
                loadFavoriteRecipes()  // Перезагружаем
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
