package kras.example.many.core

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import kras.example.many.ui.theme.ThemeId
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** Напиток в черновике расчёта (шаг 1). */
data class DraftDrink(val id: Long, val type: DrinkType, val volumeMl: Int, val abv: Float, val count: Int)

/** Единое состояние приложения с сохранением в SharedPreferences. */
object AppStore {
    private const val PREFS = "alcotest_v2"
    private const val MAX_HISTORY = 30
    const val STEP_DRINKS = 0
    const val STEP_TIME = 1
    const val STEP_PROFILE = 2
    const val STEP_RESULT = 3
    private lateinit var prefs: SharedPreferences

    var profile by mutableStateOf(Profile())
        private set
    var stomach by mutableStateOf(Stomach.SNACK)
        private set
    var history by mutableStateOf<List<SessionRecord>>(emptyList())
        private set
    var theme by mutableStateOf(ThemeId.GRAPHITE)
        private set
    var disclaimerAccepted by mutableStateOf(false)
        private set

    // ---- Мастер расчёта ----
    var step by mutableIntStateOf(STEP_DRINKS)
    var draft by mutableStateOf<List<DraftDrink>>(emptyList())
        private set
    /** Начало и конец застолья, минуты от полуночи. */
    var startMin by mutableIntStateOf(0)
        private set
    var endMin by mutableIntStateOf(0)
        private set
    /** Напитки последнего расчёта с проставленным временем. */
    var result by mutableStateOf<List<DrinkEntry>>(emptyList())
        private set
    /** Идёт расчёт (показ рекламы). */
    var calculating by mutableStateOf(false)

    /** Последние выбранные объём/крепость для каждого напитка. */
    private val lastUsed = mutableMapOf<DrinkType, Pair<Int, Float>>()

    /** Всплывающее уведомление (тост) — читает корневой экран. */
    var toast by mutableStateOf<String?>(null)

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        resetTimes()
        runCatching { load() }
        if (!prefs.contains("profile")) runCatching { migrateLegacy(context) }
        step = if (result.isNotEmpty()) STEP_RESULT else STEP_DRINKS
    }

    fun updateProfile(p: Profile) { profile = p; save() }
    fun updateStomach(s: Stomach) { stomach = s; save() }
    fun updateTheme(t: ThemeId) { theme = t; save() }
    fun acceptDisclaimer() { disclaimerAccepted = true; save() }
    fun updateStart(min: Int) { startMin = norm(min) }
    fun updateEnd(min: Int) { endMin = norm(min) }

    fun lastFor(type: DrinkType): Pair<Int, Float> = lastUsed[type] ?: (type.defaultMl to type.defaultAbv)

    fun addDraft(type: DrinkType, volumeMl: Int, abv: Float, count: Int) {
        draft = draft + DraftDrink(System.nanoTime(), type, volumeMl, abv, count)
        lastUsed[type] = volumeMl to abv
        toast = "${type.emoji} ${if (count > 1) "$count × " else ""}${type.title} добавлено"
        save()
    }

    fun removeDraft(id: Long) { draft = draft.filterNot { it.id == id } }

    /** Раскладывает порции равномерно по времени застолья и сохраняет результат в историю. */
    fun calculate(nowMs: Long) {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        fun ms(min: Int) = today.atTime(LocalTime.of(min / 60, min % 60)).atZone(zone).toInstant().toEpochMilli()
        var start = ms(startMin)
        var end = ms(endMin)
        if (end < start) start -= DAY_MS
        // Указанное время ещё не наступило — значит, пили вчера
        if (end > nowMs + 30 * 60_000L) { start -= DAY_MS; end -= DAY_MS }

        val portions = draft.flatMap { d -> List(d.count) { d } }
        val n = portions.size
        result = portions.mapIndexed { i, d ->
            val t = if (n == 1) start else start + (end - start) * i / (n - 1)
            DrinkEntry(d.id + i, d.type, d.volumeMl, d.abv, t)
        }
        if (result.isNotEmpty()) {
            val f = BacEngine.compute(result, profile, stomach, nowMs)
            val record = SessionRecord(
                startMs = start,
                endMs = end,
                drinks = n,
                totalMl = result.sumOf { it.volumeMl },
                grams = result.sumOf { it.grams.toDouble() }.toFloat(),
                peak = f.peak,
                emojis = result.map { it.type.emoji }.distinct().joinToString(""),
            )
            history = (listOf(record) + history).take(MAX_HISTORY)
        }
        step = STEP_RESULT
        save()
    }

    /** Начать новый расчёт с чистого листа. */
    fun newCalculation() {
        draft = emptyList()
        result = emptyList()
        resetTimes()
        step = STEP_DRINKS
        save()
    }

    fun clearHistory() { history = emptyList(); save() }

    private fun resetTimes() {
        val now = LocalTime.now()
        val rounded = (now.hour * 60 + now.minute) / 15 * 15
        endMin = rounded
        startMin = norm(rounded - 120)
    }

    private fun norm(min: Int) = ((min % DAY_MIN) + DAY_MIN) % DAY_MIN

    private fun save() {
        if (!::prefs.isInitialized) return
        prefs.edit {
            putString("profile", JSONObject().apply {
                put("male", profile.male); put("weight", profile.weightKg); put("height", profile.heightCm)
                put("age", profile.age); put("limit", profile.driveLimit.toDouble())
            }.toString())
            putString("drinks", JSONArray().apply {
                result.forEach { d ->
                    put(JSONObject().apply {
                        put("id", d.id); put("type", d.type.name); put("ml", d.volumeMl)
                        put("abv", d.abv.toDouble()); put("t", d.timeMs)
                    })
                }
            }.toString())
            putString("history", JSONArray().apply {
                history.forEach { h ->
                    put(JSONObject().apply {
                        put("s", h.startMs); put("e", h.endMs); put("n", h.drinks); put("ml", h.totalMl)
                        put("g", h.grams.toDouble()); put("p", h.peak.toDouble()); put("em", h.emojis)
                    })
                }
            }.toString())
            putString("last", JSONObject().apply {
                lastUsed.forEach { (k, v) -> put(k.name, "${v.first};${v.second}") }
            }.toString())
            putString("stomach", stomach.name)
            putString("theme", theme.name)
            putBoolean("disclaimer", disclaimerAccepted)
        }
    }

    private fun load() {
        prefs.getString("profile", null)?.let {
            val o = JSONObject(it)
            profile = Profile(
                male = o.optBoolean("male", true),
                weightKg = o.optInt("weight", 75),
                heightCm = o.optInt("height", 178),
                age = o.optInt("age", 30),
                driveLimit = o.optDouble("limit", 0.3).toFloat(),
            )
        }
        prefs.getString("drinks", null)?.let {
            val a = JSONArray(it)
            result = (0 until a.length()).mapNotNull { i ->
                val o = a.getJSONObject(i)
                val type = DrinkType.entries.firstOrNull { t -> t.name == o.optString("type") } ?: return@mapNotNull null
                DrinkEntry(o.getLong("id"), type, o.getInt("ml"), o.getDouble("abv").toFloat(), o.getLong("t"))
            }
        }
        prefs.getString("history", null)?.let {
            val a = JSONArray(it)
            history = (0 until a.length()).map { i ->
                val o = a.getJSONObject(i)
                SessionRecord(
                    o.getLong("s"), o.getLong("e"), o.getInt("n"), o.getInt("ml"),
                    o.getDouble("g").toFloat(), o.getDouble("p").toFloat(), o.optString("em"),
                )
            }
        }
        prefs.getString("last", null)?.let {
            val o = JSONObject(it)
            DrinkType.entries.forEach { t ->
                val parts = o.optString(t.name).split(";")
                if (parts.size == 2) {
                    val ml = parts[0].toIntOrNull(); val abv = parts[1].toFloatOrNull()
                    if (ml != null && abv != null) lastUsed[t] = ml to abv
                }
            }
        }
        stomach = Stomach.entries.firstOrNull { it.name == prefs.getString("stomach", null) } ?: Stomach.SNACK
        theme = ThemeId.entries.firstOrNull { it.name == prefs.getString("theme", null) } ?: ThemeId.GRAPHITE
        disclaimerAccepted = prefs.getBoolean("disclaimer", false)
    }

    /** Подхватывает вес/возраст/пол из предыдущей версии приложения. */
    private fun migrateLegacy(context: Context) {
        val old = context.getSharedPreferences("user_params", Context.MODE_PRIVATE)
        if (!old.contains("weight")) return
        profile = profile.copy(
            male = old.getString("gender", "М") == "М",
            weightKg = old.getInt("weight", profile.weightKg),
            age = old.getInt("age", profile.age),
        )
    }

    private const val DAY_MIN = 24 * 60
    private const val DAY_MS = 24 * 60 * 60_000L
}
