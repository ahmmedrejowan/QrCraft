package com.rejown.qrcraft.presentation.generator.state

import com.rejown.qrcraft.domain.models.BarcodeFormat
import com.rejown.qrcraft.domain.models.CodeCustomization
import com.rejown.qrcraft.domain.models.ContentType
import com.rejown.qrcraft.domain.models.GeneratedCode

data class GeneratorState(
    val selectedContentType: ContentType = ContentType.TEXT,
    val inputContent: String = "",
    val customization: CodeCustomization = CodeCustomization(),
    val generatedCode: GeneratedCode? = null,
    val isGenerating: Boolean = false,
    val error: String? = null,
    val showCustomization: Boolean = false
)

sealed interface GeneratorEvent {
    data class OnContentTypeSelected(val type: ContentType) : GeneratorEvent
    data class OnInputChanged(val input: String) : GeneratorEvent
    data object OnGenerateClicked : GeneratorEvent
    data class OnCustomizationChanged(val customization: CodeCustomization) : GeneratorEvent
    data object OnToggleCustomization : GeneratorEvent
    data object OnSaveClicked : GeneratorEvent
    data object OnShareClicked : GeneratorEvent
    data object OnClearClicked : GeneratorEvent
}
