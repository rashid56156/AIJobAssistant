package com.sample.aijobassistant.data.local

import android.content.Context
import com.sample.aijobassistant.domain.model.AppResult
import com.sample.aijobassistant.domain.model.ErrorType
import com.sample.aijobassistant.domain.repository.DocumentTextExtractor
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extracts plain text from a PDF the user picked via the system file picker.
 * Takes the Uri as a String (not android.net.Uri) at the interface boundary
 * so the domain layer's contract stays framework-free; this implementation
 * is the only place that re-parses it into a real Uri and touches the
 * ContentResolver.
 *
 * PDFBoxResourceLoader.init() must have already run (done once in
 * JobAssistantApp.onCreate) before this is ever called.
 */
@Singleton
class PdfDocumentTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) : DocumentTextExtractor {

    override suspend fun extractText(uriString: String): AppResult<String> = withContext(Dispatchers.IO) {
        try {
            val uri = android.net.Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext AppResult.Error(
                    ErrorType.PDF_PARSE_FAILURE,
                    "Could not open the selected file."
                )

            inputStream.use { stream ->
                PDDocument.load(stream).use { document ->
                    val text = PDFTextStripper().getText(document)
                    if (text.isBlank()) {
                        AppResult.Error(
                            ErrorType.PDF_PARSE_FAILURE,
                            "No readable text found in this PDF. It may be a scanned image — try pasting the text instead."
                        )
                    } else {
                        AppResult.Success(text.trim())
                    }
                }
            }
        } catch (e: Exception) {
            AppResult.Error(
                ErrorType.PDF_PARSE_FAILURE,
                "Failed to read the PDF: ${e.message ?: "unknown error"}"
            )
        }
    }
}
