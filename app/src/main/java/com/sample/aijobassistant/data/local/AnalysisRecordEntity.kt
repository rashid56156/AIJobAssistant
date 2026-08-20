package com.sample.aijobassistant.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.sample.aijobassistant.domain.model.AnalysisRecord
import com.sample.aijobassistant.domain.model.MatchAnalysis
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room entity. Deliberately separate from the domain model [AnalysisRecord] —
 * Room needs flat columns and annotations that have no business being in
 * domain code. The strengths/gaps/suggestions lists are stored as a single
 * JSON-encoded column rather than a separate child table, since this app
 * never queries into those lists individually; it always reads/writes a
 * whole record at once. A normalized table would be over-engineering here.
 */
@Entity(tableName = "analysis_records")
@TypeConverters(AnalysisListConverter::class)
data class AnalysisRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobTitle: String,
    val timestamp: Long,
    val matchScore: Int,
    val strengths: List<String>,
    val gaps: List<String>,
    val suggestions: List<String>,
    val summary: String
)

fun AnalysisRecordEntity.toDomain() = AnalysisRecord(
    id = id,
    jobTitle = jobTitle,
    timestamp = timestamp,
    analysis = MatchAnalysis(
        matchScore = matchScore,
        strengths = strengths,
        gaps = gaps,
        suggestions = suggestions,
        summary = summary
    )
)

fun MatchAnalysis.toEntity(jobTitle: String, timestamp: Long) = AnalysisRecordEntity(
    jobTitle = jobTitle,
    timestamp = timestamp,
    matchScore = matchScore,
    strengths = strengths,
    gaps = gaps,
    suggestions = suggestions,
    summary = summary
)

/**
 * Room can't store List<String> natively — these converters serialize to/from
 * a JSON string for storage. Using kotlinx.serialization here rather than
 * Gson/Moshi since the rest of the app already depends on it for the Gemini
 * response parsing; no reason to pull in a second JSON library for this.
 */
class AnalysisListConverter {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromList(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun toList(value: String): List<String> =
        try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
}
