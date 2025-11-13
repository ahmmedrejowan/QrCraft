package com.rejown.qrcraft.domain.repository

import com.rejown.qrcraft.domain.models.GeneratedCodeData
import kotlinx.coroutines.flow.Flow

interface GeneratorRepository {
    fun getAllGenerated(): Flow<List<GeneratedCodeData>>
    fun getFavorites(): Flow<List<GeneratedCodeData>>
    fun searchGenerated(query: String): Flow<List<GeneratedCodeData>>
    fun getGeneratedByType(type: String): Flow<List<GeneratedCodeData>>
    suspend fun getGeneratedById(id: Long): GeneratedCodeData?
    suspend fun insertGenerated(code: GeneratedCodeData): Long
    suspend fun updateGenerated(code: GeneratedCodeData)
    suspend fun deleteGenerated(code: GeneratedCodeData)
    suspend fun deleteByIds(ids: List<Long>)
    suspend fun deleteAll()
    suspend fun getCount(): Int
}
