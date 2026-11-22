package com.example.ui.screens.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.domain.calculators.CalcResult
import com.example.domain.calculators.NumberFormatter
import com.example.domain.calculators.TipCalculatorEngine
import com.example.domain.model.DailyTool
import com.example.ui.components.ErrorBanner
import com.example.ui.components.NumericInputField
import com.example.ui.components.PresetChipRow
import com.example.ui.components.PrimaryResultHeroCard
import com.example.ui.components.ToolScaffold
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun TipCalculatorScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSaveHistory: (String, String) -> Unit,
    onBack: () -> Unit,
    canRequestAds: Boolean,
    isSdkInitialized: Boolean
) {
    var billAmount by rememberSaveable { mutableStateOf("") }
    var tipPercent by rememberSaveable { mutableStateOf("15") }
    var splitCount by rememberSaveable { mutableIntStateOf(1) }
    var savedFlag by remember { mutableStateOf(false) }

    val result = remember(billAmount, tipPercent, splitCount) {
        if (billAmount.isBlank() || tipPercent.isBlank()) null
        else TipCalculatorEngine.calculate(billAmount, tipPercent, splitCount)
    }

    val presets = listOf("10", "15", "18", "20", "25")

    ToolScaffold(
        tool = DailyTool.TIP_CALCULATOR,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        onBack = onBack,
        canRequestAds = canRequestAds,
        isSdkInitialized = isSdkInitialized,
        onReset = {
            billAmount = ""
            tipPercent = "15"
            splitCount = 1
            savedFlag = false
        }
    ) {
        NumericInputField(
            value = billAmount,
            onValueChange = {
                billAmount = it
                savedFlag = false
            },
            label = "Bill amount",
            placeholder = "e.g. 64.50",
            imeAction = ImeAction.Next,
            testTag = "tip_bill_input"
        )

        NumericInputField(
            value = tipPercent,
            onValueChange = {
                tipPercent = it
                savedFlag = false
            },
            label = "Tip percentage",
            suffixText = "%",
            testTag = "tip_percent_input"
        )

        Slider(
            value = (tipPercent.toFloatOrNull() ?: 0f).coerceIn(0f, 50f),
            onValueChange = {
                tipPercent = it.toInt().toString()
                savedFlag = false
            },
            valueRange = 0f..50f
        )

        PresetChipRow(
            options = presets.map { "$it%" },
            selectedOption = presets.firstOrNull { it == tipPercent }?.let { "$it%" },
            onSelect = { chosen ->
                tipPercent = chosen.removeSuffix("%")
                savedFlag = false
            }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    text = "Split between people",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            if (splitCount > 1) {
                                splitCount--
                                savedFlag = false
                            }
                        },
                        enabled = splitCount > 1,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("tip_split_minus")
                    ) {
                        Icon(Icons.Rounded.Remove, contentDescription = "Decrease people")
                    }
                    Text(
                        text = "$splitCount",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.testTag("tip_split_count")
                    )
                    FilledTonalIconButton(
                        onClick = {
                            if (splitCount < 50) {
                                splitCount++
                                savedFlag = false
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("tip_split_plus")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Increase people")
                    }
                }
            }
        }

        when (result) {
            is CalcResult.Success -> {
                val out = result.data
                PrimaryResultHeroCard(
                    title = if (out.splitCount > 1) "Amount Per Person (${out.splitCount} people)" else "Total Bill",
                    primaryValue = NumberFormatter.formatFixed2(out.amountPerPerson),
                    subtitle = "Tip: ${NumberFormatter.formatFixed2(out.tipAmount)} (${NumberFormatter.formatDecimal(out.tipPercent, 1)}%)",
                    metrics = listOf(
                        "Total Bill" to NumberFormatter.formatFixed2(out.totalBill),
                        "Tip / Person" to NumberFormatter.formatFixed2(out.tipPerPerson)
                    ),
                    savedIndicator = savedFlag,
                    onSaveToHistory = {
                        onSaveHistory(
                            "Bill ${NumberFormatter.formatFixed2(out.billAmount)} + ${NumberFormatter.formatDecimal(out.tipPercent, 1)}% (${out.splitCount}p)",
                            "Total ${NumberFormatter.formatFixed2(out.totalBill)} • ${NumberFormatter.formatFixed2(out.amountPerPerson)}/person"
                        )
                        savedFlag = true
                    }
                )
            }
            is CalcResult.Error -> ErrorBanner(message = result.message)
            null -> {
                Text(
                    text = "Enter the bill amount to calculate tip and per-person split.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
