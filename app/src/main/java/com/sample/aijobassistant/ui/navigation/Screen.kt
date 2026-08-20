package com.sample.aijobassistant.ui.navigation

/**
 * Type-safe route definitions. Result screen takes the saved record's id as
 * an argument so it can be reached either right after a fresh analysis or
 * later from History — both paths just load the same record by id.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Settings : Screen("settings")
    data object History : Screen("history")
    data object Result : Screen("result/{recordId}") {
        fun createRoute(recordId: Long) = "result/$recordId"
        const val ARG_RECORD_ID = "recordId"
    }
}
