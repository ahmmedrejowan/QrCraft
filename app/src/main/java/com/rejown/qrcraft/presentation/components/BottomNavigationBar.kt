package com.rejown.qrcraft.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rejown.qrcraft.presentation.navigation.Screen

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    data object Scanner : BottomNavItem(
        route = Screen.Scanner.route,
        icon = Icons.Default.QrCodeScanner,
        label = "Scan"
    )

    data object Generator : BottomNavItem(
        route = Screen.Generator.route,
        icon = Icons.Default.Create,
        label = "Generate"
    )

    data object History : BottomNavItem(
        route = Screen.History.route,
        icon = Icons.Default.History,
        label = "History"
    )

    data object Settings : BottomNavItem(
        route = Screen.Settings.route,
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
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
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
