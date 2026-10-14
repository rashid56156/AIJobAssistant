package com.sample.aijobassistant.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.sample.aijobassistant.data.local.AnalysisRecordDao
import com.sample.aijobassistant.data.local.AnalysisRecordEntity
import com.sample.aijobassistant.data.remote.GeminiAnalysisDataSource
import com.sample.aijobassistant.data.repository.HistoryAnalysisRepositoryImpl
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

class HistoryAnalysisRepositoryImplTest {

    private lateinit var geminiAnalysisDataSource: GeminiAnalysisDataSource
    private lateinit var analysisRecordDao: AnalysisRecordDao
    private lateinit var apiKeyRepository: ApiKeyRepository
    private lateinit var repository: HistoryAnalysisRepositoryImpl

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
        repository = HistoryAnalysisRepositoryImpl(
            analysisRecordDao
        )
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
