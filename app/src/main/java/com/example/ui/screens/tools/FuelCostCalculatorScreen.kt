package com.example.ui.screens.tools

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.example.domain.calculators.CalcResult
import com.example.domain.calculators.FuelCostCalculatorEngine
import com.example.domain.calculators.FuelEfficiencyUnit
import com.example.domain.calculators.NumberFormatter
import com.example.domain.model.DailyTool
import com.example.ui.components.ErrorBanner
import com.example.ui.components.NumericInputField
import com.example.ui.components.PrimaryResultHeroCard
import com.example.ui.components.ToolScaffold

@Composable
fun FuelCostCalculatorScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSaveHistory: (String, String) -> Unit,
    onBack: () -> Unit,
    canRequestAds: Boolean,
    isSdkInitialized: Boolean
) {
    var efficiencyUnit by rememberSaveable { mutableStateOf(FuelEfficiencyUnit.KM_PER_LITER) }
    var distance by rememberSaveable { mutableStateOf("250") }
    var efficiency by rememberSaveable { mutableStateOf("14") }
    var fuelPrice by rememberSaveable { mutableStateOf("1.65") }
    var savedFlag by remember { mutableStateOf(false) }

    val result = remember(distance, efficiency, fuelPrice, efficiencyUnit) {
        if (distance.isBlank() || efficiency.isBlank() || fuelPrice.isBlank()) null
        else FuelCostCalculatorEngine.calculate(distance, efficiency, fuelPrice, efficiencyUnit)
    }

    ToolScaffold(
        tool = DailyTool.FUEL_COST_CALCULATOR,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        onBack = onBack,
        canRequestAds = canRequestAds,
        isSdkInitialized = isSdkInitialized,
        onReset = {
            distance = ""
            efficiency = ""
            fuelPrice = ""
            savedFlag = false
        }
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            FuelEfficiencyUnit.entries.forEachIndexed { idx, unit ->
                SegmentedButton(
                    selected = efficiencyUnit == unit,
                    onClick = {
                        efficiencyUnit = unit
                        savedFlag = false
                    },
                    shape = SegmentedButtonDefaults.itemShape(idx, FuelEfficiencyUnit.entries.size),
                    modifier = Modifier.testTag("fuel_unit_${unit.name}")
                ) {
                    Text(unit.title, maxLines = 1)
                }
            }
        }

        NumericInputField(
            value = distance,
            onValueChange = {
                distance = it
                savedFlag = false
            },
            label = "Trip distance",
            suffixText = efficiencyUnit.distanceUnitLabel,
            imeAction = ImeAction.Next,
            testTag = "fuel_distance_input"
        )

        NumericInputField(
            value = efficiency,
            onValueChange = {
                efficiency = it
                savedFlag = false
            },
            label = "Fuel efficiency",
            suffixText = efficiencyUnit.title,
            imeAction = ImeAction.Next,
            testTag = "fuel_efficiency_input"
        )

        NumericInputField(
            value = fuelPrice,
            onValueChange = {
                fuelPrice = it
                savedFlag = false
            },
            label = "Fuel price (${efficiencyUnit.priceUnitLabel})",
            testTag = "fuel_price_input"
        )

        when (result) {
            is CalcResult.Success -> {
                val out = result.data
                val fuelStr = "${NumberFormatter.formatDecimal(out.estimatedFuelConsumption, 2)} ${out.unit.volumeUnitLabel}"
                val costStr = NumberFormatter.formatFixed2(out.totalFuelCost)
                val costPerDist = NumberFormatter.formatFixed2(out.costPerDistanceUnit)

                PrimaryResultHeroCard(
                    title = "Total Estimated Fuel Cost",
                    primaryValue = costStr,
                    subtitle = "$distance ${out.unit.distanceUnitLabel} at $efficiency ${out.unit.title}",
                    metrics = listOf(
                        "Fuel Needed" to fuelStr,
                        "Cost / ${out.unit.distanceUnitLabel}" to costPerDist
                    ),
                    savedIndicator = savedFlag,
                    onSaveToHistory = {
                        onSaveHistory(
                            "${distance}${out.unit.distanceUnitLabel} • $efficiency ${out.unit.title}",
                            "Cost: $costStr ($fuelStr)"
                        )
                        savedFlag = true
                    }
                )
            }
            is CalcResult.Error -> ErrorBanner(message = result.message)
            null -> {
                Text(
                    text = "Enter distance, fuel efficiency, and price to estimate trip fuel costs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
