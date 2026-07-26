package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey val date: String, // e.g., "28 Mar 2026"
    val cups: Int = 0,
    val targetCups: Int = 8
)
