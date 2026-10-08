package kras.example.many.core

enum class DrinkType(
    val title: String,
    val emoji: String,
    val presetsMl: List<Int>,
    val defaultMl: Int,
    val stepMl: Int,
    val defaultAbv: Float,
    val minAbv: Float,
    val maxAbv: Float,
) {
    BEER("Пиво", "🍺", listOf(330, 500, 1000), 500, 50, 5f, 0.5f, 14f),
    WINE("Вино", "🍷", listOf(100, 150, 250, 750), 150, 25, 12f, 8f, 18f),
    SPARKLING("Игристое", "🥂", listOf(100, 150, 750), 150, 25, 11f, 6f, 14f),
    SPIRITS("Крепкое", "🥃", listOf(30, 50, 100, 250), 50, 10, 40f, 25f, 70f),
    COCKTAIL("Коктейль", "🍹", listOf(200, 250, 330), 250, 25, 8f, 3f, 30f),
    CIDER("Сидр", "🍏", listOf(330, 500, 1000), 500, 50, 5f, 1f, 10f),
}

data class DrinkEntry(
    val id: Long,
    val type: DrinkType,
    val volumeMl: Int,
    val abv: Float,
    val timeMs: Long,
) {
    /** Масса чистого этанола, г. */
    val grams: Float get() = volumeMl * abv / 100f * ETHANOL_DENSITY

    companion object {
        const val ETHANOL_DENSITY = 0.789f
    }
}

enum class Stomach(val title: String, val emoji: String, val absorptionMin: Int, val deficit: Float) {
    EMPTY("Натощак", "🫙", 30, 0.10f),
    SNACK("Перекус", "🥨", 60, 0.15f),
    FULL("Плотно", "🍖", 90, 0.25f),
}

data class Profile(
    val male: Boolean = true,
    val weightKg: Int = 75,
    val heightCm: Int = 178,
    val age: Int = 30,
    val driveLimit: Float = 0.3f,
)

data class SessionRecord(
    val startMs: Long,
    val endMs: Long,
    val drinks: Int,
    val totalMl: Int,
    val grams: Float,
    val peak: Float,
    val emojis: String,
)
