package kras.example.many.core

import kotlin.math.max
import kotlin.math.min

data class BacPoint(val timeMs: Long, val promille: Float)

data class Forecast(
    val points: List<BacPoint>,
    val current: Float,
    val peak: Float,
    val peakMs: Long,
    /** Момент, когда уровень опустится ниже лимита для вождения (null — уже можно / не пили). */
    val driveMs: Long?,
    /** Момент полного выведения (null — не пили). */
    val soberMs: Long?,
) {
    val isEmpty get() = points.isEmpty()

    /** Почасовой прогноз от момента измерения, с точной последней отметкой выведения. */
    fun hourlyFrom(timeMs: Long): List<BacPoint> {
        if (isEmpty) return emptyList()
        val end = soberMs ?: points.last().timeMs
        val result = arrayListOf(BacPoint(timeMs, current))
        if (end <= timeMs) return result
        var next = timeMs + 3_600_000L
        while (next < end) {
            val right = points.indexOfFirst { it.timeMs >= next }
            val value = when {
                right <= 0 -> points.first().promille
                else -> {
                    val a = points[right - 1]
                    val b = points[right]
                    val fraction = (next - a.timeMs).toFloat() / (b.timeMs - a.timeMs)
                    a.promille + (b.promille - a.promille) * fraction
                }
            }
            result += BacPoint(next, value.coerceAtLeast(0f))
            next += 3_600_000L
        }
        result += BacPoint(end, points.last().promille)
        return result
    }

    companion object {
        val EMPTY = Forecast(emptyList(), 0f, 0f, 0L, null, null)
    }
}

/**
 * Модель Видмарка с коэффициентом распределения по Уотсону (учитывает пол, вес, рост, возраст),
 * линейным всасыванием каждой порции и элиминацией 0.15 ‰/ч.
 */
object BacEngine {
    const val ELIMINATION_PER_HOUR = 0.15f
    private const val STEP_MS = 60_000L
    private const val CHART_EVERY = 5
    private const val MAX_STEPS = 72 * 60

    /** Общее количество воды в организме, л (формула Уотсона). */
    fun totalBodyWater(p: Profile): Float = if (p.male) {
        2.447f - 0.09516f * p.age + 0.1074f * p.heightCm + 0.3362f * p.weightKg
    } else {
        -2.097f + 0.1069f * p.heightCm + 0.2466f * p.weightKg
    }.coerceAtLeast(10f)

    /** Прирост ‰ на 1 г этанола. */
    fun promillePerGram(p: Profile): Float = 0.8f / (1.055f * totalBodyWater(p))

    fun compute(drinks: List<DrinkEntry>, profile: Profile, stomach: Stomach, nowMs: Long): Forecast {
        if (drinks.isEmpty()) return Forecast.EMPTY
        val sorted = drinks.sortedBy { it.timeMs }
        val start = sorted.first().timeMs / STEP_MS * STEP_MS
        val absorbMs = stomach.absorptionMin * 60_000L
        val k = promillePerGram(profile) * (1f - stomach.deficit)
        val lastAbsorbed = sorted.last().timeMs + absorbMs
        val elimPerStep = ELIMINATION_PER_HOUR / 60f

        val points = ArrayList<BacPoint>()
        var bac = 0f
        var peak = 0f
        var peakMs = start
        var current = 0f
        var driveMs: Long? = null
        var soberMs: Long? = null
        var t = start
        var step = 0
        while (step < MAX_STEPS) {
            val next = t + STEP_MS
            // Сколько каждой порции всосалось за [t, next)
            var absorbed = 0f
            for (d in sorted) {
                val a = max(t, d.timeMs)
                val b = min(next, d.timeMs + absorbMs)
                if (b > a) absorbed += d.grams * (b - a).toFloat() / absorbMs
            }
            bac = max(0f, bac + absorbed * k - if (bac > 0f || absorbed > 0f) elimPerStep else 0f)
            t = next
            step++
            if (bac > peak) { peak = bac; peakMs = t }
            if (t <= nowMs) current = bac
            if (step % CHART_EVERY == 0) points += BacPoint(t, bac)
            if (t >= lastAbsorbed) {
                if (driveMs == null && bac <= profile.driveLimit) driveMs = t
                if (bac <= 0f) { soberMs = t; break }
            }
        }
        if (points.isEmpty() || points.last().timeMs != t) points += BacPoint(t, bac)
        points.add(0, BacPoint(start, 0f))
        return Forecast(
            points = points,
            current = current,
            peak = peak,
            peakMs = peakMs,
            driveMs = if (peak <= profile.driveLimit) null else driveMs,
            soberMs = soberMs,
        )
    }

    fun stateOf(promille: Float): BacState = BacState.entries.last { promille >= it.from }
}

enum class BacState(val from: Float, val title: String, val hint: String, val level: Int) {
    SOBER(0f, "Трезвый", "Алкоголь не обнаружен", 0),
    TRACE(0.01f, "Следы", "Влияние минимально", 0),
    LIGHT(0.3f, "Лёгкое", "Эйфория, снижение внимания", 1),
    MEDIUM(1f, "Среднее", "Нарушена координация и речь", 2),
    STRONG(2f, "Сильное", "Риск отравления, нужен отдых", 3),
    SEVERE(3f, "Тяжёлое", "Опасно для жизни — обратитесь к врачу", 4),
}
