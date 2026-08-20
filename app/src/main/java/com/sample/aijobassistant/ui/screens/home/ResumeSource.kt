package com.sample.aijobassistant.ui.screens.home

/**
 * Resume can come from either source; only one is active at a time. Modeled
 * as a sealed type rather than two nullable strings so the UI and ViewModel
 * can't end up in an inconsistent state where both or neither are "set".
 */
sealed class ResumeSource {
    data object None : ResumeSource()
    data class PastedText(val text: String) : ResumeSource()
    data class UploadedPdf(val uriString: String, val fileName: String) : ResumeSource()
}