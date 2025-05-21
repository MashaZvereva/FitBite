package com.example.fitbite.presentation.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.R
import com.example.fitbite.data.model.MealCompositionRequest
import com.example.fitbite.data.model.Product
import com.example.fitbite.data.network.RetrofitInstance
import com.example.fitbite.data.repository.ProductRepository
import com.example.fitbite.data.storage.SessionManager
import com.example.fitbite.domain.usecase.sensor.calc.WaterViewModel
import com.example.fitbite.presentation.adapter.ProductAdapter
import com.example.fitbite.presentation.viewmodel.DailySummaryViewModel
import kotlinx.coroutines.launch

class ProductListFragment : Fragment() {

    private val productRepository = ProductRepository()
    private lateinit var productAdapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()

    private val sessionManager: SessionManager by lazy { SessionManager(requireContext()) }


    private var reportId: Int = -1
    private var mealType: String = "Неизвестно"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            reportId = it.getInt(ARG_REPORT_ID, -1)
            mealType = it.getString(ARG_MEAL_TYPE) ?: "Неизвестно"
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

        val recyclerView = view.findViewById<RecyclerView>(R.id.productsRecyclerView)
        val searchEditText = view.findViewById<EditText>(R.id.searchEditText)
        val dailySummaryViewModel = ViewModelProvider(requireActivity())[DailySummaryViewModel::class.java]

        productAdapter = ProductAdapter(
            emptyList(),
            onAddClick = { product, portion ->
                Log.d("ProductListFragment", "ID продукта: ${product.id}")
                Log.d("ProductListFragment", "Отправка: productId=${product.id}, portion=$portion, mealType=${mapMealTypeToServer(mealType)}")

                if (portion == 0) {
                    Log.e("ProductListFragment", "Ошибка: размер порции не может быть 0.")
                    return@ProductAdapter
                }

                val request = MealCompositionRequest(
                    product_id = product.id,
                    recipe_id = null,
                    portion_size = portion.toDouble(),
                    meal_type = mapMealTypeToServer(mealType)
                )

                val token = sessionManager.fetchAuthToken()
                if (token != null && reportId != -1) {
                    lifecycleScope.launch {
                        try {
                            val response = RetrofitInstance.api.addMealComposition("Bearer $token", reportId, request)
                            if (response.isSuccessful) {
                                parentFragmentManager.setFragmentResult("refreshMealComposition", Bundle().apply {
                                    putBoolean("shouldRefresh", true)
                                })
                                parentFragmentManager.popBackStack()
                                val meal = response.body()
                                Log.d("ProductListFragment", "Продукт добавлен: $meal")
                                val waterViewModel = ViewModelProvider(requireActivity())[WaterViewModel::class.java]
                                waterViewModel.refreshWater(reportId)
                                dailySummaryViewModel.refreshSummary(reportId) // ✅ Добавили вызов здесь
                            } else {
                                Log.e("ProductListFragment", "Ошибка при добавлении продукта: ${response.errorBody()?.string()}")
                            }
                        } catch (e: Exception) {
                            Log.e("ProductListFragment", "Ошибка сети или на сервере: ${e.message}")
                        }
                    }
                } else {
                    Log.e("ProductListFragment", "token или reportId невалидны")
                }
            },
            dailySummaryViewModel = dailySummaryViewModel,
            reportId = reportId
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = productAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            val products = productRepository.getProducts()
            if (products != null) {
                allProducts = products.map { product ->
                    product.copy(metric = translateMetricToRussian(product.metric))
                }
                productAdapter.updateData(allProducts)
            } else {
                Log.e("ProductListFragment", "Не удалось загрузить продукты")
            }
        }

        // Поиск по названию
        searchEditText.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            val filteredProducts = allProducts.filter { product ->
                product.name.contains(query, ignoreCase = true)
            }
            productAdapter.updateData(filteredProducts)
        }
    }

    private fun translateMetricToRussian(metric: String): String {
        return when (metric) {
            "pcs" -> "шт"
            "g" -> "г"
            "ml" -> "мл"
            else -> metric
        }
    }

    fun mapMealTypeToServer(value: String): String {
        return when (value.lowercase()) {
            "завтрак", "breakfast" -> "breakfast"
            "обед", "lunch" -> "lunch"
            "ужин", "dinner" -> "dinner"
            "перекус", "snack" -> "snack"
            else -> {
                Log.e("ProductListFragment", "Неверный тип приема пищи: $value, по умолчанию snack")
                "snack"
            }
        }
    }

    companion object {
        private const val ARG_REPORT_ID = "report_id"
        private const val ARG_MEAL_TYPE = "meal_type"

        fun newInstance(reportId: Int, mealType: String): ProductListFragment {
            val fragment = ProductListFragment()
            val args = Bundle()
            args.putInt(ARG_REPORT_ID, reportId)
            args.putString(ARG_MEAL_TYPE, mealType)
            fragment.arguments = args
            return fragment
        }
    }
}






