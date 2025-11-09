package com.rejown.qrcraft.presentation.generator.details

import android.graphics.Bitmap
import com.rejown.qrcraft.data.local.database.entities.GeneratedCodeEntity

data class CodeDetailState(
    val code: GeneratedCodeEntity? = null,
    val bitmap: Bitmap? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleting: Boolean = false,
    val isSharing: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
