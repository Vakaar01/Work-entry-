package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PieceEntryDao {
    @Query("SELECT * FROM piece_entries ORDER BY date DESC, createdAt DESC")
    fun getAllEntries(): Flow<List<PieceEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PieceEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<PieceEntryEntity>)

    @Update
    suspend fun update(entry: PieceEntryEntity)

    @Delete
    suspend fun delete(entry: PieceEntryEntity)

    @Query("DELETE FROM piece_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM piece_entries")
    suspend fun clearAll()

    @Query("SELECT DISTINCT pieceName FROM piece_entries WHERE pieceName != '' ORDER BY pieceName ASC")
    fun getDistinctPieceNames(): Flow<List<String>>

    @Query("SELECT DISTINCT color FROM piece_entries WHERE color != '' ORDER BY color ASC")
    fun getDistinctColors(): Flow<List<String>>
}
