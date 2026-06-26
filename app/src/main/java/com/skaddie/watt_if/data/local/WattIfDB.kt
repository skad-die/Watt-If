package com.skaddie.watt_if.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.skaddie.watt_if.data.local.dao.ReadingDao
import com.skaddie.watt_if.data.local.entity.ReadingEntity

@Database(
    entities = [ReadingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WattIfDB : RoomDatabase() {
    abstract fun readingDao(): ReadingDao
}