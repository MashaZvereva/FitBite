package com.example.fitbite.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.fitbite.presentation.view.ProductListFragment
import com.example.fitbite.presentation.view.RecipeListFragment
import androidx.fragment.app.FragmentActivity

class MealPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> RecipeListFragment()
            1 -> ProductListFragment()
            else -> throw IllegalArgumentException("Неверная позиция $position")
        }
    }
}
