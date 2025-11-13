package com.rejown.qrcraft.data.mappers

import com.rejown.qrcraft.data.local.database.entities.ScanHistoryEntity
import com.rejown.qrcraft.domain.models.BarcodeFormat
import com.rejown.qrcraft.domain.models.ContentType
import com.rejown.qrcraft.domain.models.ScanHistory
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

fun ScanHistoryEntity.toDomain(): ScanHistory {
    return ScanHistory(
        id = id,
        content = content,
        rawValue = rawValue,
        format = BarcodeFormat.fromString(format),
        contentType = ContentType.fromString(contentType),
        timestamp = timestamp,
        isFavorite = isFavorite,
        metadata = metadata?.let {
            try {
                Json.decodeFromString<Map<String, String>>(it)
            } catch (e: Exception) {
                null
            }
        }
    )
}

fun ScanHistory.toEntity(): ScanHistoryEntity {
    return ScanHistoryEntity(
        id = id,
        content = content,
        rawValue = rawValue,
        format = format.name,
        contentType = contentType.name,
        timestamp = timestamp,
        isFavorite = isFavorite,
        metadata = metadata?.let { Json.encodeToString(it) }
    )
}

fun List<ScanHistoryEntity>.toDomainList(): List<ScanHistory> = map { it.toDomain() }
