package com.example.fitbite.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.databinding.ItemDailyActivityBinding
import com.example.fitbite.data.model.DailyActivity

class DailyActivityAdapter(
    private var items: List<DailyActivity>,
    private val onDeleteClick: (DailyActivity) -> Unit
) : RecyclerView.Adapter<DailyActivityAdapter.VH>() {

    inner class VH(val binding: ItemDailyActivityBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DailyActivity) {
            binding.activityName.text    = item.activityName
            binding.durationMinutes.text = "${item.durationMinutes} мин"
            binding.caloriesBurned.text  = "${item.caloriesBurned.toInt()} ккал"

            binding.removeButton.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemDailyActivityBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<DailyActivity>) {
        items = newItems
        notifyDataSetChanged()
    }
}

