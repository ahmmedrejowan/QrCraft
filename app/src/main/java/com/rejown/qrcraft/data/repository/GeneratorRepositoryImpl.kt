package com.rejown.qrcraft.data.repository

import com.rejown.qrcraft.data.local.database.dao.GeneratedCodeDao
import com.rejown.qrcraft.data.mappers.toDomain
import com.rejown.qrcraft.data.mappers.toDomainList
import com.rejown.qrcraft.data.mappers.toEntity
import com.rejown.qrcraft.domain.models.GeneratedCodeData
import com.rejown.qrcraft.domain.repository.GeneratorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GeneratorRepositoryImpl(
    private val generatedCodeDao: GeneratedCodeDao
) : GeneratorRepository {

    override fun getAllGenerated(): Flow<List<GeneratedCodeData>> {
        return generatedCodeDao.getAllGenerated().map { it.toDomainList() }
    }

    override fun getFavorites(): Flow<List<GeneratedCodeData>> {
        return generatedCodeDao.getFavorites().map { it.toDomainList() }
    }

    override fun searchGenerated(query: String): Flow<List<GeneratedCodeData>> {
        return generatedCodeDao.searchGenerated(query).map { it.toDomainList() }
    }

    override fun getGeneratedByType(type: String): Flow<List<GeneratedCodeData>> {
        return generatedCodeDao.getGeneratedByType(type).map { it.toDomainList() }
    }

    override suspend fun getGeneratedById(id: Long): GeneratedCodeData? {
        return generatedCodeDao.getGeneratedById(id)?.toDomain()
    }

    override suspend fun insertGenerated(code: GeneratedCodeData): Long {
        return generatedCodeDao.insert(code.toEntity())
    }

    override suspend fun updateGenerated(code: GeneratedCodeData) {
        generatedCodeDao.update(code.toEntity())
    }

    override suspend fun deleteGenerated(code: GeneratedCodeData) {
        generatedCodeDao.delete(code.toEntity())
    }

    override suspend fun deleteByIds(ids: List<Long>) {
        generatedCodeDao.deleteByIds(ids)
    }

    override suspend fun deleteAll() {
        generatedCodeDao.deleteAll()
    }

    override suspend fun getCount(): Int {
        return generatedCodeDao.getCount()
    }
}
