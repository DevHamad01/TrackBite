package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SavedEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedEntryDao {
    @Query("SELECT * FROM saved_entries WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getSavedEntries(): Flow<List<SavedEntry>>

    @Query("SELECT * FROM saved_entries WHERE isRecent = 1 ORDER BY timestamp DESC LIMIT 15")
    fun getRecentEntries(): Flow<List<SavedEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedEntry(savedEntry: SavedEntry)

    @Delete
    suspend fun deleteSavedEntry(savedEntry: SavedEntry)

    @Query("DELETE FROM saved_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM saved_entries")
    suspend fun deleteAllSavedEntries()
}
