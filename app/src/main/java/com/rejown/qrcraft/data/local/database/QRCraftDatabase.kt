package com.rejown.qrcraft.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rejown.qrcraft.data.local.database.dao.GeneratedCodeDao
import com.rejown.qrcraft.data.local.database.dao.ScanHistoryDao
import com.rejown.qrcraft.data.local.database.entities.GeneratedCodeEntity
import com.rejown.qrcraft.data.local.database.entities.ScanHistoryEntity

@Database(
    entities = [
        ScanHistoryEntity::class,
        GeneratedCodeEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class QRCraftDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun generatedCodeDao(): GeneratedCodeDao
}

// Migration from version 1 to 2: Remove image storage columns
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Remove image_path from scan_history
        db.execSQL("ALTER TABLE scan_history DROP COLUMN image_path")

        // Remove image_path, image_width, image_height from generated_codes
        // SQLite doesn't support DROP COLUMN before 3.35.0, so we need to recreate the table
        db.execSQL("""
            CREATE TABLE generated_codes_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                template_id TEXT NOT NULL,
                template_name TEXT NOT NULL,
                barcode_format TEXT NOT NULL,
                barcode_type TEXT NOT NULL,
                title TEXT,
                note TEXT,
                content_fields TEXT NOT NULL,
                formatted_content TEXT NOT NULL,
                foreground_color INTEGER NOT NULL,
                background_color INTEGER NOT NULL,
                size INTEGER NOT NULL,
                error_correction TEXT,
                margin INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_favorite INTEGER NOT NULL DEFAULT 0,
                scan_count INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO generated_codes_new
            SELECT id, template_id, template_name, barcode_format, barcode_type,
                   title, note, content_fields, formatted_content,
                   foreground_color, background_color, size, error_correction, margin,
                   created_at, updated_at, is_favorite, scan_count
            FROM generated_codes
        """.trimIndent())

        db.execSQL("DROP TABLE generated_codes")
        db.execSQL("ALTER TABLE generated_codes_new RENAME TO generated_codes")

        // Recreate indices
        db.execSQL("CREATE INDEX idx_generated_codes_created_at ON generated_codes(created_at)")
        db.execSQL("CREATE INDEX idx_generated_codes_template_id ON generated_codes(template_id)")
        db.execSQL("CREATE INDEX idx_generated_codes_is_favorite ON generated_codes(is_favorite)")
    }
}
