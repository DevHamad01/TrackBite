package com.example.data.repository

import com.example.data.local.MealEntryDao
import com.example.data.local.SavedEntryDao
import com.example.data.local.UserProfileDao
import com.example.data.local.WaterLogDao
import com.example.data.local.WeightLogDao
import com.example.data.model.MealEntry
import com.example.data.model.SavedEntry
import com.example.data.model.UserProfile
import com.example.data.model.WaterLog
import com.example.data.model.WeightLog
import kotlinx.coroutines.flow.Flow

class JournableRepository(
    private val mealEntryDao: MealEntryDao,
    private val waterLogDao: WaterLogDao,
    private val userProfileDao: UserProfileDao,
    private val savedEntryDao: SavedEntryDao,
    private val weightLogDao: WeightLogDao
) {
    fun getEntriesForDate(date: String): Flow<List<MealEntry>> =
        mealEntryDao.getEntriesForDate(date)

    fun getAllMealEntries(): Flow<List<MealEntry>> =
        mealEntryDao.getAllEntries()

    fun getLoggedDates(): Flow<List<String>> =
        mealEntryDao.getLoggedDates()

    fun getWaterLogForDate(date: String): Flow<WaterLog?> =
        waterLogDao.getWaterLogForDate(date)

    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()

    val savedEntries: Flow<List<SavedEntry>> = savedEntryDao.getSavedEntries()

    val recentEntries: Flow<List<SavedEntry>> = savedEntryDao.getRecentEntries()

    val weightLogs: Flow<List<WeightLog>> = weightLogDao.getAllWeightLogs()

    suspend fun insertWeightLog(weightLog: WeightLog): Long =
        weightLogDao.insertWeightLog(weightLog)

    suspend fun deleteWeightLog(id: Long) =
        weightLogDao.deleteWeightLogById(id)

    suspend fun insertMealEntry(entry: MealEntry): Long {
        val id = mealEntryDao.insertEntry(entry)
        savedEntryDao.insertSavedEntry(
            SavedEntry(
                promptText = entry.originalPrompt,
                isFavorite = false,
                isRecent = true
            )
        )
        return id
    }

    suspend fun updateMealEntry(entry: MealEntry) {
        mealEntryDao.updateEntry(entry)
    }

    suspend fun deleteMealEntry(id: Long) {
        mealEntryDao.deleteEntryById(id)
    }

    suspend fun updateWaterLog(waterLog: WaterLog) {
        waterLogDao.insertOrUpdateWaterLog(waterLog)
    }

    suspend fun saveProfile(profile: UserProfile) {
        userProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun addSavedEntry(promptText: String) {
        savedEntryDao.insertSavedEntry(
            SavedEntry(
                promptText = promptText,
                isFavorite = true,
                isRecent = false
            )
        )
    }

    suspend fun deleteSavedEntry(id: Long) {
        savedEntryDao.deleteById(id)
    }

    suspend fun clearAllData() {
        mealEntryDao.deleteAllEntries()
        waterLogDao.deleteAllWaterLogs()
        savedEntryDao.deleteAllSavedEntries()
        weightLogDao.deleteAllWeightLogs()
        userProfileDao.deleteProfile()
    }
}
