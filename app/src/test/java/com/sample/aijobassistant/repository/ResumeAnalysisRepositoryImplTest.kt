package com.sample.aijobassistant.repository

import com.google.common.truth.Truth.assertThat
import com.sample.aijobassistant.data.local.AnalysisRecordDao
import com.sample.aijobassistant.data.remote.GeminiAnalysisDataSource
import com.sample.aijobassistant.data.repository.ResumeAnalysisRepositoryImpl
import com.sample.aijobassistant.domain.model.AppResult
import com.sample.aijobassistant.domain.model.ErrorType
import com.sample.aijobassistant.domain.model.MatchAnalysis
import com.sample.aijobassistant.domain.repository.ApiKeyRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ResumeAnalysisRepositoryImplTest {

    private lateinit var geminiAnalysisDataSource: GeminiAnalysisDataSource
    private lateinit var analysisRecordDao: AnalysisRecordDao
    private lateinit var apiKeyRepository: ApiKeyRepository
    private lateinit var repository: ResumeAnalysisRepositoryImpl

    private val sampleAnalysis = MatchAnalysis(
        matchScore = 70,
        strengths = listOf("Kotlin"),
        gaps = listOf("Python"),
        suggestions = listOf("Learn FastAPI"),
        summary = "Decent fit."
    )

    @Before
    fun setUp() {
        geminiAnalysisDataSource = mockk()
        analysisRecordDao = mockk()
        apiKeyRepository = mockk()
        repository = ResumeAnalysisRepositoryImpl(
            geminiAnalysisDataSource,
            apiKeyRepository
        )
    }

    @Test
    fun `analyzeMatch returns MISSING_API_KEY error when no key is stored, never calls Gemini`() = runTest {
        coEvery { apiKeyRepository.getApiKey() } returns null

        val result = repository.analyzeMatch("JD text", "Resume text")

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat((result as AppResult.Error).type).isEqualTo(ErrorType.MISSING_API_KEY)
        coVerify(exactly = 0) { geminiAnalysisDataSource.analyze(any(), any(), any()) }
    }

    @Test
    fun `analyzeMatch passes stored key through to the Gemini data source`() = runTest {
        coEvery { apiKeyRepository.getApiKey() } returns "test-key-123"
        coEvery {
            geminiAnalysisDataSource.analyze("test-key-123", "JD text", "Resume text")
        } returns AppResult.Success(sampleAnalysis)

        val result = repository.analyzeMatch("JD text", "Resume text")

        assertThat(result).isEqualTo(AppResult.Success(sampleAnalysis))
    }
}
