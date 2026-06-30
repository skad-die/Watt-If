package com.skaddie.watt_if.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "watt_if_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val DEFAULT_RATE = doublePreferencesKey("default_rate")
        val SUBMETER_NAME = stringPreferencesKey("submeter_name")
        val LAST_KWH_READING = doublePreferencesKey("last_kwh_reading")
    }

    val defaultRate: Flow<Double> = context.dataStore.data
        .map { it[DEFAULT_RATE] ?: 0.0 }

    val submeterName: Flow<String> = context.dataStore.data
        .map { it[SUBMETER_NAME] ?: "My Submeter" }

    val lastKwhReading: Flow<Double> = context.dataStore.data
        .map { it[LAST_KWH_READING] ?: 0.0 }

    suspend fun saveDefaultRate(rate: Double) {
        context.dataStore.edit { it[DEFAULT_RATE] = rate }
    }

    suspend fun saveSubmeterName(name: String) {
        context.dataStore.edit { it[SUBMETER_NAME] = name }
    }

    suspend fun saveLastKwhReading(reading: Double) {
        context.dataStore.edit { it[LAST_KWH_READING] = reading }
    }
}