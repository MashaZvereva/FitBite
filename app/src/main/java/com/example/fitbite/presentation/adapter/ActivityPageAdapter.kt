package com.example.fitbite.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.fitbite.presentation.view.ActivityFragment
import com.example.fitbite.presentation.view.DailyActivityFragment

class ActivityPageAdapter(
    fragment: Fragment,
    private val reportId: Int
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> ActivityFragment.newInstance(reportId)       // «Добавить»
        1 -> DailyActivityFragment.newInstance(reportId)  // «История»
        else -> throw IllegalArgumentException("Неверная позиция $position")
    }
}

