package com.example.fitbite.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.fitbite.presentation.view.ProductListFragment
import com.example.fitbite.presentation.view.RecipeListFragment
import androidx.fragment.app.FragmentActivity
import com.example.fitbite.presentation.view.MealCompositionListFragment

class MealPagerAdapter(
    fragment: Fragment,
    private val reportId: Int,
    private val mealType: String
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProductListFragment.newInstance(reportId, mealType) // Передаем reportId и mealType
            1 -> RecipeListFragment.newInstance(reportId, mealType) // Передаем reportId и mealType
            2 -> MealCompositionListFragment.newInstance(reportId, mealType) // Передаем reportId и mealType для MealCompositionListFragment
            else -> throw IllegalArgumentException("Неверная позиция $position")
        }
    }
}




