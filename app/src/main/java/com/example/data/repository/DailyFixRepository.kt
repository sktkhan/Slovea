package com.example.data.repository

import com.example.data.local.CalculationHistoryEntity
import com.example.data.local.DailyFixDao
import com.example.data.local.ToolUsageEntity
import com.example.domain.model.DailyTool
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DailyFixRepository(private val dao: DailyFixDao) {

    val history: Flow<List<CalculationHistoryEntity>> = dao.getHistory(limit = 100)

    val favoriteToolIds: Flow<Set<String>> = dao.observeAllToolUsage().map { list ->
        list.filter { it.isFavorite }.map { it.toolId }.toSet()
    }

    val recentTools: Flow<List<DailyTool>> = dao.observeAllToolUsage().map { list ->
        list.filter { it.lastUsedAt > 0L }
            .sortedByDescending { it.lastUsedAt }
            .mapNotNull { DailyTool.fromId(it.toolId) }
            .take(6)
    }

    suspend fun recordToolOpened(tool: DailyTool) {
        val existing = dao.getToolUsage(tool.id)
        val now = System.currentTimeMillis()
        dao.upsertToolUsage(
            ToolUsageEntity(
                toolId = tool.id,
                isFavorite = existing?.isFavorite ?: false,
                lastUsedAt = now,
                useCount = (existing?.useCount ?: 0) + 1
            )
        )
    }

    suspend fun toggleFavorite(tool: DailyTool) {
        val existing = dao.getToolUsage(tool.id)
        dao.upsertToolUsage(
            ToolUsageEntity(
                toolId = tool.id,
                isFavorite = !(existing?.isFavorite ?: false),
                lastUsedAt = existing?.lastUsedAt ?: 0L,
                useCount = existing?.useCount ?: 0
            )
        )
    }

    suspend fun saveHistory(tool: DailyTool, expressionSummary: String, resultSummary: String) {
        val now = System.currentTimeMillis()
        dao.insertHistory(
            CalculationHistoryEntity(
                toolId = tool.id,
                toolTitle = tool.title,
                expressionSummary = expressionSummary,
                resultSummary = resultSummary,
                timestamp = now
            )
        )
        val existing = dao.getToolUsage(tool.id)
        dao.upsertToolUsage(
            ToolUsageEntity(
                toolId = tool.id,
                isFavorite = existing?.isFavorite ?: false,
                lastUsedAt = now,
                useCount = existing?.useCount ?: 1
            )
        )
    }

    suspend fun deleteHistoryItem(id: Long) {
        dao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        dao.clearAllHistory()
    }
}
