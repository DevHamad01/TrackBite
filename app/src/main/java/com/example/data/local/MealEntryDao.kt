package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MealEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MealEntryDao {
    @Query("SELECT * FROM meal_entries WHERE date = :date ORDER BY id DESC")
    fun getEntriesForDate(date: String): Flow<List<MealEntry>>

    @Query("SELECT DISTINCT date FROM meal_entries WHERE isConfirmed = 1")
    fun getLoggedDates(): Flow<List<String>>

    @Query("SELECT * FROM meal_entries ORDER BY id DESC")
    fun getAllEntries(): Flow<List<MealEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MealEntry): Long

    @Update
    suspend fun updateEntry(entry: MealEntry)

    @Delete
    suspend fun deleteEntry(entry: MealEntry)

    @Query("DELETE FROM meal_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("SELECT * FROM meal_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): MealEntry?
}
