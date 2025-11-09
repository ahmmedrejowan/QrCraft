package com.rejown.qrcraft.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    // Main container with bottom nav
    @Serializable
    data class Main(
        val initialTab: Int? = null
    ) : Screen()

    // Bottom nav destinations (nested in Main)
    @Serializable
    data object Scanner : Screen()

    @Serializable
    data object Generator : Screen()

    @Serializable
    data object History : Screen()

    @Serializable
    data object Settings : Screen()

    // App-level screens (outside bottom nav)
    @Serializable
    data class Detail(
        val id: Long,
        val type: String
    ) : Screen()

    // Generator flow screens (will be added later)
    @Serializable
    data class Creation(
        val templateId: String
    ) : Screen()

    @Serializable
    data class CodeDetails(
        val codeId: Long
    ) : Screen()

    @Serializable
    data class ScanDetail(
        val rawValue: String,
        val displayValue: String,
        val format: String, // BarcodeFormat name
        val contentType: String, // ContentType name
        val timestamp: Long
    ) : Screen()

    @Serializable
    data class ScanHistoryDetail(
        val scanId: Long
    ) : Screen()
}
