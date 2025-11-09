package com.rejown.qrcraft.presentation.generator.creation

import android.graphics.Bitmap
import com.rejown.qrcraft.domain.models.BarcodeFormat
import com.rejown.qrcraft.domain.models.CodeCustomization
import com.rejown.qrcraft.domain.models.Template

data class CreationState(
    val template: Template? = null,
    val fieldValues: Map<String, String> = emptyMap(),
    val title: String = "",
    val note: String = "",
    val selectedFormat: BarcodeFormat? = null,
    val customization: CodeCustomization = CodeCustomization(),
    val generatedBitmap: Bitmap? = null,
    val isGenerating: Boolean = false,
    val isSaving: Boolean = false,
    val isSharing: Boolean = false,
    val error: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
    val showFormatSheet: Boolean = false,
    val showCustomizationSheet: Boolean = false,
    val isLoading: Boolean = true,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val showExitDialog: Boolean = false
) {
    fun hasUnsavedChanges(): Boolean {
        return fieldValues.values.any { it.isNotBlank() } ||
                title.isNotBlank() ||
                note.isNotBlank() ||
                generatedBitmap != null
    }
}
