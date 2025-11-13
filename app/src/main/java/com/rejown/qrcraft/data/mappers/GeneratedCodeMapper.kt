package com.rejown.qrcraft.data.mappers

import com.rejown.qrcraft.data.local.database.entities.GeneratedCodeEntity
import com.rejown.qrcraft.domain.models.BarcodeFormat
import com.rejown.qrcraft.domain.models.ErrorCorrectionLevel
import com.rejown.qrcraft.domain.models.GeneratedCodeData
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

fun GeneratedCodeEntity.toDomain(): GeneratedCodeData {
    return GeneratedCodeData(
        id = id,
        templateId = templateId,
        templateName = templateName,
        barcodeFormat = BarcodeFormat.fromString(barcodeFormat),
        barcodeType = barcodeType,
        title = title,
        note = note,
        contentFields = try {
            Json.decodeFromString<Map<String, String>>(contentFields)
        } catch (e: Exception) {
            emptyMap()
        },
        formattedContent = formattedContent,
        foregroundColor = foregroundColor,
        backgroundColor = backgroundColor,
        size = size,
        errorCorrection = errorCorrection?.let {
            try {
                ErrorCorrectionLevel.valueOf(it)
            } catch (e: Exception) {
                null
            }
        },
        margin = margin,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFavorite = isFavorite,
        scanCount = scanCount
    )
}

fun GeneratedCodeData.toEntity(): GeneratedCodeEntity {
    return GeneratedCodeEntity(
        id = id,
        templateId = templateId,
        templateName = templateName,
        barcodeFormat = barcodeFormat.name,
        barcodeType = barcodeType,
        title = title,
        note = note,
        contentFields = Json.encodeToString(contentFields),
        formattedContent = formattedContent,
        foregroundColor = foregroundColor,
        backgroundColor = backgroundColor,
        size = size,
        errorCorrection = errorCorrection?.name,
        margin = margin,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFavorite = isFavorite,
        scanCount = scanCount
    )
}

fun List<GeneratedCodeEntity>.toDomainList(): List<GeneratedCodeData> = map { it.toDomain() }
