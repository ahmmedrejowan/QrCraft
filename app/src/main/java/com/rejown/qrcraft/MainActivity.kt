package com.rejown.qrcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.rejown.qrcraft.data.preferences.PreferencesManager
import com.rejown.qrcraft.presentation.onboarding.OnboardingScreen
import com.rejown.qrcraft.ui.theme.QRCraftTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val preferencesManager: PreferencesManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QRCraftTheme {
                MainScreen(preferencesManager = preferencesManager)
            }
        }
    }
}

@Composable
fun MainScreen(preferencesManager: PreferencesManager) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val isOnboardingCompleted by preferencesManager.isOnboardingCompleted.collectAsState(initial = null)

    // Show nothing until we know the actual value from DataStore
    when (isOnboardingCompleted) {
        null -> {
            // Loading state - show blank screen to prevent flash
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }
        false -> {
            // Show onboarding
            OnboardingScreen(
                onFinish = {
                    scope.launch {
                        preferencesManager.setOnboardingCompleted()
                    }
                }
            )
        }
        true -> {
            // Show main app with new navigation structure
            com.rejown.qrcraft.presentation.navigation.QRCraftNavHost(
                navController = navController
            )
        }
    }
}