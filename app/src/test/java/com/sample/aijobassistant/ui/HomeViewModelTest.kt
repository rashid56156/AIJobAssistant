package com.sample.aijobassistant.ui

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.sample.aijobassistant.domain.model.AppResult
import com.sample.aijobassistant.domain.model.ErrorType
import com.sample.aijobassistant.domain.model.MatchAnalysis
import com.sample.aijobassistant.domain.repository.DocumentTextExtractor
import com.sample.aijobassistant.domain.usecase.AnalyzeJobMatchUseCase
import com.sample.aijobassistant.domain.usecase.SaveAnalysisRecordUseCase
import com.sample.aijobassistant.ui.screens.home.HomeViewModel
import com.sample.aijobassistant.ui.screens.home.ResumeSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * The interesting part of this ViewModel isn't the happy path values - it's
 * the *sequence* of states emitted: idle -> analyzing -> success/error.
 * Turbine's `test {}` block is what makes asserting that sequence readable;
 * without it you'd be manually collecting into a list and racing coroutines.
 *
 * Dispatchers.Main is swapped for a StandardTestDispatcher in setUp/tearDown
 * because viewModelScope hardcodes Dispatchers.Main.immediate internally -
 * without this swap, runTest's virtual time and the ViewModel's coroutines
 * would be running on different schedulers and the test would hang or flake.
 */
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var analyzeJobMatchUseCase: AnalyzeJobMatchUseCase
    private lateinit var saveAnalysisRecordUseCase: SaveAnalysisRecordUseCase
    private lateinit var documentTextExtractor: DocumentTextExtractor
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        analyzeJobMatchUseCase = mockk()
        saveAnalysisRecordUseCase = mockk()
        documentTextExtractor = mockk()
        viewModel =
            HomeViewModel(analyzeJobMatchUseCase, saveAnalysisRecordUseCase, documentTextExtractor)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `analyze emits analyzing state then success state and navigation event`() = runTest {
        val analysis = MatchAnalysis(
            matchScore = 90,
            strengths = listOf("Kotlin", "Compose"),
            gaps = emptyList(),
            suggestions = listOf("None needed"),
            summary = "Excellent fit."
        )
        coEvery { analyzeJobMatchUseCase(any(), any()) } returns AppResult.Success(analysis)
        coEvery { saveAnalysisRecordUseCase(any(), any()) } returns 7L

        viewModel.onJobDescriptionChanged("Senior Android Engineer role")
        viewModel.onResumeTextChanged("10 years Kotlin and Android")

        launch {
            viewModel.navigationEvent.test {
                val id = awaitItem()
                assertThat(id).isEqualTo(7L)
            }
        }

        viewModel.uiState.test {
            // Consume the state already produced by the two onChanged calls above.
            awaitItem()

            viewModel.analyze()

            val analyzing = awaitItem()
            assertThat(analyzing.isAnalyzing).isTrue()

            val completed = awaitItem()
            assertThat(completed.isAnalyzing).isFalse()
        }
    }

    @Test
    fun `analyze with missing api key error sets needsApiKey flag for the UI to react to`() = runTest {
        coEvery { analyzeJobMatchUseCase(any(), any()) } returns AppResult.Error(
            ErrorType.MISSING_API_KEY,
            "No Gemini API key configured."
        )

        viewModel.onJobDescriptionChanged("Senior Android Engineer role")
        viewModel.onResumeTextChanged("10 years Kotlin and Android")

        viewModel.uiState.test {
            awaitItem() // from onResumeTextChanged

            viewModel.analyze()

            awaitItem() // isAnalyzing = true

            val errorState = awaitItem()
            assertThat(errorState.isAnalyzing).isFalse()
            assertThat(errorState.needsApiKey).isTrue()
            assertThat(errorState.errorMessage).isEqualTo("No Gemini API key configured.")
        }
    }

    @Test
    fun `onPdfSelected success populates resumeSource as UploadedPdf and clears extracting flag`() = runTest {
        coEvery { documentTextExtractor.extractText("content://resume.pdf") } returns
            AppResult.Success("Extracted resume text")

        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.onPdfSelected("content://resume.pdf", "resume.pdf")

            val extracting = awaitItem()
            assertThat(extracting.isExtractingPdf).isTrue()

            val done = awaitItem()
            assertThat(done.isExtractingPdf).isFalse()
            assertThat(done.resumeSource).isInstanceOf(ResumeSource.UploadedPdf::class.java)
            assertThat((done.resumeSource as ResumeSource.UploadedPdf).fileName).isEqualTo("resume.pdf")
        }
    }

    @Test
    fun `onPdfSelected failure surfaces errorMessage and does not set resumeSource`() = runTest {
        coEvery { documentTextExtractor.extractText("content://bad.pdf") } returns
            AppResult.Error(ErrorType.PDF_PARSE_FAILURE, "Could not read this PDF.")

        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.onPdfSelected("content://bad.pdf", "bad.pdf")

            awaitItem() // isExtractingPdf = true

            val errorState = awaitItem()
            assertThat(errorState.isExtractingPdf).isFalse()
            assertThat(errorState.errorMessage).isEqualTo("Could not read this PDF.")
            assertThat(errorState.resumeSource).isEqualTo(ResumeSource.None)
        }
    }
}
