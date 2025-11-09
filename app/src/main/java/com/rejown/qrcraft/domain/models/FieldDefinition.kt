package com.rejown.qrcraft.domain.models

/**
 * Defines a single field in a template's form
 */
data class FieldDefinition(
    val key: String,                          // "email", "phone", etc.
    val label: String,                        // "Email Address"
    val type: FieldType,
    val required: Boolean = false,
    val placeholder: String? = null,
    val helperText: String? = null,
    val validation: ValidationRule? = null,
    val defaultValue: String? = null,
    val options: List<String>? = null         // For dropdown fields
)
