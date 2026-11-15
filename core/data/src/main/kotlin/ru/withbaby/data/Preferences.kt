package ru.withbaby.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.preferences by preferencesDataStore("preferences")
data class Settings(val parent: String = "", val theme: String = "Системная", val weatherEnabled: Boolean = false,
 val city: String = "", val lat: Double = 0.0, val lon: Double = 0.0, val notifications: Boolean = false)
class Preferences(context: Context) {
 private val store = context.preferences
 private val parent = stringPreferencesKey("parent")
 private val theme = stringPreferencesKey("theme")
 private val weather = booleanPreferencesKey("weather")
 private val city = stringPreferencesKey("city")
 private val lat = doublePreferencesKey("lat")
 private val lon = doublePreferencesKey("lon")
 private val notifications = booleanPreferencesKey("notifications")
 val settings = store.data.map { Settings(it[parent] ?: "", it[theme] ?: "Системная", it[weather] ?: false, it[city] ?: "", it[lat] ?: 0.0, it[lon] ?: 0.0, it[notifications] ?: false) }
 suspend fun save(s: Settings) { store.edit { it[parent]=s.parent; it[theme]=s.theme; it[weather]=s.weatherEnabled; it[city]=s.city; it[lat]=s.lat; it[lon]=s.lon; it[notifications]=s.notifications } }
 suspend fun clear() { store.edit { it.clear() } }
}
