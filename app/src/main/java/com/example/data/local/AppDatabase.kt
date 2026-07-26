package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.MealEntry
import com.example.data.model.SavedEntry
import com.example.data.model.UserProfile
import com.example.data.model.WaterLog

@Database(
    entities = [
        MealEntry::class,
        WaterLog::class,
        UserProfile::class,
        SavedEntry::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun waterLogDao(): WaterLogDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun savedEntryDao(): SavedEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trackbite_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
