package com.sample.aijobassistant.data.repository

import com.sample.aijobassistant.data.remote.GeminiAnalysisDataSource
import com.sample.aijobassistant.domain.model.AppResult
import com.sample.aijobassistant.domain.model.ErrorType
import com.sample.aijobassistant.domain.model.MatchAnalysis
import com.sample.aijobassistant.domain.repository.ApiKeyRepository
import com.sample.aijobassistant.domain.repository.ResumeAnalysisRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of [ResumeAnalysisRepository].
 *
 * Coordinates job-match analysis through the Gemini data source and retrieves
 * the current API key from [ApiKeyRepository] for each analysis request.
 */
@Singleton
class ResumeAnalysisRepositoryImpl @Inject constructor(
    private val geminiAnalysisDataSource: GeminiAnalysisDataSource,
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
}
