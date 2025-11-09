package com.rejown.qrcraft.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rejown.qrcraft.presentation.components.BottomNavigationBar

/**
 * Main screen - Container for the bottom navigation experience
 * This screen has its own Scaffold with bottom bar
 * It contains the BottomNavHost (nested navigation for Scanner/Generator/History/Settings)
 *
 * @param parentNavController Parent NavController for navigating outside bottom nav
 * @param initialTab The initial tab to navigate to (0=Scanner, 1=Generator, 2=History, 3=Settings), null=remember last
 */
@Composable
fun MainScreen(
    parentNavController: NavHostController,
    initialTab: Int? = null
) {
    // Local nav controller for bottom nav (nested navigation)
    val bottomNavController = rememberNavController()

    // Navigate to initial tab if specified
    LaunchedEffect(initialTab) {
        if (initialTab != null) {
            val route = when (initialTab) {
                0 -> Screen.Scanner
                1 -> Screen.Generator
                2 -> Screen.History
                3 -> Screen.Settings
                else -> null
            }
            if (route != null) {
                bottomNavController.navigate(route) {
                    popUpTo(bottomNavController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    val currentBackStackEntry by bottomNavController.currentBackStackEntryAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNavController
            )
        }
    ) { innerPadding ->
        BottomNavHost(
            navController = bottomNavController,
            parentNavController = parentNavController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
