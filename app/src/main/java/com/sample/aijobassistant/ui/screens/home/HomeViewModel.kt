package com.sample.aijobassistant.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.aijobassistant.domain.model.AppResult
import com.sample.aijobassistant.domain.model.ErrorType
import com.sample.aijobassistant.domain.model.MatchAnalysis
import com.sample.aijobassistant.domain.repository.DocumentTextExtractor
import com.sample.aijobassistant.domain.usecase.AnalyzeJobMatchUseCase
import com.sample.aijobassistant.domain.usecase.SaveAnalysisRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class HomeViewModel @Inject constructor(
    private val analyzeJobMatchUseCase: AnalyzeJobMatchUseCase,
    private val saveAnalysisRecordUseCase: SaveAnalysisRecordUseCase,
    private val documentTextExtractor: DocumentTextExtractor
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onJobDescriptionChanged(value: String) {
        _uiState.value = _uiState.value.copy(jobDescription = value, errorMessage = null)
    }

    fun onResumeTextChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            resumeSource = ResumeSource.PastedText(value),
            errorMessage = null
        )
    }

    fun clearResume() {
        _uiState.value = _uiState.value.copy(resumeSource = ResumeSource.None)
    }

    /**
     * Called when the user picks a PDF via the system document picker. The
     * Uri's permission is read-once (granted by the picker contract), so we
     * extract text immediately rather than re-reading the file later when
     * analyze() runs — by then the grant may no longer be valid.
     */
    fun onPdfSelected(uriString: String, fileName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExtractingPdf = true, errorMessage = null)

            when (val result = documentTextExtractor.extractText(uriString)) {
                is AppResult.Success -> {
                    extractedPdfText = result.data
                    _uiState.value = _uiState.value.copy(
                        isExtractingPdf = false,
                        resumeSource = ResumeSource.UploadedPdf(uriString, fileName)
                    )
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isExtractingPdf = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    // Holds the text extracted from a selected PDF. Kept outside StateFlow
    // since it's plumbing for analyze() rather than something the UI renders;
    // the UI only needs to know a PDF was selected (file name) and whether
    // extraction is in progress.
    private var extractedPdfText: String? = null

    fun analyze() {
        val state = _uiState.value
        val resumeText = when (val source = state.resumeSource) {
            is ResumeSource.PastedText -> source.text
            is ResumeSource.UploadedPdf -> extractedPdfText.orEmpty()
            ResumeSource.None -> ""
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true, errorMessage = null, needsApiKey = false)

            val result = analyzeJobMatchUseCase(state.jobDescription, resumeText)

            when (result) {
                is AppResult.Success -> {
                    val recordId = saveAnalysisRecordUseCase(
                        jobTitle = deriveJobTitle(state.jobDescription),
                        analysis = result.data
                    )
                    _uiState.value = _uiState.value.copy(isAnalyzing = false, completedRecordId = recordId)
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        errorMessage = result.message,
                        needsApiKey = result.type == ErrorType.MISSING_API_KEY
                    )
                }
            }
        }
    }

    fun consumeNavigationEvent() {
        _uiState.value = _uiState.value.copy(completedRecordId = null)
    }

    /** Best-effort first line as a human-readable title for history entries. */
    private fun deriveJobTitle(jobDescription: String): String =
        jobDescription.lineSequence().firstOrNull { it.isNotBlank() }
            ?.take(60)
            ?: "Untitled role"
}
