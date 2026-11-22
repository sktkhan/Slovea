package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyFixDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC LIMIT :limit")
    fun getHistory(limit: Int = 100): Flow<List<CalculationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: CalculationHistoryEntity)

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAllHistory()

    @Query("SELECT * FROM tool_usage")
    fun observeAllToolUsage(): Flow<List<ToolUsageEntity>>

    @Query("SELECT * FROM tool_usage WHERE toolId = :toolId LIMIT 1")
    suspend fun getToolUsage(toolId: String): ToolUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertToolUsage(usage: ToolUsageEntity)
}
