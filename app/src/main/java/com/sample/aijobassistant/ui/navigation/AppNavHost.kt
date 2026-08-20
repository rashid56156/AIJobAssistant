package com.sample.aijobassistant.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sample.aijobassistant.ui.screens.history.HistoryScreen
import com.sample.aijobassistant.ui.screens.home.HomeScreen
import com.sample.aijobassistant.ui.screens.result.ResultScreen
import com.sample.aijobassistant.ui.screens.settings.SettingsScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onAnalysisComplete = { recordId ->
                    navController.navigate(Screen.Result.createRoute(recordId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onRecordClick = { recordId ->
                    navController.navigate(Screen.Result.createRoute(recordId))
                }
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(navArgument(Screen.Result.ARG_RECORD_ID) { type = NavType.LongType })
        ) {
            ResultScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
