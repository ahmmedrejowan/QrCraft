package com.rejown.qrcraft.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "generated_codes",
    indices = [
        Index(value = ["timestamp"], name = "idx_generated_codes_timestamp"),
        Index(value = ["content_type"], name = "idx_generated_codes_content_type"),
        Index(value = ["is_favorite"], name = "idx_generated_codes_is_favorite"),
        Index(value = ["content"], name = "idx_generated_codes_content")
    ]
)
data class GeneratedCodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "format")
    val format: String,

    @ColumnInfo(name = "content_type")
    val contentType: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "image_path")
    val imagePath: String, // Path to generated image

    @ColumnInfo(name = "customization")
    val customization: String? = null, // JSON: colors, size, logo, etc.

    @ColumnInfo(name = "title")
    val title: String? = null // Optional user-defined title
)
