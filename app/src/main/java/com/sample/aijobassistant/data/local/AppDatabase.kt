package com.sample.aijobassistant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [AnalysisRecordEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(AnalysisListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun analysisRecordDao(): AnalysisRecordDao

    companion object {
        const val DATABASE_NAME = "job_assistant_db"
    }
}
