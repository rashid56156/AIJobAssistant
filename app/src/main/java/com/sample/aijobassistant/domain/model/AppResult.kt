package com.sample.aijobassistant.domain.model

/**
 * Explicit result wrapper instead of throwing exceptions across layers.
 *
 * Why this matters for this app specifically: Gemini calls can fail in a few
 * distinct, user-meaningful ways (no API key set, network failure, malformed
 * JSON response, rate limit). A sealed type forces every call site to handle
 * each case instead of catching a generic Exception and showing "Something
 * went wrong" — which is exactly the kind of UX detail worth calling out in
 * an interview walkthrough of this project.
 */
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val type: ErrorType, val message: String) : AppResult<Nothing>()
}

enum class ErrorType {
    MISSING_API_KEY,
    NETWORK,
    INVALID_RESPONSE,
    PDF_PARSE_FAILURE,
    UNKNOWN
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}

inline fun <T> AppResult<T>.onError(action: (AppResult.Error) -> Unit): AppResult<T> {
    if (this is AppResult.Error) action(this)
    return this
}
