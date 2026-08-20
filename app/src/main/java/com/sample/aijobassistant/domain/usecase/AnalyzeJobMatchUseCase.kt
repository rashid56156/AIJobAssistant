package com.sample.aijobassistant.domain.usecase

import com.sample.aijobassistant.domain.model.AppResult
import com.sample.aijobassistant.domain.model.ErrorType
import com.sample.aijobassistant.domain.model.MatchAnalysis
import com.sample.aijobassistant.domain.repository.ApiKeyRepository
import com.sample.aijobassistant.domain.repository.ResumeAnalysisRepository
import javax.inject.Inject

/**
 * Single-purpose use case: given a job description and resume text, produce
 * a MatchAnalysis. It checks API key presence here rather than letting the
 * repository silently fail — that keeps the "no key configured" UX decision
 * in the domain layer where it belongs, not buried in a network client.
 */
class AnalyzeJobMatchUseCase @Inject constructor(
    private val resumeAnalysisRepository: ResumeAnalysisRepository,
    private val apiKeyRepository: ApiKeyRepository
) {
    suspend operator fun invoke(
        jobDescription: String,
        resumeText: String
    ): AppResult<MatchAnalysis> {
        if (jobDescription.isBlank()) {
            return AppResult.Error(ErrorType.UNKNOWN, "Job description cannot be empty.")
        }
        if (resumeText.isBlank()) {
            return AppResult.Error(ErrorType.UNKNOWN, "Resume content cannot be empty.")
        }
        if (!apiKeyRepository.hasApiKey()) {
            return AppResult.Error(
                ErrorType.MISSING_API_KEY,
                "No Gemini API key configured. Add one in Settings to run an analysis."
            )
        }

        return resumeAnalysisRepository.analyzeMatch(jobDescription, resumeText)
    }
}
