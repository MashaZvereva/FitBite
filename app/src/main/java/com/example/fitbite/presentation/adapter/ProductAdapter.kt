package com.example.fitbite.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.R
import com.example.fitbite.data.model.Product

class ProductAdapter(
    private var products: List<Product>,
    private val onAddClick: (Product, Int) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.product_name)
        val calories: TextView = view.findViewById(R.id.product_calories)
        val metric: TextView = view.findViewById(R.id.product_metric)
        val addButton: Button = view.findViewById(R.id.add_button)
        val portionInput: EditText = view.findViewById(R.id.portion_input)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.name.text = product.name
        holder.calories.text = "Калории: ${product.calories ?: 0.0}"
        holder.metric.text = "Мера: ${product.metric}"

        // Кнопка по умолчанию отключена
        holder.addButton.isEnabled = false

        // Следим за вводом и включаем кнопку, если ввод корректный
        holder.portionInput.addTextChangedListener {
            val portion = it.toString().toIntOrNull()
            holder.addButton.isEnabled = portion != null && portion in 1..1000
            holder.portionInput.error = if (portion == null || portion !in 1..1000)
                "Введите от 1 до 1000"
            else null
        }

        holder.addButton.setOnClickListener {
            val portion = holder.portionInput.text.toString().toIntOrNull()
            if (portion != null && portion in 1..1000) {
                onAddClick(product, portion)
            } else {
                holder.portionInput.error = "Введите от 1 до 1000"
            }
        }
    }

    override fun getItemCount() = products.size

    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
