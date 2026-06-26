package com.skaddie.watt_if.data.repository

import com.skaddie.watt_if.data.local.dao.ReadingDao
import com.skaddie.watt_if.data.local.entity.ReadingEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReadingRepository @Inject constructor(
    private val readingDao: ReadingDao
) {
    fun getAllReadings(): Flow<List<ReadingEntity>> = readingDao.getAllReadings()

    suspend fun insertReading(reading: ReadingEntity) = readingDao.insertReading(reading)

    suspend fun deleteReading(reading: ReadingEntity) = readingDao.deleteReading(reading)
}