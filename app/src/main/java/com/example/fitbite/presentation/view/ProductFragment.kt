package com.example.fitbite.presentation.view

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.R
import com.example.fitbite.data.model.Product
import com.example.fitbite.data.repository.ProductRepository
import com.example.fitbite.presentation.adapter.ProductAdapter
import kotlinx.coroutines.launch

class ProductDialogFragment : DialogFragment() {

    private val productRepository = ProductRepository()
    private lateinit var productAdapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Применяем текущую тему в зависимости от глобальной настройки
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // Темная тема
            setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        } else {
            // Светлая тема
            setStyle(STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar_Fullscreen)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Находим элементы в layout
        val recyclerView = view.findViewById<RecyclerView>(R.id.productsRecyclerView)
        val searchEditText = view.findViewById<EditText>(R.id.searchEditText)

        // Инициализируем адаптер
        productAdapter = ProductAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = productAdapter

        // Загружаем данные о продуктах
        viewLifecycleOwner.lifecycleScope.launch {
            val products = productRepository.getProducts()
            if (products != null) {
                allProducts = products.map { product ->
                    product.copy(metric = translateMetricToRussian(product.metric))
                }
                productAdapter.updateData(allProducts)
            } else {
                Log.e("ProductDialog", "Не удалось загрузить продукты")
            }
        }

        // Обрабатываем ввод в строке поиска
        searchEditText.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            val filteredProducts = allProducts.filter { product ->
                product.name.contains(query, ignoreCase = true)
            }
            productAdapter.updateData(filteredProducts)
        }
    }
    fun translateMetricToRussian(metric: String): String {
        return when (metric) {
            "pcs" -> "шт"
            "g" -> "г"
            "ml" -> "мл"
            else -> metric
        }
    }

}


