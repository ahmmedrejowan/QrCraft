package com.rejown.qrcraft.presentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rejown.qrcraft.presentation.generator.TemplateSelectionScreen
import com.rejown.qrcraft.presentation.history.HistoryScreen
import com.rejown.qrcraft.presentation.scanner.ScannerScreen
import com.rejown.qrcraft.presentation.settings.SettingsScreen
import timber.log.Timber
import org.koin.compose.koinInject
import com.rejown.qrcraft.presentation.scanner.ScannerViewModel

/**
 * Bottom navigation graph - Nested NavHost for Scanner/Generator/History/Settings
 * This is contained within MainScreen and handles switching between bottom nav tabs
 *
 * @param navController Nested NavController for bottom nav (local to MainScreen)
 * @param parentNavController Parent NavController for navigating outside bottom nav
 */
@Composable
fun BottomNavHost(
    navController: NavHostController,
    parentNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Scanner,
        modifier = modifier
    ) {
        // ============ SCANNER SCREEN ============
        composable<Screen.Scanner> {
            Timber.tag("QRCraft BottomNavHost").e("composable - Composing Scanner screen")

            // Get the scanner ViewModel to access scan result
            val scannerViewModel: ScannerViewModel = koinInject()

            ScannerScreen(
                viewModel = scannerViewModel,
                onNavigateToDetail = {
                    Timber.tag("QRCraft BottomNavHost").e("onNavigateToDetail - Callback triggered")

                    // Get the scan result from the ViewModel state
                    val currentState = scannerViewModel.state.value
                    if (currentState.scanningState is com.rejown.qrcraft.presentation.scanner.state.ScanningState.Success) {
                        val result = (currentState.scanningState as com.rejown.qrcraft.presentation.scanner.state.ScanningState.Success).result
                        Timber.tag("QRCraft BottomNavHost").e("onNavigateToDetail - Navigating with data: ${result.displayValue}")

                        parentNavController.navigate(
                            Screen.ScanDetail(
                                rawValue = result.rawValue,
                                displayValue = result.displayValue,
                                format = result.format.name,
                                contentType = result.contentType.name,
                                timestamp = result.timestamp
                            )
                        )
                        Timber.tag("QRCraft BottomNavHost").e("onNavigateToDetail - Navigation executed")
                    } else {
                        Timber.tag("QRCraft BottomNavHost").e("onNavigateToDetail - ERROR: State is not Success!")
                    }
                }
            )
        }

        // ============ GENERATOR SCREEN ============
        composable<Screen.Generator> {
            TemplateSelectionScreen(
                onTemplateSelected = { templateId ->
                    Timber.tag("TemplateSelection").d("Selected template: $templateId")
                    parentNavController.navigate(Screen.Creation(templateId = templateId))
                }
            )
        }

        // ============ HISTORY SCREEN ============
        composable<Screen.History> {
            HistoryScreen(
                navController = parentNavController  // Use parent for navigating to Detail
            )
        }

        // ============ SETTINGS SCREEN ============
        composable<Screen.Settings> {
            SettingsScreen(
                navController = parentNavController  // Use parent for settings detail screens
            )
        }
    }
}
