package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_logs")
data class WeightLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weightKg: Float,
    val dateString: String, // e.g., "28 Mar 2026"
    val timestamp: Long = System.currentTimeMillis()
)
