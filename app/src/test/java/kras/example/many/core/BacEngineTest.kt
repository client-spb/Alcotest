package kras.example.many.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BacEngineTest {
    private val profile = Profile(male = true, weightKg = 80, heightCm = 180, age = 35)
    private val t0 = 1_700_000_000_000L
    private val hour = 3_600_000L

    @Test
    fun emptySessionGivesEmptyForecast() {
        assertTrue(BacEngine.compute(emptyList(), profile, Stomach.SNACK, t0).isEmpty)
    }

    @Test
    fun peakMatchesWidmarkWatson() {
        // 100 мл водки 40% ≈ 31.6 г этанола
        val drink = DrinkEntry(1, DrinkType.SPIRITS, 100, 40f, t0)
        val f = BacEngine.compute(listOf(drink), profile, Stomach.EMPTY, t0 + 10 * hour)
        val theoretical = drink.grams * BacEngine.promillePerGram(profile) * (1 - Stomach.EMPTY.deficit)
        assertTrue(f.peak < theoretical)
        assertTrue(f.peak > theoretical - BacEngine.ELIMINATION_PER_HOUR)
        assertEquals(0f, f.current, 0.001f)
        assertNotNull(f.soberMs)
    }

    @Test
    fun fullStomachLowersPeak() {
        val drinks = listOf(DrinkEntry(1, DrinkType.BEER, 1000, 5f, t0))
        val empty = BacEngine.compute(drinks, profile, Stomach.EMPTY, t0)
        val full = BacEngine.compute(drinks, profile, Stomach.FULL, t0)
        assertTrue(full.peak < empty.peak)
    }

    @Test
    fun driveTimeBeforeSoberTime() {
        val drinks = List(3) { DrinkEntry(it.toLong(), DrinkType.WINE, 150, 12f, t0) }
        val f = BacEngine.compute(drinks, profile, Stomach.SNACK, t0)
        assertTrue(f.driveMs!! <= f.soberMs!!)
    }

    @Test
    fun hourlyForecastStartsAtMeasurementAndEndsAtSoberTime() {
        val now = t0 + 2 * hour
        val f = BacEngine.compute(listOf(DrinkEntry(1, DrinkType.SPIRITS, 250, 40f, t0)), profile, Stomach.EMPTY, now)
        val rows = f.hourlyFrom(now)
        assertEquals(BacPoint(now, f.current), rows.first())
        assertEquals(f.soberMs, rows.last().timeMs)
        assertEquals(0f, rows.last().promille, 0.0001f)
        rows.dropLast(1).forEachIndexed { i, point -> assertEquals(now + i * hour, point.timeMs) }
        assertTrue(rows.zipWithNext().all { (a, b) -> a.timeMs < b.timeMs && a.promille >= b.promille })
    }

    @Test
    fun hourlyForecastIncludesRisingAbsorption() {
        val now = t0 + 10 * 60_000L
        val f = BacEngine.compute(listOf(DrinkEntry(1, DrinkType.SPIRITS, 250, 40f, t0)), profile, Stomach.FULL, now)
        val rows = f.hourlyFrom(now)
        assertTrue(rows[1].promille > rows.first().promille)
    }

    @Test
    fun hourlyForecastForPastSessionHasOnlyCurrentRow() {
        val now = t0 + 24 * hour
        val f = BacEngine.compute(listOf(DrinkEntry(1, DrinkType.BEER, 500, 5f, t0)), profile, Stomach.EMPTY, now)
        assertEquals(listOf(BacPoint(now, 0f)), f.hourlyFrom(now))
        assertTrue(Forecast.EMPTY.hourlyFrom(now).isEmpty())
    }
}
