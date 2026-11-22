package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.DailyTool
import com.example.domain.model.ToolCategory
import com.example.ui.DailyFixMainUiState
import com.example.ui.components.FeaturedToolTile
import com.example.ui.components.SectionHeader
import com.example.ui.components.ToolDirectoryCard

@Composable
fun HomeScreen(
    uiState: DailyFixMainUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (ToolCategory) -> Unit,
    onOpenTool: (DailyTool) -> Unit,
    onToggleFavorite: (DailyTool) -> Unit,
    onOpenAllTools: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteTools = remember(uiState.favoriteToolIds) {
        DailyTool.entries.filter { it.id in uiState.favoriteToolIds }
    }
    val featuredTools = remember {
        DailyTool.entries.filter { it.isFeatured }
    }
    val filteredTools = remember(uiState.searchQuery, uiState.selectedCategory) {
        val query = uiState.searchQuery.trim().lowercase()
        DailyTool.entries.filter { tool ->
            val matchesCategory = uiState.selectedCategory == ToolCategory.ALL || tool.category == uiState.selectedCategory
            val matchesQuery = query.isEmpty() ||
                tool.title.lowercase().contains(query) ||
                tool.description.lowercase().contains(query) ||
                tool.category.title.lowercase().contains(query) ||
                tool.keywords.any { it.contains(query) }
            if (query.isNotEmpty()) matchesQuery else matchesCategory
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HomeHeroBanner()
            }

            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search calculators, converters, tools...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search tools"
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input")
                )
            }

            if (uiState.searchQuery.isBlank()) {
                if (favoriteTools.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionHeader(title = "Favorites")
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(favoriteTools, key = { "fav_${it.id}" }) { tool ->
                                    QuickToolChip(
                                        tool = tool,
                                        badgeIcon = Icons.Rounded.Star,
                                        onClick = { onOpenTool(tool) }
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.recentTools.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionHeader(title = "Recently Used")
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.recentTools, key = { "recent_${it.id}" }) { tool ->
                                    QuickToolChip(
                                        tool = tool,
                                        badgeIcon = Icons.Rounded.History,
                                        onClick = { onOpenTool(tool) }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(
                            title = "Featured Tools",
                            actionLabel = "See all",
                            onAction = onOpenAllTools
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            featuredTools.chunked(2).forEach { rowTools ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowTools.forEach { tool ->
                                        FeaturedToolTile(
                                            tool = tool,
                                            onClick = { onOpenTool(tool) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowTools.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(title = "Browse Categories")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(ToolCategory.entries) { category ->
                                FilterChip(
                                    selected = uiState.selectedCategory == category,
                                    onClick = { onCategorySelected(category) },
                                    label = { Text(category.title) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.testTag("home_category_${category.name}")
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "Search Results (${filteredTools.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (filteredTools.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No matching tools found",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Try searching for calculator, loan, BMI, fuel, or units.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredTools, key = { it.id }) { tool ->
                    ToolDirectoryCard(
                        tool = tool,
                        isFavorite = tool.id in uiState.favoriteToolIds,
                        onClick = { onOpenTool(tool) },
                        onToggleFavorite = { onToggleFavorite(tool) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(152.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_hero_banner),
            contentDescription = stringResource(R.string.cd_hero_banner),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xE607253B),
                            Color(0xA60B4F6C),
                            Color(0x330D9488)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(100.dp)
            ) {
                Text(
                    text = "10 OFFLINE-READY TOOLS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF99F6E4),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Text(
                    text = "Smart everyday calculators & converters in seconds",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun QuickToolChip(
    tool: DailyTool,
    badgeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("quick_chip_${tool.id}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = tool.shortTitle,
                style = MaterialTheme.typography.labelLarge
            )
            Icon(
                imageVector = badgeIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
