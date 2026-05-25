package kras.example.many.Screen.Main.Drinks

import android.content.Context

object DrinkParamsStorage {
    private const val PREFS_NAME = "drink_params"
    private const val KEY_DEGREES_PREFIX = "degrees_"
    private const val KEY_DEGREES_FLOAT_PREFIX = "degrees_float_"

    private var sharedPreferences: android.content.SharedPreferences? = null

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getDegrees(drinkId: String, default: Int): Int {
        return sharedPreferences?.getInt(KEY_DEGREES_PREFIX + drinkId, default) ?: default
    }

    fun saveDegrees(drinkId: String, degrees: Int) {
        sharedPreferences?.edit()?.putInt(KEY_DEGREES_PREFIX + drinkId, degrees)?.apply()
    }

    fun getDegreesFloat(drinkId: String, default: Float): Float {
        return sharedPreferences?.getFloat(KEY_DEGREES_FLOAT_PREFIX + drinkId, default) ?: default
    }

    fun saveDegreesFloat(drinkId: String, degrees: Float) {
        sharedPreferences?.edit()?.putFloat(KEY_DEGREES_FLOAT_PREFIX + drinkId, degrees)?.apply()
    }
}
