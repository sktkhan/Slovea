package com.example.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.domain.calculators.CalcResult
import com.example.domain.calculators.PercentageCalculatorEngine
import com.example.domain.calculators.PercentageMode
import com.example.domain.model.DailyTool
import com.example.ui.components.ErrorBanner
import com.example.ui.components.NumericInputField
import com.example.ui.components.PrimaryResultHeroCard
import com.example.ui.components.ToolScaffold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PercentageCalculatorScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSaveHistory: (String, String) -> Unit,
    onBack: () -> Unit,
    canRequestAds: Boolean,
    isSdkInitialized: Boolean
) {
    var mode by rememberSaveable { mutableStateOf(PercentageMode.WHAT_PERCENT_OF) }
    var firstInput by rememberSaveable { mutableStateOf("") }
    var secondInput by rememberSaveable { mutableStateOf("") }
    var savedFlag by remember { mutableStateOf(false) }

    val result = remember(mode, firstInput, secondInput) {
        if (firstInput.isBlank() || secondInput.isBlank()) null
        else PercentageCalculatorEngine.calculate(mode, firstInput, secondInput)
    }

    ToolScaffold(
        tool = DailyTool.PERCENTAGE_CALCULATOR,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        onBack = onBack,
        canRequestAds = canRequestAds,
        isSdkInitialized = isSdkInitialized,
        onReset = {
            firstInput = ""
            secondInput = ""
            savedFlag = false
        }
    ) {
        Text(
            text = "Calculation Mode",
            style = MaterialTheme.typography.titleMedium
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PercentageMode.entries.forEach { item ->
                FilterChip(
                    selected = mode == item,
                    onClick = {
                        mode = item
                        savedFlag = false
                    },
                    label = { Text(item.title) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("pct_mode_${item.name}")
                )
            }
        }

        Text(
            text = mode.formulaHint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        NumericInputField(
            value = firstInput,
            onValueChange = {
                firstInput = it
                savedFlag = false
            },
            label = mode.firstLabel,
            allowSigned = true,
            imeAction = ImeAction.Next,
            testTag = "pct_input_first"
        )

        NumericInputField(
            value = secondInput,
            onValueChange = {
                secondInput = it
                savedFlag = false
            },
            label = mode.secondLabel,
            allowSigned = true,
            suffixText = if (mode == PercentageMode.ADD_PERCENTAGE || mode == PercentageMode.SUBTRACT_PERCENTAGE) "%" else null,
            testTag = "pct_input_second"
        )

        when (result) {
            is CalcResult.Success -> {
                val out = result.data
                PrimaryResultHeroCard(
                    title = out.primaryLabel,
                    primaryValue = out.primaryValue,
                    subtitle = "${out.secondaryLabel}: ${out.secondaryValue}",
                    savedIndicator = savedFlag,
                    onSaveToHistory = {
                        onSaveHistory(
                            "${mode.title}: $firstInput & $secondInput",
                            "${out.primaryValue} (${out.secondaryValue})"
                        )
                        savedFlag = true
                    }
                )
            }
            is CalcResult.Error -> {
                ErrorBanner(message = result.message)
            }
            null -> {
                Text(
                    text = "Enter both values above to see instant percentage results.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
