package com.sample.aijobassistant.ui.screens.home

data class HomeUiState(
    val jobDescription: String = "",
    val resumeSource: ResumeSource = ResumeSource.None,
    val extractedPdfText: String? = null,
    val isExtractingPdf: Boolean = false,
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null,
    val needsApiKey: Boolean = false
)