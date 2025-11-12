package com.rejown.qrcraft.presentation.settings

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rejown.qrcraft.BuildConfig
import com.rejown.qrcraft.data.local.preferences.ThemePreferences
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val themePreferences: ThemePreferences = koinInject()
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()

    val currentTheme by themePreferences.getTheme().collectAsState(initial = "System")
    val dynamicColorEnabled by themePreferences.isDynamicColorEnabled().collectAsState(initial = false)
    val hapticFeedbackEnabled by themePreferences.isHapticFeedbackEnabled().collectAsState(initial = true)

    var showThemeDialog by remember { mutableStateOf(false) }
    var showClearScanHistoryDialog by remember { mutableStateOf(false) }
    var showClearGeneratedHistoryDialog by remember { mutableStateOf(false) }
    var showClearAllDataDialog by remember { mutableStateOf(false) }
    var showChangelogSheet by remember { mutableStateOf(false) }
    var showPrivacyPolicySheet by remember { mutableStateOf(false) }
    var showLicensesSheet by remember { mutableStateOf(false) }
    var showCreatorSheet by remember { mutableStateOf(false) }
    var showAppLicenseSheet by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar for success/error messages
    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
        state.error?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = androidx.compose.material3.SnackbarDuration.Long
            )
            viewModel.clearMessages()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Stats Section
            SettingsSectionTitle("Statistics")

            StatsCard(
                scanCount = state.scanCount,
                generatedCount = state.generatedCount,
                totalCount = state.totalCount
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Appearance Section
            SettingsSectionTitle("Appearance")

        SettingsCard {
            SettingsClickableItem(
                title = "Theme",
                description = currentTheme,
                onClick = { showThemeDialog = true }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                HorizontalDivider()

                SettingsItem(
                    title = "Dynamic Colors",
                    description = "Use colors from your wallpaper",
                    checked = dynamicColorEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            themePreferences.setDynamicColor(enabled)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // General Section
        SettingsSectionTitle("General")

        SettingsCard {
            SettingsItem(
                title = "Haptic Feedback",
                description = "Vibration on interactions",
                checked = hapticFeedbackEnabled,
                onCheckedChange = { enabled ->
                    scope.launch {
                        themePreferences.setHapticFeedback(enabled)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Data Section
        SettingsSectionTitle("Data")

        SettingsCard {
            SettingsClickableItem(
                title = "Clear Scan History",
                description = "Delete all scanned codes",
                onClick = { showClearScanHistoryDialog = true }
            )

            HorizontalDivider()

            SettingsClickableItem(
                title = "Clear Generated History",
                description = "Delete all generated codes",
                onClick = { showClearGeneratedHistoryDialog = true }
            )

            HorizontalDivider()

            SettingsClickableItem(
                title = "Clear All Data",
                description = "Delete all history and settings",
                onClick = { showClearAllDataDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About Section
        SettingsSectionTitle("About")

        SettingsCard {
            SettingsClickableItem(
                title = "Version ${BuildConfig.VERSION_NAME}",
                description = "View changelog",
                onClick = { showChangelogSheet = true }
            )

            HorizontalDivider()

            SettingsClickableItem(
                title = "Privacy Policy",
                description = "View our privacy policy",
                onClick = { showPrivacyPolicySheet = true }
            )

            HorizontalDivider()

            SettingsClickableItem(
                title = "Open Source Licenses",
                description = "View third-party libraries",
                onClick = { showLicensesSheet = true }
            )

            HorizontalDivider()

            SettingsClickableItem(
                title = "Creator",
                description = "About the developer",
                onClick = { showCreatorSheet = true }
            )

            HorizontalDivider()

            SettingsClickableItem(
                title = "App License",
                description = "GNU General Public License v3.0",
                onClick = { showAppLicenseSheet = true }
            )

            HorizontalDivider()

            SettingsClickableItem(
                title = "Contact",
                description = "Get in touch with the developer",
                onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                        data = android.net.Uri.parse("mailto:kmrejowan@gmail.com")
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "QRCraft Feedback")
                    }
                    context.startActivity(intent)
                }
            )

            HorizontalDivider()

            SettingsClickableItem(
                title = "GitHub Repository",
                description = "View source code",
                onClick = {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://github.com/ahmmedrejowan/QrCraft")
                    )
                    context.startActivity(intent)
                }
            )
        }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Theme selection dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { theme ->
                scope.launch {
                    themePreferences.setTheme(theme)
                }
                showThemeDialog = false
            }
        )
    }

    // Clear scan history dialog
    if (showClearScanHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearScanHistoryDialog = false },
            title = { Text("Clear Scan History?") },
            text = { Text("This will permanently delete all scanned codes. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearScanHistory()
                        showClearScanHistoryDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearScanHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear generated history dialog
    if (showClearGeneratedHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearGeneratedHistoryDialog = false },
            title = { Text("Clear Generated History?") },
            text = { Text("This will permanently delete all generated codes. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearGeneratedHistory()
                        showClearGeneratedHistoryDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearGeneratedHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear all data dialog
    if (showClearAllDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDataDialog = false },
            title = { Text("Clear All Data?") },
            text = { Text("This will permanently delete all history and reset all settings to defaults. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearAllDataDialog = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Changelog bottom sheet
    if (showChangelogSheet) {
        ModalBottomSheet(
            onDismissRequest = { showChangelogSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            ChangelogContent()
        }
    }

    // Privacy policy bottom sheet
    if (showPrivacyPolicySheet) {
        ModalBottomSheet(
            onDismissRequest = { showPrivacyPolicySheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            PrivacyPolicyContent()
        }
    }

    // Open source licenses bottom sheet
    if (showLicensesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLicensesSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            LicensesContent()
        }
    }

    // Creator info bottom sheet
    if (showCreatorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreatorSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            CreatorContent()
        }
    }

    // App License bottom sheet
    if (showAppLicenseSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAppLicenseSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            AppLicenseContent()
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
private fun StatsCard(
    scanCount: Int,
    generatedCount: Int,
    totalCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "Scanned",
                count = scanCount,
                modifier = Modifier.weight(1f)
            )

            StatItem(
                label = "Generated",
                count = generatedCount,
                modifier = Modifier.weight(1f)
            )

            StatItem(
                label = "Total",
                count = totalCount,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsClickableItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsInfoItem(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onThemeSelected: (String) -> Unit
) {
    val themeOptions = listOf("System", "Light", "Dark")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Choose Theme")
        },
        text = {
            Column {
                themeOptions.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = { onThemeSelected(theme) }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = theme,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ChangelogContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Changelog",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ChangelogVersionItem(
            version = "1.0.0",
            date = "2025-01-12",
            changes = listOf(
                "Initial release of QRCraft",
                "QR code and barcode scanner with real-time detection",
                "Multiple QR code and barcode format support",
                "Template-based code generation system",
                "Customizable QR code styling with colors",
                "Scan history with search and filtering",
                "Generated codes history management",
                "Favorite items support",
                "Dark mode and dynamic color theming",
                "Haptic feedback controls",
                "Share functionality for codes",
                "100% offline - no internet required"
            )
        )
    }
}

@Composable
private fun ChangelogVersionItem(
    version: String,
    date: String,
    changes: List<String>
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Version $version",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        changes.forEach { change ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = "• ",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = change,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PrivacyPolicyContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Privacy Policy",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Privacy Highlights Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Your Privacy is Protected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = "✓ ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(text = "No internet connection required", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = "✓ ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(text = "No data collection or sharing", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = "✓ ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(text = "No analytics or tracking", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = "✓ ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(text = "100% offline operation", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        PrivacySection(
            title = "No Data Collection",
            content = "QRCraft does not collect, store, transmit, or share any personal data whatsoever. The app operates completely offline and does not require an internet connection. There are no analytics, tracking, or telemetry of any kind."
        )

        PrivacySection(
            title = "Local Data Storage",
            content = "All scanned and generated QR codes are stored exclusively on your device. This data never leaves your device and is not accessible to anyone except you. You have complete control and can delete this data at any time from the Settings screen."
        )

        PrivacySection(
            title = "Camera Permission",
            content = "Camera permission is required solely for scanning QR codes and barcodes in real-time. The app does not save, record, or transmit any images or video from your camera. Camera access is only used during active scanning."
        )

        PrivacySection(
            title = "Storage Permission",
            content = "Storage permission is used only to save generated QR code images to your device when you choose to export them. You have complete control over what gets saved and where."
        )

        PrivacySection(
            title = "Third-Party Libraries",
            content = "QRCraft uses open-source libraries (ZXing and Google ML Kit) for QR code processing. These libraries process all data locally on your device and do not transmit any information to external servers."
        )

        PrivacySection(
            title = "No Internet Access",
            content = "This app does not use or require internet access. All functionality works completely offline. Your data stays on your device, always."
        )

        PrivacySection(
            title = "Data Security",
            content = "Since all data is stored locally on your device and never transmitted anywhere, your information is protected by your device's built-in security features. We recommend keeping your device secure with a password, PIN, or biometric lock."
        )

        Text(
            text = "Last updated: November 12, 2025",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun PrivacySection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LicensesContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Open Source Licenses",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LicenseItem(
            name = "ZXing (Zebra Crossing)",
            description = "Barcode scanning library",
            license = "Apache License 2.0",
            url = "https://github.com/zxing/zxing"
        )

        LicenseItem(
            name = "ML Kit Barcode Scanning",
            description = "Google's machine learning barcode scanning",
            license = "Apache License 2.0",
            url = "https://developers.google.com/ml-kit/vision/barcode-scanning"
        )

        LicenseItem(
            name = "Jetpack Compose",
            description = "Modern UI toolkit for Android",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/compose"
        )

        LicenseItem(
            name = "Koin",
            description = "Dependency injection framework",
            license = "Apache License 2.0",
            url = "https://insert-koin.io/"
        )

        LicenseItem(
            name = "Room Database",
            description = "SQLite object mapping library",
            license = "Apache License 2.0",
            url = "https://developer.android.com/training/data-storage/room"
        )

        LicenseItem(
            name = "CameraX",
            description = "Camera API for Android",
            license = "Apache License 2.0",
            url = "https://developer.android.com/training/camerax"
        )

        LicenseItem(
            name = "Accompanist Permissions",
            description = "Permissions handling for Compose",
            license = "Apache License 2.0",
            url = "https://google.github.io/accompanist/permissions/"
        )

        LicenseItem(
            name = "Timber",
            description = "Logging library",
            license = "Apache License 2.0",
            url = "https://github.com/JakeWharton/timber"
        )
    }
}

@Composable
private fun LicenseItem(
    name: String,
    description: String,
    license: String,
    url: String
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(url)
                )
                context.startActivity(intent)
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = license,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun CreatorContent() {
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "About the Creator",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "K M Rejowan Ahmmed",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Senior Android Developer",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Text(
            text = "As a developer who frequently needs to scan QR codes and barcodes, I found that standard camera apps didn't always provide the control and results I needed. QRCraft was born from this need - a dedicated tool that gives you full control over scanning and generating codes, exactly the way you want it.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                CreatorLinkItem(
                    icon = "🌐",
                    label = "Website",
                    value = "rejowan.com",
                    onClick = {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://rejowan.com")
                        )
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                CreatorLinkItem(
                    icon = "📧",
                    label = "Email",
                    value = "kmrejowan@gmail.com",
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                            data = android.net.Uri.parse("mailto:kmrejowan@gmail.com")
                        }
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                CreatorLinkItem(
                    icon = "💼",
                    label = "GitHub",
                    value = "github.com/ahmmedrejowan",
                    onClick = {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://github.com/ahmmedrejowan")
                        )
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                CreatorLinkItem(
                    icon = "🔗",
                    label = "LinkedIn",
                    value = "linkedin.com/in/ahmmedrejowan",
                    onClick = {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://linkedin.com/in/ahmmedrejowan")
                        )
                        context.startActivity(intent)
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .padding(top = 24.dp)
                .clickable {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://rejowan.com")
                    )
                    context.startActivity(intent)
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Made with ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "❤️",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = " by ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "K M Rejowan Ahmmed",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CreatorLinkItem(
    icon: String,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AppLicenseContent() {
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "GNU General Public License v3.0",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = """
QRCraft - QR Code and Barcode Scanner & Generator
Copyright (C) 2025 K M Rejowan Ahmmed

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see the link below.
            """.trimIndent(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Key Terms",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LicenseTermItem("✓ Freedom to use the software for any purpose")
                LicenseTermItem("✓ Freedom to study and modify the source code")
                LicenseTermItem("✓ Freedom to distribute copies")
                LicenseTermItem("✓ Freedom to distribute modified versions")
                LicenseTermItem("✓ Derivative works must be open source under GPL v3.0")
                LicenseTermItem("✓ Modified versions must provide full source code access")
            }
        }

        Text(
            text = "This is a summary. For the complete license terms, please visit the official GPL v3.0 page:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        TextButton(
            onClick = {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://www.gnu.org/licenses/gpl-3.0.en.html")
                )
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "View Full GPL v3.0 License",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LicenseTermItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
