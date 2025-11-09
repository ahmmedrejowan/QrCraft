package com.rejown.qrcraft.domain.models

/**
 * Types of input fields for dynamic forms
 */
enum class FieldType {
    TEXT,              // Single-line text
    MULTILINE_TEXT,    // Multi-line text area
    EMAIL,             // Email with validation
    PHONE,             // Phone with formatting
    URL,               // URL with https:// prefix helper
    NUMBER,            // Numeric input
    DROPDOWN,          // Dropdown selection
    DATE,              // Date picker
    TIME,              // Time picker
    DATETIME,          // Date & time picker
    CHECKBOX           // Boolean checkbox
}
