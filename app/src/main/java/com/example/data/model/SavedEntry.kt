package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_entries")
data class SavedEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val promptText: String,
    val isFavorite: Boolean = true,
    val isRecent: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
