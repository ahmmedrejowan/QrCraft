package com.rejown.qrcraft.presentation.generator.details

import android.graphics.Bitmap
import com.rejown.qrcraft.domain.models.GeneratedCodeData

data class CodeDetailState(
    val code: GeneratedCodeData? = null,
    val bitmap: Bitmap? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleting: Boolean = false,
    val isSavingToGallery: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showShareBottomSheet: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
