package com.sample.aijobassistant.domain.model

/**
 * Pure domain model — no Gemini SDK types, no Room annotations here.
 * This is what the UI layer and use cases work with. Keeping it framework-free
 * is what makes this layer testable without mocking Android or network classes.
 */
data class MatchAnalysis(
    val matchScore: Int, // 0-100
    val strengths: List<String>,
    val gaps: List<String>,
    val suggestions: List<String>,
    val summary: String
)

/**
 * Represents one saved analysis run, shown in history.
 * Distinct from MatchAnalysis because this carries persistence concerns
 * (id, timestamp) that the analysis itself shouldn't know about.
 */
data class AnalysisRecord(
    val id: Long = 0,
    val jobTitle: String,
    val timestamp: Long,
    val analysis: MatchAnalysis
)
