package com.rejown.qrcraft.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.rejown.qrcraft.presentation.detail.DetailScreen
import com.rejown.qrcraft.presentation.generator.creation.CreationScreen
import com.rejown.qrcraft.presentation.generator.details.CodeDetailScreen

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

        composable<Screen.Detail> { backStackEntry ->
            val detail = backStackEntry.toRoute<Screen.Detail>()
            DetailScreen(
                id = detail.id,
                type = detail.type,
                navController = navController
            )
        }

        // ============ GENERATOR FLOW SCREENS (To be implemented) ============

        composable<Screen.Creation> { backStackEntry ->
            val creation = backStackEntry.toRoute<Screen.Creation>()
            CreationScreen(
                templateId = creation.templateId,
                onSaved = { codeId ->
                    navController.navigate(Screen.CodeDetails(codeId)) {
                        popUpTo<Screen.Generator> { inclusive = false }
                    }
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.CodeDetails> { backStackEntry ->
            val codeDetails = backStackEntry.toRoute<Screen.CodeDetails>()
            CodeDetailScreen(
                codeId = codeDetails.codeId,
                onEdit = { templateId ->
                    // TODO: Implement edit functionality
                    // Would need to load existing data into creation screen
                },
                onBack = {
                    navController.navigate(Screen.Main(initialTab = 1)) {
                        popUpTo<Screen.Main> { inclusive = true }
                    }
                }
            )
        }
    }
}
