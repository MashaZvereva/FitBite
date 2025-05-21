package com.example.fitbite.presentation.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fitbite.R
import com.example.fitbite.presentation.adapter.MealPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2

class MealFragment : Fragment() {


    private var mealType: String? = null
    private var reportId: Int = -1

    companion object {
        private const val ARG_MEAL_TYPE = "meal_type"
        private const val ARG_REPORT_ID = "report_id"

        fun newInstance(mealType: String, reportId: Int): MealFragment {
            val fragment = MealFragment()
            val args = Bundle()
            args.putString(ARG_MEAL_TYPE, mealType)
            args.putInt(ARG_REPORT_ID, reportId) // Здесь передаем Int
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mealType = it.getString(ARG_MEAL_TYPE)
            reportId = it.getInt(ARG_REPORT_ID)
            Log.d("MealFragment", "Получен mealType: $mealType, reportId: $reportId")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_meal, container, false)

}
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)

        val adapter = MealPagerAdapter(this, reportId, mealType ?: "Неизвестно")
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Продукты"
                1 -> "Рецепты"
                2 -> "Прием пищи"
                else -> "Tab ${position + 1}"
            }
        }.attach()
    }
}


