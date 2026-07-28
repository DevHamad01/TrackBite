package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_entries")
data class MealEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // e.g. "2026-03-28" or "28 Mar 2026"
    val originalPrompt: String,
    val formattedTime: String, // e.g. "17:12"
    val itemsJson: String, // JSON representation of List<FoodItem>
    val totalCalories: Int,
    val totalCarbs: Int,
    val totalProtein: Int,
    val totalFat: Int,
    val isSaved: Boolean = false,
    val isConfirmed: Boolean = true,
    val imagePath: String? = null,
    val isAnalyzing: Boolean = false
)
