package com.rejown.qrcraft.data.repository

import com.rejown.qrcraft.data.local.database.dao.GeneratedCodeDao
import com.rejown.qrcraft.data.local.database.entities.GeneratedCodeEntity
import com.rejown.qrcraft.domain.repository.GeneratorRepository
import kotlinx.coroutines.flow.Flow

class GeneratorRepositoryImpl(
    private val generatedCodeDao: GeneratedCodeDao
) : GeneratorRepository {

    override fun getAllGenerated(): Flow<List<GeneratedCodeEntity>> {
        return generatedCodeDao.getAllGenerated()
    }

    override fun getFavorites(): Flow<List<GeneratedCodeEntity>> {
        return generatedCodeDao.getFavorites()
    }

    override fun searchGenerated(query: String): Flow<List<GeneratedCodeEntity>> {
        return generatedCodeDao.searchGenerated(query)
    }

    override fun getGeneratedByType(type: String): Flow<List<GeneratedCodeEntity>> {
        return generatedCodeDao.getGeneratedByType(type)
    }

    override suspend fun getGeneratedById(id: Long): GeneratedCodeEntity? {
        return generatedCodeDao.getGeneratedById(id)
    }

    override suspend fun insertGenerated(code: GeneratedCodeEntity): Long {
        return generatedCodeDao.insert(code)
    }

    override suspend fun updateGenerated(code: GeneratedCodeEntity) {
        generatedCodeDao.update(code)
    }

    override suspend fun deleteGenerated(code: GeneratedCodeEntity) {
        generatedCodeDao.delete(code)
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
