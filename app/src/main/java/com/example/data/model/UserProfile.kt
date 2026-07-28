package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val targetCalories: Int = 2523,
    val targetCarbs: Int = 316,
    val targetProtein: Int = 158,
    val targetFat: Int = 71,
    val targetWeightKg: Float = 70f,
    val streakCount: Int = 4,
    val freeEntriesRemaining: Int = 5,
    val isPremium: Boolean = false
)
