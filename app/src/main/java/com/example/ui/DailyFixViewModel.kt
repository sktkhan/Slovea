package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CalculationHistoryEntity
import com.example.data.preferences.AppPreferencesRepository
import com.example.data.repository.DailyFixRepository
import com.example.domain.model.DailyTool
import com.example.domain.model.ToolCategory
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DailyFixMainUiState(
    val searchQuery: String = "",
    val selectedCategory: ToolCategory = ToolCategory.ALL,
    val favoriteToolIds: Set<String> = emptySet(),
    val recentTools: List<DailyTool> = emptyList(),
    val history: List<CalculationHistoryEntity> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val privacyPolicyUrl: String = ""
)

class DailyFixViewModel(
    private val repository: DailyFixRepository,
    private val preferencesRepository: AppPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ToolCategory.ALL)
    val selectedCategory: StateFlow<ToolCategory> = _selectedCategory.asStateFlow()

    val uiState: StateFlow<DailyFixMainUiState> = combine(
        _searchQuery,
        _selectedCategory,
        repository.favoriteToolIds,
        repository.recentTools,
        repository.history,
        preferencesRepository.themeMode,
        preferencesRepository.privacyPolicyUrl
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        DailyFixMainUiState(
            searchQuery = values[0] as String,
            selectedCategory = values[1] as ToolCategory,
            favoriteToolIds = values[2] as Set<String>,
            recentTools = values[3] as List<DailyTool>,
            history = values[4] as List<CalculationHistoryEntity>,
            themeMode = values[5] as ThemeMode,
            privacyPolicyUrl = values[6] as String
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DailyFixMainUiState()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: ToolCategory) {
        _selectedCategory.value = category
    }

    fun recordToolOpened(tool: DailyTool) {
        viewModelScope.launch {
            repository.recordToolOpened(tool)
        }
    }

    fun toggleFavorite(tool: DailyTool) {
        viewModelScope.launch {
            repository.toggleFavorite(tool)
        }
    }

    fun saveCalculation(tool: DailyTool, expressionSummary: String, resultSummary: String) {
        viewModelScope.launch {
            repository.saveHistory(tool, expressionSummary, resultSummary)
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setPrivacyPolicyUrl(url: String) {
        viewModelScope.launch {
            preferencesRepository.setPrivacyPolicyUrl(url)
        }
    }

    class Factory(
        private val repository: DailyFixRepository,
        private val preferencesRepository: AppPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DailyFixViewModel(repository, preferencesRepository) as T
        }
    }
}
