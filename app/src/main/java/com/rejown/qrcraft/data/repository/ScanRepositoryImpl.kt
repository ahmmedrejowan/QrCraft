package com.rejown.qrcraft.data.repository

import com.rejown.qrcraft.data.local.database.dao.ScanHistoryDao
import com.rejown.qrcraft.data.mappers.toDomain
import com.rejown.qrcraft.data.mappers.toDomainList
import com.rejown.qrcraft.data.mappers.toEntity
import com.rejown.qrcraft.domain.models.ScanHistory
import com.rejown.qrcraft.domain.repository.ScanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class ScanRepositoryImpl(
    private val scanHistoryDao: ScanHistoryDao
) : ScanRepository {

    override fun getAllHistory(): Flow<List<ScanHistory>> {
        return scanHistoryDao.getAllHistory().map { it.toDomainList() }
    }

    override fun getFavorites(): Flow<List<ScanHistory>> {
        return scanHistoryDao.getFavorites().map { it.toDomainList() }
    }

    override fun searchHistory(query: String): Flow<List<ScanHistory>> {
        return scanHistoryDao.searchHistory(query).map { it.toDomainList() }
    }

    override fun getHistoryByType(type: String): Flow<List<ScanHistory>> {
        return scanHistoryDao.getHistoryByType(type).map { it.toDomainList() }
    }

    override suspend fun getHistoryById(id: Long): ScanHistory? {
        return scanHistoryDao.getHistoryById(id)?.toDomain()
    }

    override suspend fun insertScan(history: ScanHistory): Long {
        Timber.tag("QC ScanRepositoryImpl").d("insertScan - Called with content: ${history.content}")
        return try {
            val id = scanHistoryDao.insert(history.toEntity())
            Timber.tag("QC ScanRepositoryImpl").d("insertScan - Insert successful, ID: $id")
            id
        } catch (e: Exception) {
            Timber.tag("QC ScanRepositoryImpl").e(e, "insertScan - Insert failed")
            throw e
        }
    }

    override suspend fun updateScan(history: ScanHistory) {
        scanHistoryDao.update(history.toEntity())
    }

    override suspend fun deleteScan(history: ScanHistory) {
        scanHistoryDao.delete(history.toEntity())
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

    override suspend fun findDuplicate(format: String, content: String): ScanHistory? {
        return scanHistoryDao.findDuplicate(format, content)?.toDomain()
    }
}
