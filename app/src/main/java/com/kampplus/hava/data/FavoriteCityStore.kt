package com.kampplus.hava.data

import android.content.Context
import com.google.gson.Gson
import com.kampplus.hava.model.City

/** Persists the user's saved locations locally on the device. */
class FavoriteCityStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun read(): List<City> = preferences.getString(KEY_CITIES, null)
        ?.let { json -> runCatching { gson.fromJson(json, Array<City>::class.java)?.toList().orEmpty() }.getOrDefault(emptyList()) }
        .orEmpty()

    fun write(cities: List<City>) {
        preferences.edit().putString(KEY_CITIES, gson.toJson(cities)).apply()
    }

    private companion object {
        const val PREFERENCES = "hava_saved_cities"
        const val KEY_CITIES = "cities"
    }
}
