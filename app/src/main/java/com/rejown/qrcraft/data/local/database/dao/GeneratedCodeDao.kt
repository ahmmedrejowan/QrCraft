package com.rejown.qrcraft.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rejown.qrcraft.data.local.database.entities.GeneratedCodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneratedCodeDao {

    @Query("SELECT * FROM generated_codes ORDER BY timestamp DESC")
    fun getAllGenerated(): Flow<List<GeneratedCodeEntity>>

    @Query("SELECT * FROM generated_codes WHERE is_favorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<GeneratedCodeEntity>>

    @Query("SELECT * FROM generated_codes WHERE content LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%'")
    fun searchGenerated(query: String): Flow<List<GeneratedCodeEntity>>

    @Query("SELECT * FROM generated_codes WHERE content_type = :type ORDER BY timestamp DESC")
    fun getGeneratedByType(type: String): Flow<List<GeneratedCodeEntity>>

    @Query("SELECT * FROM generated_codes WHERE id = :id")
    suspend fun getGeneratedById(id: Long): GeneratedCodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(code: GeneratedCodeEntity): Long

    @Update
    suspend fun update(code: GeneratedCodeEntity)

    @Delete
    suspend fun delete(code: GeneratedCodeEntity)

    @Query("DELETE FROM generated_codes WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM generated_codes")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM generated_codes")
    suspend fun getCount(): Int
}
