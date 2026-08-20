package com.sample.aijobassistant.domain.usecase

import com.sample.aijobassistant.domain.model.AnalysisRecord
import com.sample.aijobassistant.domain.model.MatchAnalysis
import com.sample.aijobassistant.domain.repository.ApiKeyRepository
import com.sample.aijobassistant.domain.repository.ResumeAnalysisRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SaveApiKeyUseCase @Inject constructor(
    private val apiKeyRepository: ApiKeyRepository
) {
    suspend operator fun invoke(key: String) {
        val trimmed = key.trim()
        require(trimmed.isNotEmpty()) { "API key cannot be blank." }
        apiKeyRepository.saveApiKey(trimmed)
    }
}

class GetApiKeyStatusUseCase @Inject constructor(
    private val apiKeyRepository: ApiKeyRepository
) {
    suspend operator fun invoke(): Boolean = apiKeyRepository.hasApiKey()
}

class ClearApiKeyUseCase @Inject constructor(
    private val apiKeyRepository: ApiKeyRepository
) {
    suspend operator fun invoke() = apiKeyRepository.clearApiKey()
}

class SaveAnalysisRecordUseCase @Inject constructor(
    private val resumeAnalysisRepository: ResumeAnalysisRepository
) {
    suspend operator fun invoke(jobTitle: String, analysis: MatchAnalysis): Long {
        val title = jobTitle.ifBlank { "Untitled role" }
        return resumeAnalysisRepository.saveRecord(title, analysis)
    }
}

class GetHistoryUseCase @Inject constructor(
    private val resumeAnalysisRepository: ResumeAnalysisRepository
) {
    operator fun invoke(): Flow<List<AnalysisRecord>> = resumeAnalysisRepository.getHistory()
}

/**
 * Result screen is reached two ways (fresh analysis, or tapping a History
 * item) and both need the same record by id. Rather than add a dedicated
 * "get by id" DAO query + repository method for what's a small, already-
 * in-memory list, this reuses getHistory() and filters. Simpler contract,
 * one fewer DB query method to maintain, and history lists are small
 * (single user's local data) so the cost is negligible.
 */
class GetAnalysisRecordByIdUseCase @Inject constructor(
    private val resumeAnalysisRepository: ResumeAnalysisRepository
) {
    operator fun invoke(id: Long): Flow<AnalysisRecord?> =
        resumeAnalysisRepository.getHistory().map { records -> records.firstOrNull { it.id == id } }
}

class DeleteAnalysisRecordUseCase @Inject constructor(
    private val resumeAnalysisRepository: ResumeAnalysisRepository
) {
    suspend operator fun invoke(id: Long) = resumeAnalysisRepository.deleteRecord(id)
}
