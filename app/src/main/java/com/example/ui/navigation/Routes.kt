package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object AllToolsRoute

@Serializable
data object HistoryRoute

@Serializable
data object SettingsRoute

@Serializable
data class ToolRoute(val toolId: String)

@Serializable
data object PrivacyPolicyRoute
