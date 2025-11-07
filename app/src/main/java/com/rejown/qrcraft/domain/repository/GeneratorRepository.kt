package com.rejown.qrcraft.domain.repository

import com.rejown.qrcraft.data.local.database.entities.GeneratedCodeEntity
import kotlinx.coroutines.flow.Flow

interface GeneratorRepository {
    fun getAllGenerated(): Flow<List<GeneratedCodeEntity>>
    fun getFavorites(): Flow<List<GeneratedCodeEntity>>
    fun searchGenerated(query: String): Flow<List<GeneratedCodeEntity>>
    fun getGeneratedByType(type: String): Flow<List<GeneratedCodeEntity>>
    suspend fun getGeneratedById(id: Long): GeneratedCodeEntity?
    suspend fun insertGenerated(code: GeneratedCodeEntity): Long
    suspend fun updateGenerated(code: GeneratedCodeEntity)
    suspend fun deleteGenerated(code: GeneratedCodeEntity)
    suspend fun deleteByIds(ids: List<Long>)
    suspend fun deleteAll()
    suspend fun getCount(): Int
}
