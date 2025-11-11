package com.rejown.qrcraft.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rejown.qrcraft.data.local.database.entities.ScanHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {

    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history WHERE is_favorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history WHERE content LIKE '%' || :query || '%' OR content_type LIKE '%' || :query || '%'")
    fun searchHistory(query: String): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history WHERE content_type = :type ORDER BY timestamp DESC")
    fun getHistoryByType(type: String): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history WHERE id = :id")
    suspend fun getHistoryById(id: Long): ScanHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: ScanHistoryEntity): Long

    @Update
    suspend fun update(history: ScanHistoryEntity)

    @Delete
    suspend fun delete(history: ScanHistoryEntity)

    @Query("DELETE FROM scan_history WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM scan_history WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("DELETE FROM scan_history")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM scan_history")
    suspend fun getCount(): Int

    @Query("SELECT * FROM scan_history WHERE format = :format AND content = :content LIMIT 1")
    suspend fun findDuplicate(format: String, content: String): ScanHistoryEntity?
}
