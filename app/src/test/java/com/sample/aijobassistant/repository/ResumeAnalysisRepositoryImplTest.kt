package com.sample.aijobassistant.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.sample.aijobassistant.data.local.AnalysisRecordDao
import com.sample.aijobassistant.data.local.AnalysisRecordEntity
import com.sample.aijobassistant.data.remote.GeminiAnalysisDataSource
import com.sample.aijobassistant.data.repository.ResumeAnalysisRepositoryImpl
import com.sample.aijobassistant.domain.model.AppResult
import com.sample.aijobassistant.domain.model.ErrorType
import com.sample.aijobassistant.domain.model.MatchAnalysis
import com.sample.aijobassistant.domain.repository.ApiKeyRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
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
            analysisRecordDao,
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

    @Test
    fun `saveRecord builds an entity with current timestamp and delegates to dao insert`() = runTest {
        coEvery { analysisRecordDao.insert(any()) } returns 42L

        val id = repository.saveRecord("Senior Android Engineer", sampleAnalysis)

        assertThat(id).isEqualTo(42L)
        coVerify(exactly = 1) {
            analysisRecordDao.insert(
                match { entity ->
                    entity.jobTitle == "Senior Android Engineer" &&
                        entity.matchScore == sampleAnalysis.matchScore &&
                        entity.strengths == sampleAnalysis.strengths
                }
            )
        }
    }

    @Test
    fun `getHistory maps entity flow to domain model flow`() = runTest {
        val entity = AnalysisRecordEntity(
            id = 1,
            jobTitle = "Staff Mobile Engineer",
            timestamp = 1_700_000_000_000L,
            matchScore = 88,
            strengths = listOf("Compose"),
            gaps = listOf("Backend"),
            suggestions = listOf("Add a backend project"),
            summary = "Strong fit."
        )
        coEvery { analysisRecordDao.getAll() } returns flowOf(listOf(entity))

        repository.getHistory().test {
            val records = awaitItem()
            assertThat(records).hasSize(1)
            assertThat(records.first().jobTitle).isEqualTo("Staff Mobile Engineer")
            assertThat(records.first().analysis.matchScore).isEqualTo(88)
            awaitComplete()
        }
    }

    @Test
    fun `deleteRecord delegates to dao deleteById`() = runTest {
        coEvery { analysisRecordDao.deleteById(5L) } returns Unit

        repository.deleteRecord(5L)

        coVerify(exactly = 1) { analysisRecordDao.deleteById(5L) }
    }
}
