package com.rejown.qrcraft.presentation.generator

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.rejown.qrcraft.data.local.database.entities.GeneratedCodeEntity
import com.rejown.qrcraft.domain.repository.GeneratorRepository
import com.rejown.qrcraft.presentation.generator.state.GeneratorEvent
import com.rejown.qrcraft.presentation.generator.state.GeneratorState
import com.rejown.qrcraft.utils.generator.CodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class GeneratorViewModel(
    private val generatorRepository: GeneratorRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GeneratorState())
    val state: StateFlow<GeneratorState> = _state.asStateFlow()

    fun onEvent(event: GeneratorEvent) {
        when (event) {
            is GeneratorEvent.OnContentTypeSelected -> {
                _state.update {
                    it.copy(
                        selectedContentType = event.type,
                        inputContent = "",
                        generatedCode = null,
                        error = null
                    )
                }
            }

            is GeneratorEvent.OnBarcodeTypeSelected -> {
                val newFormat = when (event.type) {
                    com.rejown.qrcraft.domain.models.BarcodeType.TWO_D ->
                        com.rejown.qrcraft.domain.models.BarcodeFormat.QR_CODE
                    com.rejown.qrcraft.domain.models.BarcodeType.ONE_D ->
                        com.rejown.qrcraft.domain.models.BarcodeFormat.CODE_128
                }
                _state.update {
                    it.copy(
                        selectedBarcodeType = event.type,
                        selectedBarcodeFormat = newFormat,
                        generatedCode = null,
                        error = null
                    )
                }
                // Regenerate if content exists
                if (_state.value.inputContent.isNotEmpty() && _state.value.realTimePreview) {
                    generateCode()
                }
            }

            is GeneratorEvent.OnBarcodeFormatSelected -> {
                _state.update {
                    it.copy(
                        selectedBarcodeFormat = event.format,
                        generatedCode = null,
                        error = null
                    )
                }
                // Regenerate if content exists
                if (_state.value.inputContent.isNotEmpty() && _state.value.realTimePreview) {
                    generateCode()
                }
            }

            is GeneratorEvent.OnInputChanged -> {
                _state.update {
                    it.copy(
                        inputContent = event.input,
                        error = null
                    )
                }
                // Real-time generation
                if (event.input.isNotEmpty() && _state.value.realTimePreview) {
                    generateCode()
                }
            }

            is GeneratorEvent.OnGenerateClicked -> {
                generateCode()
            }

            is GeneratorEvent.OnCustomizationChanged -> {
                _state.update {
                    it.copy(customization = event.customization)
                }
                // Regenerate if there's already a code
                if (_state.value.generatedCode != null) {
                    generateCode()
                }
            }

            is GeneratorEvent.OnToggleCustomization -> {
                _state.update {
                    it.copy(showCustomization = !it.showCustomization)
                }
            }

            is GeneratorEvent.OnSaveClicked -> {
                saveGeneratedCode()
            }

            is GeneratorEvent.OnShareClicked -> {
                // Share will be handled in the UI with Intent
            }

            is GeneratorEvent.OnClearClicked -> {
                _state.update {
                    GeneratorState(selectedContentType = it.selectedContentType)
                }
            }
        }
    }

    private fun generateCode() {
        val currentState = _state.value
        val content = currentState.inputContent.trim()

        if (content.isEmpty()) {
            _state.update { it.copy(error = "Content cannot be empty") }
            return
        }

        _state.update { it.copy(isGenerating = true, error = null) }

        viewModelScope.launch {
            try {
                val formattedContent = CodeGenerator.formatContentForType(
                    content,
                    currentState.selectedContentType
                )

                val bitmap = withContext(Dispatchers.Default) {
                    CodeGenerator.generateCode(
                        content = formattedContent,
                        format = currentState.selectedBarcodeFormat,
                        customization = currentState.customization
                    )
                }

                if (bitmap != null) {
                    val generatedCode = com.rejown.qrcraft.domain.models.GeneratedCode(
                        content = formattedContent,
                        format = currentState.selectedBarcodeFormat,
                        contentType = currentState.selectedContentType,
                        bitmap = bitmap,
                        customization = currentState.customization
                    )

                    _state.update {
                        it.copy(
                            generatedCode = generatedCode,
                            isGenerating = false,
                            error = null
                        )
                    }

                    Timber.d("Code generated successfully")
                } else {
                    _state.update {
                        it.copy(
                            isGenerating = false,
                            error = "Failed to generate code"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error generating code")
                _state.update {
                    it.copy(
                        isGenerating = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    private fun saveGeneratedCode() {
        // TODO: Update to use new template-based system
        // This old generator is being replaced by the template-based creation flow
        Timber.w("Old generator save functionality disabled - use template-based creation")
    }

    private suspend fun saveBitmapToFile(bitmap: Bitmap?): String? {
        if (bitmap == null) return null

        return withContext(Dispatchers.IO) {
            try {
                // This would need context to get files directory
                // For now, return a placeholder path
                // In real implementation, pass context or use a better storage solution
                val timestamp = System.currentTimeMillis()
                "generated_$timestamp.png"
            } catch (e: Exception) {
                Timber.e(e, "Failed to save bitmap to file")
                null
            }
        }
    }
}
