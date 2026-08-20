package com.sample.aijobassistant.domain.repository

import com.sample.aijobassistant.domain.model.AnalysisRecord
import com.sample.aijobassistant.domain.model.AppResult
import com.sample.aijobassistant.domain.model.MatchAnalysis
import kotlinx.coroutines.flow.Flow

/**
 * Domain owns this contract; data/repository provides the implementation.
 * Use cases and ViewModels depend on this interface only — never on the
 * concrete Gemini SDK or Room types. That's what lets us unit test use
 * cases with a fake repository instead of mocking Android/network internals.
 */
interface ResumeAnalysisRepository {

    suspend fun analyzeMatch(
        jobDescription: String,
        resumeText: String
    ): AppResult<MatchAnalysis>

    suspend fun saveRecord(jobTitle: String, analysis: MatchAnalysis): Long

    fun getHistory(): Flow<List<AnalysisRecord>>

    suspend fun deleteRecord(id: Long)
}

/**
 * Separate contract for API key storage. Kept distinct from ResumeAnalysisRepository
 * because it has a different lifecycle and a different implementation concern
 * (Keystore-backed encryption) that has nothing to do with analysis logic.
 */
interface ApiKeyRepository {
    suspend fun saveApiKey(key: String)
    suspend fun getApiKey(): String?
    suspend fun hasApiKey(): Boolean
    suspend fun clearApiKey()
}

/**
 * Abstraction over "given a file Uri, give me text". Lets the domain layer
 * ask for resume text without knowing whether it came from a pasted string
 * or a parsed PDF.
 */
interface DocumentTextExtractor {
    suspend fun extractText(uriString: String): AppResult<String>
}
