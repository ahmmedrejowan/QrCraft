package com.rejown.qrcraft.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Scanner.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Scanner.route) {
            com.rejown.qrcraft.presentation.scanner.ScannerScreen()
        }

        composable(Screen.Generator.route) {
            com.rejown.qrcraft.presentation.generator.GeneratorScreen()
        }

        composable(Screen.History.route) {
            com.rejown.qrcraft.presentation.history.HistoryScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            com.rejown.qrcraft.presentation.settings.SettingsScreen(navController = navController)
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("type") { type = NavType.StringType }
            ),
            enterTransition = { slideIntoContainer() },
            exitTransition = { slideOutOfContainer() }
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            val type = backStackEntry.arguments?.getString("type") ?: ""
            com.rejown.qrcraft.presentation.detail.DetailScreen(
                id = id,
                type = type,
                navController = navController
            )
        }
    }
}

/**
 * Slide in from right animation
 */
private fun slideIntoContainer(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth }
    ) + fadeIn()
}

/**
 * Slide out to left animation
 */
private fun slideOutOfContainer(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth }
    ) + fadeOut()
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
