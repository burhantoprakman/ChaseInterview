package com.chase.interview.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")
class DataStoreManager(private val context: Context) {

    // Define a key for the last searched city
    companion object {
        val LAST_SEARCHED_CITY_KEY = stringPreferencesKey("last_searched_city")
    }

    // Save the last searched city
    suspend fun saveLastSearchedCity(city: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SEARCHED_CITY_KEY] = city
        }
    }

    // Retrieve the last searched city
    val lastSearchedCity: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_SEARCHED_CITY_KEY]
        }
}