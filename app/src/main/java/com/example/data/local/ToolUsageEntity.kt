package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tool_usage")
data class ToolUsageEntity(
    @PrimaryKey val toolId: String,
    val isFavorite: Boolean = false,
    val lastUsedAt: Long = 0L,
    val useCount: Int = 0
)
