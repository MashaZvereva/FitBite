package com.example.fitbite.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.R
import com.example.fitbite.data.model.Product

class ProductAdapter(private var products: List<Product>) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.product_name)
        val calories: TextView = view.findViewById(R.id.product_calories)
        val metric: TextView = view.findViewById(R.id.product_metric)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.name.text = product.name
        holder.calories.text = "Калории: ${product.calories ?: 0.0}"
        holder.metric.text = "Мера: ${product.metric}"
    }

    override fun getItemCount() = products.size

    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
