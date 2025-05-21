package com.example.fitbite.presentation.adapter

import android.util.Log
import com.example.fitbite.data.model.Activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView

import com.example.fitbite.R
class ActivityAdapter(
    private var activities: List<Activity>,
    private val onAddClick: (activityId: Int, minutes: Int) -> Unit
) : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    inner class ActivityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameTv: TextView = view.findViewById(R.id.activity_name)
        private val calPerMinTv: TextView = view.findViewById(R.id.activity_calories_burned)
        private val minutesEt: EditText = view.findViewById(R.id.portion_input_minutes)
        private val addBtn: Button = view.findViewById(R.id.add_button)

        fun bind(act: Activity) {
            nameTv.text = act.nameActivity ?: "Неизвестная активность"
            calPerMinTv.text = "Калории/час: ${act.caloriesBurned}"

            // По умолчанию кнопка не активна
            addBtn.isEnabled = false

            // Слушатель изменений в поле ввода
            minutesEt.addTextChangedListener {
                val mins = it.toString().toIntOrNull()
                addBtn.isEnabled = mins != null && mins in 1..600
                minutesEt.error = if (mins == null || mins !in 1..600)
                    "Введите от 1 до 600 минут"
                else null
            }

            addBtn.setOnClickListener {
                val mins = minutesEt.text.toString().toIntOrNull()
                if (mins != null && mins in 1..600) {
                    onAddClick(act.id, mins)
                } else {
                    minutesEt.error = "Введите от 1 до 600 минут"
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(activities[position])
    }

    override fun getItemCount(): Int = activities.size

    fun updateData(newActivities: List<Activity>) {
        activities = newActivities
        notifyDataSetChanged()
    }
}




