package kras.example.many.calculation

import java.time.LocalTime

data class CalculationInput(
    val age: Int,
    val weightKg: Int,
    val gender: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val drinkAmounts: Map<String, Float>,
    val drinkDegrees: Map<String, Float>,
)

data class EliminationPoint(
    val title: String,
    val timeLabel: String,
    val promille: Float,
)

data class CalculationResult(
    val initialPromille: Float,
    val points: List<EliminationPoint>,
    val shareText: String,
)
