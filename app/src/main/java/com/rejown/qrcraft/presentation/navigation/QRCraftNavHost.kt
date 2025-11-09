package com.rejown.qrcraft.presentation.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.rejown.qrcraft.presentation.detail.DetailScreen

/**
 * Parent navigation host for the QRCraft app
 * Contains all app-level navigation EXCEPT the bottom nav screens
 * Bottom nav screens (Scanner/Generator/History/Settings) are in BottomNavHost within MainScreen
 *
 * @param navController The NavHostController for app-level navigation
 */
@Composable
fun QRCraftNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main()
    ) {
        // ============ MAIN SCREEN (Container for Bottom Nav) ============
        composable<Screen.Main> { backStackEntry ->
            val mainRoute = backStackEntry.toRoute<Screen.Main>()
            MainScreen(
                parentNavController = navController,
                initialTab = mainRoute.initialTab
            )
        }

        // ============ APP-LEVEL SCREENS ============

        composable<Screen.Detail>(
            enterTransition = { slideIntoContainer() },
            exitTransition = { slideOutOfContainer() }
        ) { backStackEntry ->
            val detail = backStackEntry.toRoute<Screen.Detail>()
            DetailScreen(
                id = detail.id,
                type = detail.type,
                navController = navController
            )
        }

        // ============ GENERATOR FLOW SCREENS (To be implemented) ============

        composable<Screen.Creation>(
            enterTransition = { slideIntoContainer() },
            exitTransition = { slideOutOfContainer() }
        ) { backStackEntry ->
            val creation = backStackEntry.toRoute<Screen.Creation>()
            // TODO: CreationScreen(
            //     templateId = creation.templateId,
            //     onSaved = { codeId ->
            //         navController.navigate(Screen.CodeDetails(codeId)) {
            //             popUpTo<Screen.Main> { inclusive = false }
            //         }
            //     },
            //     onBackPressed = {
            //         navController.popBackStack()
            //     }
            // )
        }

        composable<Screen.CodeDetails>(
            enterTransition = { slideIntoContainer() },
            exitTransition = { slideOutOfContainer() }
        ) { backStackEntry ->
            val codeDetails = backStackEntry.toRoute<Screen.CodeDetails>()
            // TODO: CodeDetailsScreen(
            //     codeId = codeDetails.codeId,
            //     onEdit = { templateId ->
            //         navController.navigate(Screen.Creation(templateId))
            //     },
            //     onBack = {
            //         navController.navigate(Screen.Main(initialTab = 2)) {
            //             popUpTo<Screen.Main> { inclusive = true }
            //         }
            //     }
            // )
        }
    }
}

/**
 * Slide in from right animation
 */
private fun slideIntoContainer() = slideInHorizontally(
    initialOffsetX = { fullWidth -> fullWidth }
)

/**
 * Slide out to left animation
 */
private fun slideOutOfContainer() = slideOutHorizontally(
    targetOffsetX = { fullWidth -> -fullWidth }
)
