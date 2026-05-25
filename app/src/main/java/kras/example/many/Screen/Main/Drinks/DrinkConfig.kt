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
    val defaultDegrees: Int,
    val minDegrees: Int,
    val maxDegrees: Int,
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
        defaultDegrees = 5,
        minDegrees = 2,
        maxDegrees = 12,
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
        defaultDegrees = 12,
        minDegrees = 8,
        maxDegrees = 18,
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
        defaultDegrees = 40.coerceIn(wine.maxDegrees, 99),
        minDegrees = wine.maxDegrees,
        maxDegrees = 99,
    )

    val all = listOf(beer, wine, spirits)
}
