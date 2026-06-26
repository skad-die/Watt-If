package com.skaddie.watt_if.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.skaddie.watt_if.data.local.entity.ReadingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: ReadingEntity)

    @Query("SELECT * FROM readings ORDER BY date DESC")
    fun getAllReadings(): Flow<List<ReadingEntity>>

    @Delete
    suspend fun deleteReading(reading: ReadingEntity)
}