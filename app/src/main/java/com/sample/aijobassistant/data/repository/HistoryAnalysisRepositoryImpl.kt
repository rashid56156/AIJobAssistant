package com.sample.aijobassistant.data.repository

import com.sample.aijobassistant.data.local.AnalysisRecordDao
import com.sample.aijobassistant.data.local.toDomain
import com.sample.aijobassistant.data.local.toEntity
import com.sample.aijobassistant.domain.model.AnalysisRecord
import com.sample.aijobassistant.domain.model.MatchAnalysis
import com.sample.aijobassistant.domain.repository.AnalysisHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of [AnalysisHistoryRepository].
 *
 * Persists and retrieves analysis history through the Room DAO and maps
 * database entities to domain models.
 */

@Singleton
class HistoryAnalysisRepositoryImpl @Inject constructor(
    private val analysisRecordDao: AnalysisRecordDao
) : AnalysisHistoryRepository {


    override suspend fun saveRecord(jobTitle: String, analysis: MatchAnalysis): Long {
        val entity = analysis.toEntity(jobTitle = jobTitle, timestamp = System.currentTimeMillis())
        return analysisRecordDao.insert(entity)
    }

    override fun getHistory(): Flow<List<AnalysisRecord>> =
        analysisRecordDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun deleteRecord(id: Long) = analysisRecordDao.deleteById(id)
}
