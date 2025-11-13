package com.rejown.qrcraft.presentation.generator.creation

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejown.qrcraft.data.repository.TemplateRepository
import com.rejown.qrcraft.domain.models.BarcodeFormat
import com.rejown.qrcraft.domain.models.CodeCustomization
import com.rejown.qrcraft.domain.models.GeneratedCodeData
import com.rejown.qrcraft.domain.repository.GeneratorRepository
import com.rejown.qrcraft.utils.generator.CodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

class CreationViewModel(
    private val context: Context,
    private val generatorRepository: GeneratorRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreationState())
    val state: StateFlow<CreationState> = _state.asStateFlow()

    private var generationJob: Job? = null

    fun loadTemplate(templateId: String) {
        val template = TemplateRepository.getTemplateById(templateId)
        if (template != null) {
            _state.update {
                it.copy(
                    template = template,
                    selectedFormat = template.defaultFormat,
                    isLoading = false,
                    // Initialize field values with default values
                    fieldValues = template.fields.associate { field ->
                        field.key to (field.defaultValue ?: "")
                    }
                )
            }
        } else {
            _state.update {
                it.copy(
                    error = "Template not found",
                    isLoading = false
                )
            }
        }
    }

    fun loadExistingCode(codeId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val code = generatorRepository.getGeneratedById(codeId)
                if (code != null) {
                    // Load the template
                    val template = TemplateRepository.getTemplateById(code.templateId)
                    if (template != null) {
                        // Field values are already a Map<String, String> in domain model
                        val fieldValues = code.contentFields

                        // Parse the barcode format (already a BarcodeFormat enum in domain model)
                        val barcodeFormat = code.barcodeFormat

                        // Error correction is already an ErrorCorrectionLevel enum in domain model
                        val errorCorrectionLevel = code.errorCorrection
                            ?: com.rejown.qrcraft.domain.models.ErrorCorrectionLevel.MEDIUM

                        // Create customization from stored values
                        val customization = CodeCustomization(
                            foregroundColor = code.foregroundColor,
                            backgroundColor = code.backgroundColor,
                            size = code.size,
                            errorCorrectionLevel = errorCorrectionLevel,
                            margin = code.margin
                        )

                        _state.update {
                            it.copy(
                                template = template,
                                fieldValues = fieldValues,
                                title = code.title ?: "",
                                note = code.note ?: "",
                                selectedFormat = barcodeFormat,
                                customization = customization,
                                isLoading = false,
                                editingCodeId = codeId,
                                isEditMode = true
                            )
                        }

                        // Generate the code preview
                        generateCode()
                    } else {
                        _state.update {
                            it.copy(
                                error = "Template not found",
                                isLoading = false
                            )
                        }
                    }
                } else {
                    _state.update {
                        it.copy(
                            error = "Code not found",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load existing code")
                _state.update {
                    it.copy(
                        error = "Failed to load code: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateFieldValue(key: String, value: String) {
        _state.update {
            it.copy(
                fieldValues = it.fieldValues + (key to value),
                validationErrors = it.validationErrors - key // Clear error for this field
            )
        }
        // Trigger debounced generation
        debouncedGenerate()
    }

    fun updateTitle(title: String) {
        _state.update { it.copy(title = title) }
    }

    fun updateNote(note: String) {
        _state.update { it.copy(note = note) }
    }

    fun updateFormat(format: BarcodeFormat) {
        _state.update { it.copy(selectedFormat = format) }
        // Regenerate immediately when format changes
        generateCode()
    }

    fun updateCustomization(customization: CodeCustomization) {
        _state.update { it.copy(customization = customization) }
        // Regenerate immediately when customization changes
        generateCode()
    }

    fun showFormatSheet() {
        _state.update { it.copy(showFormatSheet = true) }
    }

    fun hideFormatSheet() {
        _state.update { it.copy(showFormatSheet = false) }
    }

    fun showCustomizationSheet() {
        _state.update { it.copy(showCustomizationSheet = true) }
    }

    fun hideCustomizationSheet() {
        _state.update { it.copy(showCustomizationSheet = false) }
    }

    private fun debouncedGenerate() {
        // Cancel previous job
        generationJob?.cancel()

        // Start new debounced job (300ms delay)
        generationJob = viewModelScope.launch {
            delay(300)
            generateCode()
        }
    }

    private fun generateCode() {
        val currentState = _state.value
        val template = currentState.template ?: return
        val format = currentState.selectedFormat ?: return

        // Validate required fields
        val errors = mutableMapOf<String, String>()
        template.fields.forEach { field ->
            if (field.required) {
                val value = currentState.fieldValues[field.key]
                if (value.isNullOrBlank()) {
                    errors[field.key] = "This field is required"
                }
            }
        }

        if (errors.isNotEmpty()) {
            _state.update {
                it.copy(
                    validationErrors = errors,
                    generatedBitmap = null
                )
            }
            return
        }

        // Generate content using template's formatter
        val content = try {
            template.formatContentProvider(currentState.fieldValues)
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    error = "Error formatting content: ${e.message}",
                    isGenerating = false
                )
            }
            return
        }

        if (content.isBlank()) {
            _state.update {
                it.copy(
                    generatedBitmap = null,
                    isGenerating = false,
                    contentLength = 0,
                    maxCapacity = 0,
                    capacityWarning = null,
                    capacityPercentage = 0f
                )
            }
            return
        }

        // Calculate capacity
        val maxCapacity = com.rejown.qrcraft.utils.QRCapacityCalculator.getMaxCapacity(
            format = format,
            errorCorrection = currentState.customization.errorCorrectionLevel,
            content = content
        )
        val capacityPercentage = com.rejown.qrcraft.utils.QRCapacityCalculator.getCapacityPercentage(
            content = content,
            format = format,
            errorCorrection = currentState.customization.errorCorrectionLevel
        )

        // Check capacity
        val isWithinCapacity = com.rejown.qrcraft.utils.QRCapacityCalculator.isWithinCapacity(
            content = content,
            format = format,
            errorCorrection = currentState.customization.errorCorrectionLevel
        )

        // Check format-specific validation
        val formatError = com.rejown.qrcraft.utils.QRCapacityCalculator.validateFormatSpecificContent(
            content = content,
            format = format
        )

        // Generate capacity warning
        val capacityWarning = when {
            formatError != null -> formatError
            !isWithinCapacity -> "Content exceeds ${format.displayName} capacity limit"
            capacityPercentage > 90f -> "Warning: Near capacity limit (${capacityPercentage.toInt()}%)"
            else -> null
        }

        // Update state with capacity info
        _state.update {
            it.copy(
                contentLength = content.length,
                maxCapacity = maxCapacity,
                capacityPercentage = capacityPercentage,
                capacityWarning = capacityWarning
            )
        }

        // Don't generate if there's a capacity error
        if (formatError != null || !isWithinCapacity) {
            _state.update {
                it.copy(
                    generatedBitmap = null,
                    isGenerating = false
                )
            }
            return
        }

        _state.update { it.copy(isGenerating = true, error = null) }

        viewModelScope.launch {
            try {
                val bitmap = CodeGenerator.generateCode(
                    content = content,
                    format = format,
                    customization = currentState.customization
                )

                _state.update {
                    it.copy(
                        generatedBitmap = bitmap,
                        isGenerating = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to generate code: ${e.message}",
                        isGenerating = false,
                        generatedBitmap = null
                    )
                }
            }
        }
    }

    private fun is1DFormat(format: BarcodeFormat): Boolean {
        return format in listOf(
            BarcodeFormat.CODE_128,
            BarcodeFormat.CODE_39,
            BarcodeFormat.EAN_8,
            BarcodeFormat.EAN_13,
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E,
            BarcodeFormat.CODABAR,
            BarcodeFormat.ITF
        )
    }

    fun validateAllFields(): Boolean {
        val currentState = _state.value
        val template = currentState.template ?: return false

        val errors = mutableMapOf<String, String>()
        template.fields.forEach { field ->
            if (field.required) {
                val value = currentState.fieldValues[field.key]
                if (value.isNullOrBlank()) {
                    errors[field.key] = "This field is required"
                }
            }
        }

        _state.update { it.copy(validationErrors = errors) }
        return errors.isEmpty()
    }

    suspend fun saveCode(): Long? {
        val currentState = _state.value
        val bitmap = currentState.generatedBitmap
        val template = currentState.template
        val format = currentState.selectedFormat

        if (bitmap == null) {
            _state.update { it.copy(errorMessage = "No QR code to save") }
            return null
        }

        if (template == null || format == null) {
            _state.update { it.copy(errorMessage = "Template or format not selected") }
            return null
        }

        _state.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }

        return withContext(Dispatchers.IO) {
            try {
                val timestamp = System.currentTimeMillis()

                // Determine if we're updating or creating
                val isEditMode = currentState.isEditMode && currentState.editingCodeId != null

                // For edit mode, get the existing entity to preserve some fields
                val existingCode = if (isEditMode) {
                    generatorRepository.getGeneratedById(currentState.editingCodeId!!)
                } else null

                // Format content
                val formattedContent = template.formatContentProvider(currentState.fieldValues)

                // Create or update domain model (no file storage needed)
                val codeData = if (isEditMode && existingCode != null) {
                    // Update existing code
                    existingCode.copy(
                        templateId = template.id,
                        templateName = template.name,
                        barcodeFormat = format,
                        barcodeType = format.type.name,
                        title = currentState.title.takeIf { it.isNotBlank() },
                        note = currentState.note.takeIf { it.isNotBlank() },
                        contentFields = currentState.fieldValues,
                        formattedContent = formattedContent,
                        foregroundColor = currentState.customization.foregroundColor,
                        backgroundColor = currentState.customization.backgroundColor,
                        size = currentState.customization.size,
                        errorCorrection = currentState.customization.errorCorrectionLevel,
                        margin = currentState.customization.margin,
                        updatedAt = timestamp
                        // Preserve: createdAt, isFavorite, scanCount
                    )
                } else {
                    // Create new code
                    GeneratedCodeData(
                        templateId = template.id,
                        templateName = template.name,
                        barcodeFormat = format,
                        barcodeType = format.type.name,
                        title = currentState.title.takeIf { it.isNotBlank() },
                        note = currentState.note.takeIf { it.isNotBlank() },
                        contentFields = currentState.fieldValues,
                        formattedContent = formattedContent,
                        foregroundColor = currentState.customization.foregroundColor,
                        backgroundColor = currentState.customization.backgroundColor,
                        size = currentState.customization.size,
                        errorCorrection = currentState.customization.errorCorrectionLevel,
                        margin = currentState.customization.margin,
                        createdAt = timestamp,
                        updatedAt = timestamp,
                        isFavorite = false,
                        scanCount = 0
                    )
                }

                // Insert or update
                val codeId = if (isEditMode && existingCode != null) {
                    generatorRepository.updateGenerated(codeData)
                    codeData.id // Return existing ID
                } else {
                    generatorRepository.insertGenerated(codeData)
                }

                _state.update {
                    it.copy(
                        isSaving = false,
                        successMessage = if (isEditMode) "QR code updated successfully" else "QR code saved successfully"
                    )
                }
                Timber.d("Generated code ${if (isEditMode) "updated" else "saved"} successfully with ID: $codeId")
                codeId
            } catch (e: Exception) {
                Timber.e(e, "Failed to save generated code")
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to save: ${e.message ?: "Unknown error"}"
                    )
                }
                null
            }
        }
    }

    suspend fun getShareUri(): Uri? {
        val bitmap = _state.value.generatedBitmap

        if (bitmap == null) {
            _state.update { it.copy(errorMessage = "No QR code to share") }
            return null
        }

        _state.update { it.copy(isSharing = true, errorMessage = null, successMessage = null) }

        return withContext(Dispatchers.IO) {
            try {
                // Create temp file for sharing
                val timestamp = System.currentTimeMillis()
                val fileName = "share_qrcode_${timestamp}.png"
                val shareDir = File(context.cacheDir, "shared")
                shareDir.mkdirs()

                val file = File(shareDir, fileName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                // Get content URI using FileProvider
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                _state.update { it.copy(isSharing = false) }
                Timber.d("Share URI created successfully")
                uri
            } catch (e: Exception) {
                Timber.e(e, "Failed to create share URI")
                _state.update {
                    it.copy(
                        isSharing = false,
                        errorMessage = "Failed to share: ${e.message ?: "Unknown error"}"
                    )
                }
                null
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(successMessage = null, errorMessage = null) }
    }

    fun showExitDialog() {
        _state.update { it.copy(showExitDialog = true) }
    }

    fun hideExitDialog() {
        _state.update { it.copy(showExitDialog = false) }
    }

    fun handleBackPress(): Boolean {
        return if (_state.value.hasUnsavedChanges()) {
            showExitDialog()
            true // Consume the back press
        } else {
            false // Allow back press to proceed
        }
    }
}
