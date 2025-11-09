package com.rejown.qrcraft.domain.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Categories for organizing QR code templates
 */
enum class TemplateCategory(
    val displayName: String,
    val icon: ImageVector
) {
    GENERAL(
        displayName = "General & Personal",
        icon = Icons.Default.Description
    ),
    COMMUNICATION(
        displayName = "Communication",
        icon = Icons.Default.Message
    ),
    SOCIAL_WEB(
        displayName = "Social & Web",
        icon = Icons.Default.Language
    ),
    LOCATION_EVENTS(
        displayName = "Location & Events",
        icon = Icons.Default.Place
    ),
    BUSINESS(
        displayName = "Business & Professional",
        icon = Icons.Default.Business
    ),
    PRODUCT(
        displayName = "Product & Inventory",
        icon = Icons.Default.Inventory
    ),
    DOCUMENTS(
        displayName = "Documents & Files",
        icon = Icons.Default.FileCopy
    ),
    TICKETS(
        displayName = "Tickets & Passes",
        icon = Icons.Default.ConfirmationNumber
    )
}
