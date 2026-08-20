package com.sample.aijobassistant.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Direct REST interface to the Gemini API (generativelanguage.googleapis.com).
 *
 * Deliberately a plain Retrofit interface, not a vendor SDK. This is the
 * entire surface area of "talking to Gemini" for this app — anyone auditing
 * the open-source repo can read this one file and know exactly what data
 * leaves the device and where it goes.
 *
 * Auth uses the `x-goog-api-key` header (the current standard for the Gemini
 * Developer API), not a query parameter — keeps the key out of logs that
 * capture URLs but not headers.
 */
interface GeminiApi {

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/"
        const val DEFAULT_MODEL = "gemini-2.5-flash"
    }
}

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig
)

@Serializable
data class Content(
    val role: String = "user",
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GenerationConfig(
    val temperature: Float = 0.3f,
    val responseMimeType: String = "application/json"
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate> = emptyList()
)

@Serializable
data class Candidate(
    val content: Content? = null
)
