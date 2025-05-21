package com.example.fitbite.utils

fun translateMetricToRussian(metric: String): String {
    return when (metric) {
        "pcs" -> "шт"
        "g" -> "г"
        "ml" -> "мл"
        else -> metric
    }
}

fun translateMealTypeToRussian(mealType: String): String {
    return when (mealType.lowercase()) {
        "breakfast" -> "Завтрак"
        "lunch" -> "Обед"
        "dinner" -> "Ужин"
        "snack" -> "Перекус"
        else -> mealType
    }
}
