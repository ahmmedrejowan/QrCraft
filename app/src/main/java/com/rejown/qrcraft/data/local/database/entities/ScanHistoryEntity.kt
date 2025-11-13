package com.rejown.qrcraft.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scan_history",
    indices = [
        Index(value = ["timestamp"], name = "idx_scan_history_timestamp"),
        Index(value = ["content_type"], name = "idx_scan_history_content_type"),
        Index(value = ["is_favorite"], name = "idx_scan_history_is_favorite"),
        Index(value = ["content"], name = "idx_scan_history_content")
    ]
)
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "raw_value")
    val rawValue: String,

    @ColumnInfo(name = "format")
    val format: String, // QR_CODE, EAN_13, CODE_128, etc.

    @ColumnInfo(name = "content_type")
    val contentType: String, // URL, EMAIL, PHONE, TEXT, etc.

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "metadata")
    val metadata: String? = null // JSON for additional data
)
