package com.example.fitbite.presentation.adapter

import android.util.Log
import com.example.fitbite.data.model.Activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.example.fitbite.R


class ActivityAdapter(private var activities: List<Activity>) : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    class ActivityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.activity_name)
        val caloriesBurned: TextView = view.findViewById(R.id.activity_calories_burned)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]
        holder.name.text = activity.nameActivity ?: "Неизвестная активность"
        holder.caloriesBurned.text = "Калории: ${activity.caloriesBurned}"
        Log.d("ActivityAdapter", "Activities count: ${activities.size}")
    }

    override fun getItemCount() = activities.size

    // Обновление данных
    fun updateData(newActivities: List<Activity>) {
        activities = newActivities
        notifyDataSetChanged()
    }
}


