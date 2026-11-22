package com.example.ui.screens.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.calculators.CalcResult
import com.example.domain.calculators.MeasureUnit
import com.example.domain.calculators.UnitCategory
import com.example.domain.calculators.UnitConverterEngine
import com.example.domain.model.DailyTool
import com.example.ui.components.ErrorBanner
import com.example.ui.components.NumericInputField
import com.example.ui.components.PrimaryResultHeroCard
import com.example.ui.components.ToolScaffold
import com.example.ui.theme.JetBrainsMonoFontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UnitConverterScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSaveHistory: (String, String) -> Unit,
    onBack: () -> Unit,
    canRequestAds: Boolean,
    isSdkInitialized: Boolean
) {
    var category by rememberSaveable { mutableStateOf(UnitCategory.LENGTH) }
    var fromUnit by rememberSaveable { mutableStateOf(MeasureUnit.KILOMETER) }
    var toUnit by rememberSaveable { mutableStateOf(MeasureUnit.MILE) }
    var inputValue by rememberSaveable { mutableStateOf("1") }
    var savedFlag by remember { mutableStateOf(false) }

    val availableUnits = remember(category) { MeasureUnit.unitsForCategory(category) }

    val result = remember(inputValue, fromUnit, toUnit) {
        if (inputValue.isBlank()) null
        else UnitConverterEngine.convert(inputValue, fromUnit, toUnit)
    }

    ToolScaffold(
        tool = DailyTool.UNIT_CONVERTER,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        onBack = onBack,
        canRequestAds = canRequestAds,
        isSdkInitialized = isSdkInitialized,
        onReset = {
            inputValue = "1"
            savedFlag = false
        }
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            UnitCategory.entries.forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = {
                        category = cat
                        val list = MeasureUnit.unitsForCategory(cat)
                        fromUnit = list.first()
                        toUnit = list.getOrElse(1) { list.first() }
                        savedFlag = false
                    },
                    label = { Text(cat.title) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("unit_cat_${cat.name}")
                )
            }
        }

        NumericInputField(
            value = inputValue,
            onValueChange = {
                inputValue = it
                savedFlag = false
            },
            label = "Value in ${fromUnit.label}",
            suffixText = fromUnit.symbol,
            allowSigned = category == UnitCategory.TEMPERATURE,
            testTag = "unit_value_input"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UnitDropdownBox(
                label = "From",
                selectedUnit = fromUnit,
                units = availableUnits,
                onSelect = {
                    fromUnit = it
                    savedFlag = false
                },
                modifier = Modifier.weight(1f)
            )

            FilledTonalIconButton(
                onClick = {
                    val temp = fromUnit
                    fromUnit = toUnit
                    toUnit = temp
                    savedFlag = false
                },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("unit_swap_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.SwapHoriz,
                    contentDescription = stringResource(R.string.cd_swap_units)
                )
            }

            UnitDropdownBox(
                label = "To",
                selectedUnit = toUnit,
                units = availableUnits,
                onSelect = {
                    toUnit = it
                    savedFlag = false
                },
                modifier = Modifier.weight(1f)
            )
        }

        when (result) {
            is CalcResult.Success -> {
                val out = result.data
                PrimaryResultHeroCard(
                    title = "${fromUnit.label} to ${toUnit.label}",
                    primaryValue = "${out.primaryFormatted} ${toUnit.symbol}",
                    subtitle = "$inputValue ${fromUnit.symbol} = ${out.primaryFormatted} ${toUnit.symbol}",
                    savedIndicator = savedFlag,
                    onSaveToHistory = {
                        onSaveHistory(
                            "$inputValue ${fromUnit.symbol} → ${toUnit.symbol}",
                            "${out.primaryFormatted} ${toUnit.symbol}"
                        )
                        savedFlag = true
                    }
                )

                Text(
                    text = "All ${category.title} Conversions",
                    style = MaterialTheme.typography.titleMedium
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    out.allConversions.forEach { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.unit == toUnit) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.unit.label} (${item.unit.symbol})",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${item.formattedValue} ${item.unit.symbol}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            is CalcResult.Error -> ErrorBanner(message = result.message)
            null -> {
                Text(
                    text = "Enter a number to convert across ${category.title.lowercase()} units.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UnitDropdownBox(
    label: String,
    selectedUnit: MeasureUnit,
    units: List<MeasureUnit>,
    onSelect: (MeasureUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedCard(
            onClick = { expanded = true },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${selectedUnit.label} (${selectedUnit.symbol})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = "Select $label unit"
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text("${unit.label} (${unit.symbol})") },
                    onClick = {
                        onSelect(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}
