package com.rejown.qrcraft.domain.models

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Template for QR code generation
 * Defines what fields to show and how to format the content
 */
data class Template(
    val id: String,                                              // "business_card", "wifi", etc.
    val name: String,                                            // "Business Card"
    val description: String,                                     // "Professional digital business card"
    val icon: ImageVector,
    val category: TemplateCategory,
    val defaultFormat: BarcodeFormat,
    val allowedFormats: List<BarcodeFormat>,                    // Formats compatible with this template
    val fields: List<FieldDefinition>,
    val formatContentProvider: (Map<String, String>) -> String  // Lambda to format field values into QR content
)
