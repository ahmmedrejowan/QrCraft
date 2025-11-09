package com.rejown.qrcraft.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.rejown.qrcraft.presentation.detail.DetailScreen
import com.rejown.qrcraft.presentation.generator.creation.CreationScreen
import com.rejown.qrcraft.presentation.generator.details.CodeDetailScreen
import com.rejown.qrcraft.presentation.scanner.ScannerViewModel
import com.rejown.qrcraft.presentation.scanner.details.ScanDetailScreen
import com.rejown.qrcraft.presentation.scanner.details.ScanHistoryDetailViewModel
import com.rejown.qrcraft.presentation.scanner.state.ScannerEvent
import com.rejown.qrcraft.presentation.scanner.state.ScannerState
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment

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

        composable<Screen.ScanDetail> {
            timber.log.Timber.tag("QRCraft QRCraftNavHost").e("composable - Composing ScanDetail screen")

            // Get the SAME ScannerViewModel instance (singleton)
            val scannerViewModel: ScannerViewModel = koinInject()
            val state by scannerViewModel.state.collectAsState()

            timber.log.Timber.tag("QRCraft QRCraftNavHost").e("composable - ScanDetail current state: ${state::class.simpleName}")

            when (state) {
                is ScannerState.Success -> {
                    val scanResult = (state as ScannerState.Success).result
                    timber.log.Timber.tag("QRCraft QRCraftNavHost").e("composable - Showing detail for: ${scanResult.displayValue}")
                    ScanDetailScreen(
                        scanResult = scanResult,
                        onBack = {
                            timber.log.Timber.tag("QRCraft QRCraftNavHost").e("onBack - Back pressed from ScanDetail")
                            // Reset scanner state before navigating back
                            // This ensures camera is ready when we return to scanner screen
                            scannerViewModel.onEvent(ScannerEvent.StartScanning)
                            navController.navigate(Screen.Main(initialTab = 0)) {
                                popUpTo<Screen.Main> { inclusive = true }
                            }
                            timber.log.Timber.tag("QRCraft QRCraftNavHost").e("onBack - Navigation back to Main executed")
                        }
                    )
                }
                else -> {
                    timber.log.Timber.tag("QRCraft QRCraftNavHost").e("composable - State is not Success (${state::class.simpleName}), navigating back")
                    // If state is not Success (shouldn't happen), navigate back
                    LaunchedEffect(Unit) {
                        timber.log.Timber.tag("QRCraft QRCraftNavHost").e("LaunchedEffect - Executing fallback navigation to Main")
                        navController.navigate(Screen.Main(initialTab = 0)) {
                            popUpTo<Screen.Main> { inclusive = true }
                        }
                    }
                }
            }
        }

        composable<Screen.ScanHistoryDetail> { backStackEntry ->
            val scanHistoryDetail = backStackEntry.toRoute<Screen.ScanHistoryDetail>()
            val viewModel: ScanHistoryDetailViewModel = koinViewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(scanHistoryDetail.scanId) {
                viewModel.loadScan(scanHistoryDetail.scanId)
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    // Error state - navigate back
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Main(initialTab = 2)) {
                            popUpTo<Screen.Main> { inclusive = true }
                        }
                    }
                }
                state.scanResult != null -> {
                    ScanDetailScreen(
                        scanResult = state.scanResult!!,
                        onBack = {
                            navController.navigate(Screen.Main(initialTab = 2)) {
                                popUpTo<Screen.Main> { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
