package com.rejown.qrcraft.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.rejown.qrcraft.presentation.generator.creation.CreationScreen
import com.rejown.qrcraft.presentation.generator.details.CodeDetailScreen
import com.rejown.qrcraft.presentation.scanner.details.ScanDetailScreen
import com.rejown.qrcraft.presentation.scanner.details.ScanHistoryDetailViewModel
import org.koin.androidx.compose.koinViewModel

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
        // ============ GENERATOR FLOW SCREENS ============

        composable<Screen.Creation> { backStackEntry ->
            val creation = backStackEntry.toRoute<Screen.Creation>()
            val isEditMode = creation.codeId != null

            CreationScreen(
                templateId = creation.templateId,
                codeId = creation.codeId, // For edit mode
                onSaved = { codeId ->
                    if (isEditMode) {
                        // Edit mode: Just pop back to the existing CodeDetails screen
                        navController.popBackStack()
                    } else {
                        // Create mode: Navigate to new CodeDetails and clear creation screen from stack
                        navController.navigate(Screen.CodeDetails(codeId)) {
                            popUpTo<Screen.Main> { inclusive = false }
                        }
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
                    // Navigate to creation screen in edit mode
                    navController.navigate(Screen.Creation(
                        templateId = templateId,
                        codeId = codeDetails.codeId
                    ))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.ScanDetail> { backStackEntry ->
            val scanDetail = backStackEntry.toRoute<Screen.ScanDetail>()
            timber.log.Timber.tag("QC QRCraftNavHost").d("composable - ScanDetail with data: ${scanDetail.displayValue}")

            // Reconstruct ScanResult from navigation arguments
            val scanResult = com.rejown.qrcraft.domain.models.ScanResult(
                rawValue = scanDetail.rawValue,
                displayValue = scanDetail.displayValue,
                format = com.rejown.qrcraft.domain.models.BarcodeFormat.valueOf(scanDetail.format),
                contentType = com.rejown.qrcraft.domain.models.ContentType.valueOf(scanDetail.contentType),
                timestamp = scanDetail.timestamp
            )

            ScanDetailScreen(
                scanResult = scanResult,
                onBack = {
                    timber.log.Timber.tag("QC QRCraftNavHost").d("onBack - Back pressed from ScanDetail")
                    navController.popBackStack()
                }
            )
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
                    // Error state - pop back
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
                state.scanResult != null -> {
                    ScanDetailScreen(
                        scanResult = state.scanResult!!,
                        onBack = {
                            navController.popBackStack()
                        },
                        autoSave = false, // Don't auto-save - already in database
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
