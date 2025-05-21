package com.example.fitbite.domain.usecase.sensor

interface StepProvider {
    fun start(listener: (steps: Int) -> Unit)
    fun stop()
}
