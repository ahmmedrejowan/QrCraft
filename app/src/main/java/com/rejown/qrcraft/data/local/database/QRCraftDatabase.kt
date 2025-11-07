package com.rejown.qrcraft.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
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
