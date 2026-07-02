package com.skaddie.watt_if.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skaddie.watt_if.data.local.entity.ReadingEntity
import com.skaddie.watt_if.data.repository.PreferencesRepository
import com.skaddie.watt_if.data.repository.ReadingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentKwh: String = "",
    val previousKwh: String = "",
    val rate: String = "",
    val consumption: Double = 0.0,
    val estimatedBill: Double = 0.0,
    val currentKwhError: String? = null,
    val rateError: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val readingRepository: ReadingRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDefaultRate()
    }

    private fun loadDefaultRate() {
        viewModelScope.launch {
            val savedRate = preferencesRepository.defaultRate.firstOrNullValid()
            val savedReading = preferencesRepository.lastKwhReading.firstOrNullValid()

            _uiState.update {
                it.copy(
                    rate = if (savedRate > 0.0) savedRate.toString() else it.rate,
                    previousKwh = if (savedReading > 0.0) savedReading.toString() else it.previousKwh
                )
            }
            recalculate()
        }
    }

    fun validate(): Boolean {
        val state = _uiState.value
        val current = state.currentKwh.toDoubleOrNull()
        val previous = state.previousKwh.toDoubleOrNull() ?: 0.0
        val rate = state.rate.toDoubleOrNull()

        var currentError: String? = null
        var rateError: String? = null

        when {
            state.currentKwh.isBlank() -> currentError = "Please enter your current reading"
            current == null -> currentError = "Invalid number"
            current <= previous -> currentError = "Must be higher than previous reading"
        }

        when {
            state.rate.isBlank() -> rateError = "Please enter the rate per kWh"
            rate == null -> rateError = "Invalid number"
            rate <= 0.0 -> rateError = "Rate must be greater than 0"
        }

        _uiState.update {
            it.copy(
                currentKwhError = currentError,
                rateError = rateError
            )
        }

        return currentError == null && rateError == null
    }

    fun saveReading() {
        if (!validate()) return

        val state = _uiState.value
        val current = state.currentKwh.toDoubleOrNull() ?: return
        val previous = state.previousKwh.toDoubleOrNull() ?: 0.0
        val rate = state.rate.toDoubleOrNull() ?: return

        viewModelScope.launch {
            readingRepository.insertReading(
                ReadingEntity(
                    currentKwh = current,
                    previousKwh = previous,
                    ratePerKwh = rate,
                    consumption = state.consumption,
                    totalBill = state.estimatedBill
                )
            )
            preferencesRepository.saveDefaultRate(rate)
            preferencesRepository.saveLastKwhReading(current)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun onCurrentKwhChange(value: String) {
        _uiState.update {
            it.copy(
                currentKwh = value,
                currentKwhError = null,
                isSaved = false
            )
        }
        recalculate()
    }

    fun onPreviousKwhChange(value: String) {
        _uiState.update {
            it.copy(
                previousKwh = value,
                currentKwhError = null,
                isSaved = false
            )
        }
        recalculate()
    }

    fun onRateChange(value: String) {
        _uiState.update {
            it.copy(
                rate = value,
                rateError = null,
                isSaved = false
            )
        }
        recalculate()
    }

    fun onSavedConsumed() {
        _uiState.update { it.copy(isSaved = false) }
    }

    private fun recalculate() {
        val state = _uiState.value
        val current = state.currentKwh.toDoubleOrNull() ?: 0.0
        val previous = state.previousKwh.toDoubleOrNull() ?: 0.0
        val rate = state.rate.toDoubleOrNull() ?: 0.0

        val consumption = (current - previous).coerceAtLeast(0.0)
        val bill = consumption * rate

        _uiState.update { it.copy(consumption = consumption, estimatedBill = bill) }
    }
}

private suspend fun kotlinx.coroutines.flow.Flow<Double>.firstOrNullValid(): Double {
    return try {
        this.first()
    } catch (e: Exception) {
        0.0
    }
}