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
}
