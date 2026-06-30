package com.skaddie.watt_if.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skaddie.watt_if.data.local.entity.ReadingEntity
import com.skaddie.watt_if.data.repository.ReadingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentKwh: String = "",
    val previousKwh: String = "",
    val rate: String = "",
    val consumption: Double = 0.0,
    val estimatedBill: Double = 0.0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val readingRepository: ReadingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onCurrentKwhChange(value: String) {
        _uiState.update { it.copy(currentKwh = value) }
        recalculate()
    }

    fun onPreviousKwhChange(value: String) {
        _uiState.update { it.copy(previousKwh = value) }
        recalculate()
    }

    fun onRateChange(value: String) {
        _uiState.update { it.copy(rate = value) }
        recalculate()
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

    fun saveReading() {
        val state = _uiState.value
        val current = state.currentKwh.toDoubleOrNull() ?: return
        val previous = state.previousKwh.toDoubleOrNull() ?: return
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
        }
    }
}