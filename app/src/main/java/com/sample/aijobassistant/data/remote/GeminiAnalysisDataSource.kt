package com.sample.aijobassistant.data.remote

import com.sample.aijobassistant.domain.model.AppResult
import com.sample.aijobassistant.domain.model.ErrorType
import com.sample.aijobassistant.domain.model.MatchAnalysis
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Talks to the Gemini API directly over REST using the user's own API key.
 * Knows nothing about Room, the UI, or use cases — takes raw text in,
 * returns a MatchAnalysis or a typed error out. Prompt construction lives
 * here so there's exactly one place to tune it.
 */
@Singleton
class GeminiAnalysisDataSource @Inject constructor(
    private val geminiApi: GeminiApi
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun analyze(
        apiKey: String,
        jobDescription: String,
        resumeText: String
    ): AppResult<MatchAnalysis> {
        return try {
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(buildPrompt(jobDescription, resumeText))))),
                generationConfig = GenerationConfig(temperature = 0.3f, responseMimeType = "application/json")
            )

            val response = geminiApi.generateContent(
                model = GeminiApi.DEFAULT_MODEL,
                apiKey = apiKey,
                request = request
            )

            val rawText = response.candidates.firstOrNull()
                ?.content?.parts?.firstOrNull()
                ?.text

            if (rawText.isNullOrBlank()) {
                AppResult.Error(ErrorType.INVALID_RESPONSE, "Gemini returned an empty response.")
            } else {
                parseResponse(rawText)
            }
        } catch (e: HttpException) {
            mapHttpExceptionToResult(e)
        } catch (e: IOException) {
            AppResult.Error(ErrorType.NETWORK, "Network error while reaching Gemini. Check your connection and try again.")
        } catch (e: Exception) {
            AppResult.Error(ErrorType.UNKNOWN, e.message ?: "Something went wrong during analysis.")
        }
    }

    private fun parseResponse(rawText: String): AppResult<MatchAnalysis> {
        return try {
            val dto = json.decodeFromString<GeminiAnalysisDto>(stripMarkdownFence(rawText))
            AppResult.Success(dto.toDomain())
        } catch (e: Exception) {
            AppResult.Error(
                ErrorType.INVALID_RESPONSE,
                "Could not parse the model's response. Try again."
            )
        }
    }

    private fun stripMarkdownFence(text: String): String {
        val trimmed = text.trim()
        if (!trimmed.startsWith("```")) return trimmed

        return trimmed
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private fun mapHttpExceptionToResult(e: HttpException): AppResult.Error {
        return when (e.code()) {
            400, 401, 403 -> AppResult.Error(
                ErrorType.MISSING_API_KEY,
                "Your Gemini API key was rejected. Double-check it in Settings."
            )
            429 -> AppResult.Error(
                ErrorType.NETWORK,
                "Rate limit reached on your Gemini API key. Wait a moment and try again."
            )
            else -> AppResult.Error(
                ErrorType.UNKNOWN,
                "Gemini API request failed (HTTP ${e.code()})."
            )
        }
    }

    private fun buildPrompt(jobDescription: String, resumeText: String): String = """
        You are an expert technical recruiter and resume reviewer.

        Compare the RESUME against the JOB DESCRIPTION below and evaluate fit.

        Respond with ONLY valid JSON matching exactly this schema, no markdown, no commentary:
        {
          "matchScore": <integer 0-100>,
          "strengths": [<short strings, max 5>],
          "gaps": [<short strings, max 5>],
          "suggestions": [<short actionable strings, max 5>],
          "summary": "<one or two sentence overall summary>"
        }

        Be specific and reference actual skills/requirements mentioned in the job description.
        Do not invent experience the resume does not support.

        JOB DESCRIPTION:
        $jobDescription

        RESUME:
        $resumeText
    """.trimIndent()
}

@Serializable
private data class GeminiAnalysisDto(
    val matchScore: Int,
    val strengths: List<String> = emptyList(),
    val gaps: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val summary: String = ""
) {
    fun toDomain() = MatchAnalysis(
        matchScore = matchScore.coerceIn(0, 100),
        strengths = strengths,
        gaps = gaps,
        suggestions = suggestions,
        summary = summary
    )
}
