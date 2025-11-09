package com.rejown.qrcraft.presentation.generator.details

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejown.qrcraft.domain.repository.GeneratorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class CodeDetailViewModel(
    private val context: Context,
    private val generatorRepository: GeneratorRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CodeDetailState())
    val state: StateFlow<CodeDetailState> = _state.asStateFlow()

    fun loadCode(codeId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val code = generatorRepository.getGeneratedById(codeId)
                if (code != null) {
                    // Load bitmap from file
                    val bitmap = loadBitmapFromFile(code.imagePath)
                    _state.update {
                        it.copy(
                            code = code,
                            bitmap = bitmap,
                            isLoading = false,
                            error = if (bitmap == null) "Failed to load image" else null
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Code not found"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load code")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load code: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun loadBitmapFromFile(fileName: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, fileName)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    Timber.w("Image file not found: $fileName")
                    null
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load bitmap from file")
                null
            }
        }
    }

    fun toggleFavorite() {
        val code = _state.value.code ?: return

        viewModelScope.launch {
            try {
                val updatedCode = code.copy(
                    isFavorite = !code.isFavorite,
                    updatedAt = System.currentTimeMillis()
                )
                generatorRepository.updateGenerated(updatedCode)
                _state.update {
                    it.copy(
                        code = updatedCode,
                        successMessage = if (updatedCode.isFavorite) "Added to favorites" else "Removed from favorites"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to toggle favorite")
                _state.update {
                    it.copy(errorMessage = "Failed to update favorite")
                }
            }
        }
    }

    fun showDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = true) }
    }

    fun hideDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false) }
    }

    suspend fun deleteCode(): Boolean {
        val code = _state.value.code ?: return false

        _state.update { it.copy(isDeleting = true, showDeleteDialog = false) }

        return withContext(Dispatchers.IO) {
            try {
                // Delete image file
                val file = File(context.filesDir, code.imagePath)
                if (file.exists()) {
                    file.delete()
                }

                // Delete from database
                generatorRepository.deleteGenerated(code)
                _state.update {
                    it.copy(
                        isDeleting = false,
                        successMessage = "Code deleted successfully"
                    )
                }
                Timber.d("Code deleted successfully")
                true
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete code")
                _state.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage = "Failed to delete: ${e.message}"
                    )
                }
                false
            }
        }
    }

    suspend fun getShareUri(): Uri? {
        val bitmap = _state.value.bitmap

        if (bitmap == null) {
            _state.update { it.copy(errorMessage = "No image to share") }
            return null
        }

        _state.update { it.copy(isSharing = true, errorMessage = null) }

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
}
