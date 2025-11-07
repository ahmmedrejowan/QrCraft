package com.rejown.qrcraft.domain.repository

import com.rejown.qrcraft.data.local.database.entities.ScanHistoryEntity
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    fun getAllHistory(): Flow<List<ScanHistoryEntity>>
    fun getFavorites(): Flow<List<ScanHistoryEntity>>
    fun searchHistory(query: String): Flow<List<ScanHistoryEntity>>
    fun getHistoryByType(type: String): Flow<List<ScanHistoryEntity>>
    suspend fun getHistoryById(id: Long): ScanHistoryEntity?
    suspend fun insertScan(history: ScanHistoryEntity): Long
    suspend fun updateScan(history: ScanHistoryEntity)
    suspend fun deleteScan(history: ScanHistoryEntity)
    suspend fun deleteByIds(ids: List<Long>)
    suspend fun deleteOlderThan(timestamp: Long)
    suspend fun deleteAll()
    suspend fun getCount(): Int
}
