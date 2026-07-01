package com.skaddie.watt_if.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skaddie.watt_if.data.local.entity.ReadingEntity
import com.skaddie.watt_if.data.repository.ReadingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val readingRepository: ReadingRepository
) : ViewModel() {

    val readings: StateFlow<List<ReadingEntity>> = readingRepository
        .getAllReadings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var deletedReading: ReadingEntity? = null
    fun deleteReading(reading: ReadingEntity) {
        deletedReading = reading
        viewModelScope.launch {
            readingRepository.deleteReading(reading)
        }
    }

    fun undoDelete(reading: ReadingEntity) {
        viewModelScope.launch {
            readingRepository.insertReading(reading)
        }
    }
}