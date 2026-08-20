package com.sample.aijobassistant.data.repository

import com.sample.aijobassistant.data.local.AnalysisRecordDao
import com.sample.aijobassistant.data.local.toDomain
import com.sample.aijobassistant.data.local.toEntity
import com.sample.aijobassistant.data.remote.GeminiAnalysisDataSource
import com.sample.aijobassistant.domain.model.AnalysisRecord
import com.sample.aijobassistant.domain.model.AppResult
import com.sample.aijobassistant.domain.model.ErrorType
import com.sample.aijobassistant.domain.model.MatchAnalysis
import com.sample.aijobassistant.domain.repository.ApiKeyRepository
import com.sample.aijobassistant.domain.repository.ResumeAnalysisRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of [ResumeAnalysisRepository]. Bridges the Gemini
 * remote data source and the Room DAO. Reads the API key from
 * [ApiKeyRepository] right before each call rather than caching it, so a key
 * change in Settings takes effect on the very next analysis with no extra
 * wiring needed.
 */
@Singleton
class ResumeAnalysisRepositoryImpl @Inject constructor(
    private val geminiAnalysisDataSource: GeminiAnalysisDataSource,
    private val analysisRecordDao: AnalysisRecordDao,
    private val apiKeyRepository: ApiKeyRepository
) : ResumeAnalysisRepository {

    override suspend fun analyzeMatch(
        jobDescription: String,
        resumeText: String
    ): AppResult<MatchAnalysis> {
        val apiKey = apiKeyRepository.getApiKey()
            ?: return AppResult.Error(
                ErrorType.MISSING_API_KEY,
                "No Gemini API key configured. Add one in Settings."
            )

        return geminiAnalysisDataSource.analyze(apiKey, jobDescription, resumeText)
    }

    override suspend fun saveRecord(jobTitle: String, analysis: MatchAnalysis): Long {
        val entity = analysis.toEntity(jobTitle = jobTitle, timestamp = System.currentTimeMillis())
        return analysisRecordDao.insert(entity)
    }

    override fun getHistory(): Flow<List<AnalysisRecord>> =
        analysisRecordDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun deleteRecord(id: Long) = analysisRecordDao.deleteById(id)
}
