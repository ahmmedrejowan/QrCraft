package com.rejown.qrcraft.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rejown.qrcraft.presentation.navigation.Screen

/**
 * Bottom navigation items with Material Icons
 */
sealed class BottomNavItem(
    val route: Screen,
    val icon: ImageVector,
    val label: String
) {
    data object Scanner : BottomNavItem(
        route = Screen.Scanner,
        icon = Icons.Default.QrCodeScanner,
        label = "Scan"
    )

    data object Generator : BottomNavItem(
        route = Screen.Generator,
        icon = Icons.Default.Create,
        label = "Generate"
    )

    data object History : BottomNavItem(
        route = Screen.History,
        icon = Icons.Default.History,
        label = "History"
    )

    data object Settings : BottomNavItem(
        route = Screen.Settings,
        icon = Icons.Default.Settings,
        label = "Settings"
    )
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Scanner,
        BottomNavItem.Generator,
        BottomNavItem.History,
        BottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        items.forEach { item ->
            // Check if current destination matches this item's route
            val isSelected = when (item.route) {
                is Screen.Scanner -> currentDestination?.route?.contains("Scanner") == true
                is Screen.Generator -> currentDestination?.route?.contains("Generator") == true
                is Screen.History -> currentDestination?.route?.contains("History") == true
                is Screen.Settings -> currentDestination?.route?.contains("Settings") == true
                else -> false
            }

            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
