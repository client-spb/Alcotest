package kras.example.many.Screen.Main.Drinks

enum class DrinkAmountUnit {
    LITERS,
    GRAMS,
}

data class DrinkConfig(
    val id: String,
    val name: String,
    val emoji: String,
    val unit: DrinkAmountUnit,
    val portionSize: Float,
    val defaultAmount: Float,
    val step: Float,
    val minAmount: Float,
    val defaultDegrees: Float,
    val minDegrees: Float,
    val maxDegrees: Float,
) {
    fun portionLabel(): String = formatAmount(portionSize, withUnit = true, shortUnit = true)

    fun formatAmount(value: Float, withUnit: Boolean = false, shortUnit: Boolean = false): String {
        val amountText = when (unit) {
            DrinkAmountUnit.LITERS -> if (value % 1f == 0f) value.toInt().toString() else value.toString()
            DrinkAmountUnit.GRAMS -> value.toInt().toString()
        }
        if (!withUnit) return amountText
        val unitText = when (unit) {
            DrinkAmountUnit.LITERS -> if (shortUnit) "л" else " л"
            DrinkAmountUnit.GRAMS -> if (shortUnit) "г" else " г"
        }
        return amountText + unitText
    }
}

object DrinksCatalog {
    val beer = DrinkConfig(
        id = "beer",
        name = "Пиво",
        emoji = "🍺",
        unit = DrinkAmountUnit.LITERS,
        portionSize = 0.5f,
        defaultAmount = 0f,
        step = 0.5f,
        minAmount = 0f,
        defaultDegrees = 5.0f,
        minDegrees = 2.0f,
        maxDegrees = 12.0f,
    )

    val wine = DrinkConfig(
        id = "wine",
        name = "Вино",
        emoji = "🍷",
        unit = DrinkAmountUnit.GRAMS,
        portionSize = 150f,
        defaultAmount = 0f,
        step = 150f,
        minAmount = 0f,
        defaultDegrees = 12.0f,
        minDegrees = 8.0f,
        maxDegrees = 18.0f,
    )

    val spirits = DrinkConfig(
        id = "spirits",
        name = "Крепкие",
        emoji = "🥃",
        unit = DrinkAmountUnit.GRAMS,
        portionSize = 50f,
        defaultAmount = 0f,
        step = 50f,
        minAmount = 0f,
        defaultDegrees = 40.0f.coerceIn(wine.maxDegrees, 99f),
        minDegrees = wine.maxDegrees,
        maxDegrees = 99f,
    )

    val all = listOf(beer, wine, spirits)
}
