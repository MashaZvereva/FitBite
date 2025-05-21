package com.example.fitbite.presentation.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fitbite.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import com.example.fitbite.presentation.adapter.ActivityPageAdapter

class ActivityPageFragment : Fragment() {

    private var reportId: Int = -1

    companion object {
        private const val ARG_REPORT_ID = "report_id"
        fun newInstance(reportId: Int): ActivityPageFragment =
            ActivityPageFragment().apply {
                arguments = Bundle().apply { putInt(ARG_REPORT_ID, reportId) }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reportId = arguments?.getInt(ARG_REPORT_ID) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_activities_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = ActivityPageAdapter(this, reportId)
        viewPager.offscreenPageLimit = 2

        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            tab.text = if (pos == 0) "Добавить" else "История"
        }.attach()
    }

}
