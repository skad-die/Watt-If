package com.skaddie.watt_if.data.repository

import com.skaddie.watt_if.data.datastore.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PreferencesRepository @Inject constructor(
    private val userPreferences: UserPreferences
) {
    val defaultRate: Flow<Double> = userPreferences.defaultRate
    val submeterName: Flow<String> = userPreferences.submeterName

    suspend fun saveDefaultRate(rate: Double) = userPreferences.saveDefaultRate(rate)
    suspend fun saveSubmeterName(name: String) = userPreferences.saveSubmeterName(name)
}