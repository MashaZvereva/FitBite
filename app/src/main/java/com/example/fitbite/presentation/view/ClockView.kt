package com.example.fitbite.presentation.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.fitbite.R
import java.util.*

class ClockView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint()
    private val calendar = Calendar.getInstance()

    private var hour = 0
    private var minute = 0
    private val backgroundBitmap: Bitmap

    init {
        paint.color = Color.BLACK
        paint.strokeWidth = 8f
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE

        // Загружаем изображение тарелки
        backgroundBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.time_food)
    }

    // Метод для рисования круга и стрелок
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        val centerX = width / 2
        val centerY = height / 2
        val radius = minOf(centerX, centerY) - 110f  // Отступ от центра для радиуса

        // Масштабируем изображение тарелки
        val scale = radius * 2 / Math.min(backgroundBitmap.width, backgroundBitmap.height) // Масштабируем изображение по радиусу
        val scaledBitmap = Bitmap.createScaledBitmap(backgroundBitmap, (backgroundBitmap.width * scale).toInt(), (backgroundBitmap.height * scale).toInt(), true)

        // Маска для округления изображения
        val bitmapSize = minOf(scaledBitmap.width, scaledBitmap.height)
        val roundedBitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888)
        val canvasForBitmap = Canvas(roundedBitmap)
        val path = Path()
        path.addCircle((bitmapSize / 2).toFloat(), (bitmapSize / 2).toFloat(), (bitmapSize / 2).toFloat(), Path.Direction.CW)
        canvasForBitmap.clipPath(path)
        canvasForBitmap.drawBitmap(scaledBitmap, 0f, 0f, paint)

        // Рисуем изображение тарелки как круглый фон
        canvas.drawBitmap(roundedBitmap, centerX - bitmapSize / 2, centerY - bitmapSize / 2, paint)

        // Получаем текущее время
        calendar.timeInMillis = System.currentTimeMillis()
        hour = calendar.get(Calendar.HOUR_OF_DAY)
        minute = calendar.get(Calendar.MINUTE)

        // Часовая стрелка: сдвиг на 90 градусов, чтобы начать с 12:00
        val hourAngle = (hour % 12) * 30f + (minute / 60f) * 30f - 90f  // 30 градусов на каждый час + дополнительное движение по минутам
        val hourX = centerX + (radius - 90) * Math.cos(Math.toRadians(hourAngle.toDouble())).toFloat()  // Уменьшаем радиус стрелки
        val hourY = centerY + (radius - 90) * Math.sin(Math.toRadians(hourAngle.toDouble())).toFloat()  // Уменьшаем радиус стрелки

        paint.color = Color.BLACK
        paint.strokeWidth = 15f
        canvas.drawLine(centerX, centerY, hourX, hourY, paint)

        // Минутная стрелка: сдвиг на 90 градусов, чтобы начать с 12:00
        val minuteAngle = minute * 6f - 90f  // 6 градусов на каждую минуту
        val minuteX = centerX + (radius - 70) * Math.cos(Math.toRadians(minuteAngle.toDouble())).toFloat()  // Уменьшаем радиус стрелки
        val minuteY = centerY + (radius - 70) * Math.sin(Math.toRadians(minuteAngle.toDouble())).toFloat()  // Уменьшаем радиус стрелки

        paint.color = Color.BLACK  // Минутная стрелка красная
        paint.strokeWidth = 10f
        canvas.drawLine(centerX, centerY, minuteX, minuteY, paint)
    }
}
