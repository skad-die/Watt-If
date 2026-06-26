package com.skaddie.watt_if.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "readings")
data class ReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val currentKwh: Double,
    val previousKwh: Double,
    val ratePerKwh: Double,
    val consumption: Double,
    val totalBill: Double,
    val date: Long = System.currentTimeMillis()
)