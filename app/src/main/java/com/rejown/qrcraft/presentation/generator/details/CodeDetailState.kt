package com.rejown.qrcraft.presentation.generator.details

import android.graphics.Bitmap
import com.rejown.qrcraft.data.local.database.entities.GeneratedCodeEntity

data class CodeDetailState(
    val code: GeneratedCodeEntity? = null,
    val bitmap: Bitmap? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleting: Boolean = false,
    val isSavingToGallery: Boolean = false,
    val isCopying: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showShareBottomSheet: Boolean = false,
    val showCopyBottomSheet: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
