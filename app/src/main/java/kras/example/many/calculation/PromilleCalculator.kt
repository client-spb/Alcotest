package kras.example.many.calculation

import kras.example.many.Screen.Main.Drinks.DrinkAmountUnit
import kras.example.many.Screen.Main.Drinks.DrinksCatalog
import java.time.LocalTime
import kotlin.math.ceil

object PromilleCalculator {

    private const val ETHANOL_DENSITY = 0.789f
    private const val ELIMINATION_PER_HOUR = 0.15f
    const val RUSTORE_APP_URL = "https://www.rustore.ru/catalog/app/kras.example.many"

    fun calculate(input: CalculationInput): CalculationResult {
        val alcoholGrams = totalAlcoholGrams(input)
        val r = if (input.gender == "М") 0.7f else 0.6f
        // Формула Видмарка: промилле = масса алкоголя (г) / (вес тела (кг) × r)
        // Умножаем на 1000 для перевода в промилле (г/кг)
        val initial = (alcoholGrams / (r * input.weightKg)) 
        val baseTime = input.endTime.withMinute(0).withSecond(0).withNano(0)
        val points = buildEliminationSchedule(initial, baseTime)
        val shareText = buildShareText(points, initial)
        return CalculationResult(
            initialPromille = initial,
            points = points,
            shareText = shareText,
        )
    }

    private fun totalAlcoholGrams(input: CalculationInput): Float {
        var total = 0f
        DrinksCatalog.all.forEach { config ->
            val amount = input.drinkAmounts[config.id] ?: 0f
            if (amount <= 0f) return@forEach
            val degrees = input.drinkDegrees[config.id] ?: config.defaultDegrees
            // Крепость в процентах (например, 40% = 0.40)
            val alcoholFraction = degrees / 100f
            // Расчет массы чистого алкоголя в граммах
            total += when (config.unit) {
                DrinkAmountUnit.LITERS -> {
                    // amount в литрах, переводим в мл (×1000), затем умножаем на крепость и плотность этанола
                    amount * 1000f * alcoholFraction * ETHANOL_DENSITY
                }
                DrinkAmountUnit.GRAMS -> {
                    // amount в граммах (мл), умножаем на крепость и плотность этанола
                    amount * alcoholFraction * ETHANOL_DENSITY
                }
            }
        }
        return total
    }

    private fun buildEliminationSchedule(
        initial: Float,
        baseTime: LocalTime,
    ): List<EliminationPoint> {
        if (initial <= 0f) {
            return listOf(
                EliminationPoint(
                    title = "Сразу после",
                    timeLabel = formatHour(baseTime),
                    promille = 0f,
                )
            )
        }

        val soberHours = ceil(initial / ELIMINATION_PER_HOUR).toInt().coerceAtLeast(1)
        val points = mutableListOf<EliminationPoint>()

        points.add(
            EliminationPoint(
                title = "Сразу после",
                timeLabel = formatHour(baseTime),
                promille = roundPromille(initial),
            )
        )

        for (hour in 1 until soberHours) {
            val promille = initial - ELIMINATION_PER_HOUR * hour
            points.add(
                EliminationPoint(
                    title = "Через $hour ч",
                    timeLabel = formatHour(baseTime.plusHours(hour.toLong())),
                    promille = roundPromille(promille),
                )
            )
        }

        points.add(
            EliminationPoint(
                title = "Через $soberHours ч",
                timeLabel = formatHour(baseTime.plusHours(soberHours.toLong())),
                promille = 0f,
            )
        )

        return points
    }

    private fun formatHour(time: LocalTime): String {
        return String.format("%02d:00", time.hour)
    }

    private fun roundPromille(value: Float): Float {
        return (value * 10f).toInt() / 10f
    }

    private fun formatPromille(value: Float): String {
        return String.format("%.1f ‰", roundPromille(value))
    }

    private fun buildShareText(points: List<EliminationPoint>, initial: Float): String {
        val builder = StringBuilder()
        builder.appendLine("АлкоТестер")
        builder.appendLine("Динамика выведения")
        builder.appendLine()
        builder.appendLine("Первое — сразу после: ${formatPromille(initial)}")
        builder.appendLine()
        points.forEach { point ->
            builder.appendLine("${point.timeLabel}  ${point.title}  ${formatPromille(point.promille)}")
        }
        builder.appendLine()
        builder.appendLine("Скачать приложение:")
        builder.appendLine(RUSTORE_APP_URL)
        return builder.toString().trim()
    }
}
