package kras.example.many.Screen.Main

import android.content.Context

object UserParamsStorage {
    private const val PREFS_NAME = "user_params"
    private const val KEY_AGE = "age"
    private const val KEY_WEIGHT = "weight"
    private const val KEY_GENDER = "gender"

    private var sharedPreferences: android.content.SharedPreferences? = null

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getAge(): Int {
        return sharedPreferences?.getInt(KEY_AGE, 35) ?: 35
    }

    fun getWeight(): Int {
        return sharedPreferences?.getInt(KEY_WEIGHT, 72) ?: 72
    }

    fun getGender(): String {
        return sharedPreferences?.getString(KEY_GENDER, "М") ?: "М"
    }

    fun saveAge(age: Int) {
        sharedPreferences?.edit()?.putInt(KEY_AGE, age)?.apply()
    }

    fun saveWeight(weight: Int) {
        sharedPreferences?.edit()?.putInt(KEY_WEIGHT, weight)?.apply()
    }

    fun saveGender(gender: String) {
        sharedPreferences?.edit()?.putString(KEY_GENDER, gender)?.apply()
    }
}
