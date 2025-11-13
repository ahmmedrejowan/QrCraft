package com.rejown.qrcraft.domain.repository

import com.rejown.qrcraft.domain.models.ScanHistory
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    fun getAllHistory(): Flow<List<ScanHistory>>
    fun getFavorites(): Flow<List<ScanHistory>>
    fun searchHistory(query: String): Flow<List<ScanHistory>>
    fun getHistoryByType(type: String): Flow<List<ScanHistory>>
    suspend fun getHistoryById(id: Long): ScanHistory?
    suspend fun insertScan(history: ScanHistory): Long
    suspend fun updateScan(history: ScanHistory)
    suspend fun deleteScan(history: ScanHistory)
    suspend fun deleteByIds(ids: List<Long>)
    suspend fun deleteOlderThan(timestamp: Long)
    suspend fun deleteAll()
    suspend fun getCount(): Int
    suspend fun findDuplicate(format: String, content: String): ScanHistory?
}
