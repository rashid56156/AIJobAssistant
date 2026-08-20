package com.sample.aijobassistant.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.sample.aijobassistant.domain.model.AppResult
import com.sample.aijobassistant.domain.model.ErrorType
import com.sample.aijobassistant.domain.model.MatchAnalysis
import com.sample.aijobassistant.domain.repository.ApiKeyRepository
import com.sample.aijobassistant.domain.repository.ResumeAnalysisRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * This use case has three branches worth testing independently:
 * blank job description, blank resume, and missing API key - each should
 * short-circuit before ever calling the repository. The fourth test proves
 * the happy path actually delegates through.
 *
 * Notice none of these tests touch the network or Room - that's the whole
 * point of putting the validation logic in a use case rather than directly
 * in the ViewModel or repository: it's testable in complete isolation.
 */
class AnalyzeJobMatchUseCaseTest {

    private lateinit var resumeAnalysisRepository: ResumeAnalysisRepository
    private lateinit var apiKeyRepository: ApiKeyRepository
    private lateinit var useCase: AnalyzeJobMatchUseCase

    @Before
    fun setUp() {
        resumeAnalysisRepository = mockk()
        apiKeyRepository = mockk()
        useCase = AnalyzeJobMatchUseCase(resumeAnalysisRepository, apiKeyRepository)
    }

    @Test
    fun blank_job_description_returns_error_without_touching_repository() = runTest {
        val result = useCase(jobDescription = "  ", resumeText = "Some resume text")

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        coVerify(exactly = 0) { resumeAnalysisRepository.analyzeMatch(any(), any()) }
    }

    @Test
    fun `blank resume text returns error without touching repository`() = runTest {
        val result = useCase(jobDescription = "Senior Android Engineer", resumeText = "")

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        coVerify(exactly = 0) { resumeAnalysisRepository.analyzeMatch(any(), any()) }
    }

    @Test
    fun `missing api key returns MISSING_API_KEY error without calling repository`() = runTest {
        coEvery { apiKeyRepository.hasApiKey() } returns false

        val result = useCase(jobDescription = "Senior Android Engineer", resumeText = "10 years Kotlin")

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat((result as AppResult.Error).type).isEqualTo(ErrorType.MISSING_API_KEY)
        coVerify(exactly = 0) { resumeAnalysisRepository.analyzeMatch(any(), any()) }
    }

    @Test
    fun `valid input with api key present delegates to repository and returns its result`() = runTest {
        val expected = MatchAnalysis(
            matchScore = 82,
            strengths = listOf("Strong Kotlin background"),
            gaps = listOf("No Python experience"),
            suggestions = listOf("Highlight architecture experience"),
            summary = "Strong overall fit."
        )
        coEvery { apiKeyRepository.hasApiKey() } returns true
        coEvery {
            resumeAnalysisRepository.analyzeMatch("Senior Android Engineer", "10 years Kotlin")
        } returns AppResult.Success(expected)

        val result = useCase(jobDescription = "Senior Android Engineer", resumeText = "10 years Kotlin")

        assertThat(result).isEqualTo(AppResult.Success(expected))
        coVerify(exactly = 1) {
            resumeAnalysisRepository.analyzeMatch("Senior Android Engineer", "10 years Kotlin")
        }
    }
}
