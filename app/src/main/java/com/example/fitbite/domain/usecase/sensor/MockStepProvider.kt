package com.example.fitbite.domain.usecase.sensor

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

class MockStepProvider : StepProvider {
    private var steps = 0
    private val isRunning = AtomicBoolean(false)
    private var handler: Handler? = null
    private var runnable: Runnable? = null

    override fun start(listener: (steps: Int) -> Unit) {
        if (isRunning.get()) {
            Log.w("MockStepProvider", "Already running, ignoring duplicate start()")
            return
        }
        isRunning.set(true)

        listener(steps) // Сразу отображаем текущее значение

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                if (!isRunning.get()) {
                    Log.d("MockStepProvider", "Stopped, skipping step generation")
                    return
                }
                steps += (1..5).random()
                listener(steps)
                handler?.postDelayed(this, 1000)
            }
        }
        handler?.postDelayed(runnable!!, 1000)
    }

    override fun stop() {
        if (!isRunning.get()) {
            Log.w("MockStepProvider", "Already stopped, ignoring duplicate stop()")
            return
        }
        isRunning.set(false)
        handler?.removeCallbacksAndMessages(null)
        handler = null
        runnable = null
        Log.d("MockStepProvider", "MockStepProvider stopped")
    }
}
