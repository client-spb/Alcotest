package kras.example.many.core

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import kras.example.many.ui.theme.ThemeId
import org.json.JSONArray
import org.json.JSONObject

/** Единое состояние приложения с сохранением в SharedPreferences. */
object AppStore {
    private const val PREFS = "alcotest_v2"
    private const val MAX_HISTORY = 30
    private lateinit var prefs: SharedPreferences

    var profile by mutableStateOf(Profile())
        private set
    var drinks by mutableStateOf<List<DrinkEntry>>(emptyList())
        private set
    var stomach by mutableStateOf(Stomach.SNACK)
        private set
    var history by mutableStateOf<List<SessionRecord>>(emptyList())
        private set
    var theme by mutableStateOf(ThemeId.GRAPHITE)
        private set
    var disclaimerAccepted by mutableStateOf(false)
        private set
    /** Последние выбранные объём/крепость для каждого напитка. */
    private val lastUsed = mutableMapOf<DrinkType, Pair<Int, Float>>()

    /** Всплывающее уведомление (тост) — читает корневой экран. */
    var toast by mutableStateOf<String?>(null)
    /** Растёт при каждом изменении сессии — для показа межстраничной рекламы. */
    var changeCounter by mutableStateOf(0)
        private set

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        runCatching { load() }
        if (!prefs.contains("profile")) runCatching { migrateLegacy(context) }
    }

    fun updateProfile(p: Profile) { profile = p; save() }
    fun updateStomach(s: Stomach) { stomach = s; touch() }
    fun updateTheme(t: ThemeId) { theme = t; save() }
    fun acceptDisclaimer() { disclaimerAccepted = true; save() }

    fun lastFor(type: DrinkType): Pair<Int, Float> = lastUsed[type] ?: (type.defaultMl to type.defaultAbv)

    fun addDrinks(type: DrinkType, volumeMl: Int, abv: Float, timeMs: Long, count: Int) {
        val base = System.nanoTime()
        drinks = drinks + List(count) { DrinkEntry(base + it, type, volumeMl, abv, timeMs) }
        lastUsed[type] = volumeMl to abv
        toast = "${type.emoji} ${if (count > 1) "$count × " else ""}${type.title} добавлено"
        touch()
    }

    fun removeDrink(id: Long) { drinks = drinks.filterNot { it.id == id }; touch() }

    /** Завершает сессию; при наличии напитков сохраняет её в историю. */
    fun finishSession(nowMs: Long) {
        if (drinks.isNotEmpty()) {
            val f = BacEngine.compute(drinks, profile, stomach, nowMs)
            val record = SessionRecord(
                startMs = drinks.minOf { it.timeMs },
                endMs = drinks.maxOf { it.timeMs },
                drinks = drinks.size,
                totalMl = drinks.sumOf { it.volumeMl },
                grams = drinks.sumOf { it.grams.toDouble() }.toFloat(),
                peak = f.peak,
                emojis = drinks.map { it.type.emoji }.distinct().joinToString(""),
            )
            history = (listOf(record) + history).take(MAX_HISTORY)
            toast = "Сессия сохранена в историю"
        }
        drinks = emptyList()
        touch()
    }

    fun clearHistory() { history = emptyList(); save() }

    private fun touch() { changeCounter++; save() }

    private fun save() {
        if (!::prefs.isInitialized) return
        prefs.edit {
            putString("profile", JSONObject().apply {
                put("male", profile.male); put("weight", profile.weightKg); put("height", profile.heightCm)
                put("age", profile.age); put("limit", profile.driveLimit.toDouble())
            }.toString())
            putString("drinks", JSONArray().apply {
                drinks.forEach { d ->
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
            drinks = (0 until a.length()).mapNotNull { i ->
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
}
