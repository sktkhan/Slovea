package com.example

import android.app.Application
import com.example.ads.AdManager
import com.example.data.local.DailyFixDatabase
import com.example.data.preferences.AppPreferencesRepository
import com.example.data.repository.DailyFixRepository

class DailyFixApplication : Application() {

    val database: DailyFixDatabase by lazy {
        DailyFixDatabase.getInstance(this)
    }

    val repository: DailyFixRepository by lazy {
        DailyFixRepository(database.dailyFixDao())
    }

    val preferencesRepository: AppPreferencesRepository by lazy {
        AppPreferencesRepository(this)
    }

    val adManager: AdManager by lazy {
        AdManager()
    }
}
