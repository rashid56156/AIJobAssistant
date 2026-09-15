package com.sample.aijobassistant.domain.usecase

import com.sample.aijobassistant.domain.model.AppResult
import com.sample.aijobassistant.domain.model.ErrorType
import com.sample.aijobassistant.domain.model.MatchAnalysis
import com.sample.aijobassistant.domain.repository.ResumeAnalysisRepository
import javax.inject.Inject

/**
 * Analyzes a job description against resume text.
 * Validates the inputs and delegates the actual analysis
 * to the repository.
 */
class AnalyzeJobMatchUseCase @Inject constructor(
    private val resumeAnalysisRepository: ResumeAnalysisRepository
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
        return resumeAnalysisRepository.analyzeMatch(jobDescription, resumeText)
    }
}
