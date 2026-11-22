package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CalculationHistoryEntity::class, ToolUsageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DailyFixDatabase : RoomDatabase() {
    abstract fun dailyFixDao(): DailyFixDao

    companion object {
        @Volatile
        private var INSTANCE: DailyFixDatabase? = null

        fun getInstance(context: Context): DailyFixDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DailyFixDatabase::class.java,
                    "dailyfix_local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
