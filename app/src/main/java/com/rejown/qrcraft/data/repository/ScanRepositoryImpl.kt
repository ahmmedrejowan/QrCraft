package com.rejown.qrcraft.data.repository

import com.rejown.qrcraft.data.local.database.dao.ScanHistoryDao
import com.rejown.qrcraft.data.local.database.entities.ScanHistoryEntity
import com.rejown.qrcraft.domain.repository.ScanRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class ScanRepositoryImpl(
    private val scanHistoryDao: ScanHistoryDao
) : ScanRepository {

    override fun getAllHistory(): Flow<List<ScanHistoryEntity>> {
        return scanHistoryDao.getAllHistory()
    }

    override fun getFavorites(): Flow<List<ScanHistoryEntity>> {
        return scanHistoryDao.getFavorites()
    }

    override fun searchHistory(query: String): Flow<List<ScanHistoryEntity>> {
        return scanHistoryDao.searchHistory(query)
    }

    override fun getHistoryByType(type: String): Flow<List<ScanHistoryEntity>> {
        return scanHistoryDao.getHistoryByType(type)
    }

    override suspend fun getHistoryById(id: Long): ScanHistoryEntity? {
        return scanHistoryDao.getHistoryById(id)
    }

    override suspend fun insertScan(history: ScanHistoryEntity): Long {
        Timber.tag("QRCraft ScanRepositoryImpl").e("insertScan - Called with content: ${history.content}")
        return try {
            val id = scanHistoryDao.insert(history)
            Timber.tag("QRCraft ScanRepositoryImpl").e("insertScan - Insert successful, ID: $id")
            id
        } catch (e: Exception) {
            Timber.tag("QRCraft ScanRepositoryImpl").e(e, "insertScan - Insert failed")
            throw e
        }
    }

    override suspend fun updateScan(history: ScanHistoryEntity) {
        scanHistoryDao.update(history)
    }

    override suspend fun deleteScan(history: ScanHistoryEntity) {
        scanHistoryDao.delete(history)
    }

    override suspend fun deleteByIds(ids: List<Long>) {
        scanHistoryDao.deleteByIds(ids)
    }

    override suspend fun deleteOlderThan(timestamp: Long) {
        scanHistoryDao.deleteOlderThan(timestamp)
    }

    override suspend fun deleteAll() {
        scanHistoryDao.deleteAll()
    }

    override suspend fun getCount(): Int {
        return scanHistoryDao.getCount()
    }
}
