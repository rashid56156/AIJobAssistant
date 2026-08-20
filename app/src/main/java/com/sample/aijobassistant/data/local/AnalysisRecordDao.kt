package com.sample.aijobassistant.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisRecordDao {

    @Insert
    suspend fun insert(record: AnalysisRecordEntity): Long

    @Query("SELECT * FROM analysis_records ORDER BY timestamp DESC")
    fun getAll(): Flow<List<AnalysisRecordEntity>>

    @Query("DELETE FROM analysis_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
