package com.example.fitbite.presentation.viewmodel

import com.example.fitbite.data.model.UserParameters

class InfoUserViewModel {

    // Рассчитываем норму калорий
    fun calculateCalories(
        weight: Float,
        height: Int?,
        age: Int,
        gender: String,
        activity: String,
        result: String
    ): Int {
        val safeHeight = height ?: return 0
        // Пример расчета для женского пола
        val bmr = if (gender == "Женский") {
            10 * weight + 6.25 * height - 5 * age - 161
        } else {
            10 * weight + 6.25 * height - 5 * age + 5
        }

        // Уровень активности
        val activityFactor = when (activity) {
            "Отсутствие физической активности" -> 1.2
            "Легкая активность" -> 1.375
            "Средняя активность" -> 1.55
            "Высокая активность" -> 1.725
            "Очень высокая активность" -> 1.9
            else -> 1.2
        }

        // Суточная норма калорий для поддержания веса
        val tdee = bmr * activityFactor

        // Желаемый результат
        return when (result) {
            "Быстрое похудение" -> (tdee - 500).toInt()
            "Умеренное похудение" -> (tdee - 300).toInt()
            "Поддержание веса" -> tdee.toInt()
            "Набор массы" -> (tdee + 300).toInt()
            else -> tdee.toInt()
        }
    }

    // Новая функция: возвращает норму калорий И изменение веса за месяц
    fun calculateCaloriesAndWeightChange(
        weight: Float,
        height: Int?,
        age: Int,
        gender: String,
        activity: String,
        result: String
    ): Pair<Int, Float> {
        val safeHeight = height ?: return Pair(0, 0f)

        val bmr = if (gender == "Женский") {
            10 * weight + 6.25 * safeHeight - 5 * age - 161
        } else {
            10 * weight + 6.25 * safeHeight - 5 * age + 5
        }

        val activityFactor = when (activity) {
            "Отсутствие физической активности" -> 1.2
            "Легкая активность" -> 1.375
            "Средняя активность" -> 1.55
            "Высокая активность" -> 1.725
            "Очень высокая активность" -> 1.9
            else -> 1.2
        }

        val tdee = bmr * activityFactor

        val recommendedCalories = when (result) {
            "Быстрое похудение" -> tdee - 500
            "Умеренное похудение" -> tdee - 300
            "Поддержание веса" -> tdee
            "Набор массы" -> tdee + 300
            else -> tdee
        }

        val calorieDifference = recommendedCalories - tdee
        val weightChangePerDay = calorieDifference / 7700f
        val weightChangePerMonth = (weightChangePerDay * 30)

        return Pair(recommendedCalories.toInt(), weightChangePerMonth.toFloat())
    }


    // Функция для расчета ИМТ
    fun calculateBMI(weight: Float, height: Int?): Float {
        height ?: return 0.0f  // безопасно выйти, если height == null
        val heightInMeters = height / 100f
        return weight / (heightInMeters * heightInMeters)
    }


    // Функция для расчета минимального веса для набора
    fun calculateWeightToGain(bmi: Float, weight: Float, height: Int?): Float {
        height ?: return 0.0f
        val heightMeters = height / 100f
        val targetWeight = 18.5f * heightMeters * heightMeters
        return targetWeight - weight
    }

    // Функция для расчета минимального веса для сброса
    fun calculateWeightToLose(bmi: Float, weight: Float, height: Int?): Float {
        height ?: return 0.0f
        val heightMeters = height / 100f
        val targetWeight = 24.9f * heightMeters * heightMeters
        return weight - targetWeight
    }

    // Функция для расчета суточной нормы воды
    fun calculateWaterIntake(
        weight: Float,
        age: Int,
        gender: String,
        activity: String,
        result: String

    ): Float {
        // Исходное количество воды рассчитываем как 35 мл на 1 кг веса
        var waterIntake = weight * 35 / 1000f  // в литрах

        // Учитываем пол: мужчины могут пить немного больше
        if (gender == "Мужской") {
            waterIntake *= 1.1f  // Увеличиваем норму воды для мужчин на 10%
        }

        // Учитываем возраст: для людей старше 60 лет уменьшаем потребность на 10%
        if (age > 60) {
            waterIntake *= 0.9f  // Уменьшаем норму воды на 10% для пожилых людей
        }

        // Уровень активности, который влияет на потребление воды
        val activityFactor = when (activity) {
            "Отсутствие физической активности" -> 0.9f
            "Легкая активность" -> 1.0f
            "Средняя активность" -> 1.1f
            "Высокая активность" -> 1.2f
            "Очень высокая активность" -> 1.3f
            else -> 1.0f
        }

        waterIntake *= activityFactor

        // Для целей работы с весом:
        when (result) {
            "Быстрое похудение" -> {
                waterIntake *= 1.10f  // Увеличиваем норму воды на 10% для быстрого похудения
            }

            "Умеренное похудение" -> {
                waterIntake *= 1.05f  // Увеличиваем норму воды на 5% для умеренного похудения
            }

            "Поддержание веса" -> {
                // Без изменений
            }

            "Набор массы" -> {
                waterIntake *= 1.05f  // Увеличиваем норму воды на 5% для набора массы
            }
        }

        return waterIntake
    }
}