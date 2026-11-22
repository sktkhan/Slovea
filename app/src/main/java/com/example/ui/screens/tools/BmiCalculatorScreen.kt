package com.example.ui.screens.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.calculators.BmiCalculatorEngine
import com.example.domain.calculators.BmiCategory
import com.example.domain.calculators.BmiUnitSystem
import com.example.domain.calculators.CalcResult
import com.example.domain.calculators.NumberFormatter
import com.example.domain.model.DailyTool
import com.example.ui.components.DisclaimerCard
import com.example.ui.components.ErrorBanner
import com.example.ui.components.NumericInputField
import com.example.ui.components.PrimaryResultHeroCard
import com.example.ui.components.ToolScaffold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun BmiCalculatorScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSaveHistory: (String, String) -> Unit,
    onBack: () -> Unit,
    canRequestAds: Boolean,
    isSdkInitialized: Boolean
) {
    var unitSystem by rememberSaveable { mutableStateOf(BmiUnitSystem.METRIC) }
    var weightKg by rememberSaveable { mutableStateOf("70") }
    var heightCm by rememberSaveable { mutableStateOf("175") }
    var weightLb by rememberSaveable { mutableStateOf("154") }
    var heightFt by rememberSaveable { mutableStateOf("5") }
    var heightIn by rememberSaveable { mutableStateOf("9") }
    var savedFlag by remember { mutableStateOf(false) }

    val result = remember(unitSystem, weightKg, heightCm, weightLb, heightFt, heightIn) {
        when (unitSystem) {
            BmiUnitSystem.METRIC -> {
                if (weightKg.isBlank() || heightCm.isBlank()) null
                else BmiCalculatorEngine.calculateMetric(weightKg, heightCm)
            }
            BmiUnitSystem.IMPERIAL -> {
                if (weightLb.isBlank() || heightFt.isBlank()) null
                else BmiCalculatorEngine.calculateImperial(weightLb, heightFt, heightIn)
            }
        }
    }

    ToolScaffold(
        tool = DailyTool.BMI_CALCULATOR,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        onBack = onBack,
        canRequestAds = canRequestAds,
        isSdkInitialized = isSdkInitialized,
        onReset = {
            weightKg = ""
            heightCm = ""
            weightLb = ""
            heightFt = ""
            heightIn = ""
            savedFlag = false
        }
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            BmiUnitSystem.entries.forEachIndexed { idx, sys ->
                SegmentedButton(
                    selected = unitSystem == sys,
                    onClick = {
                        unitSystem = sys
                        savedFlag = false
                    },
                    shape = SegmentedButtonDefaults.itemShape(idx, BmiUnitSystem.entries.size),
                    modifier = Modifier.testTag("bmi_unit_${sys.name}")
                ) {
                    Text(sys.title, maxLines = 1)
                }
            }
        }

        if (unitSystem == BmiUnitSystem.METRIC) {
            NumericInputField(
                value = weightKg,
                onValueChange = {
                    weightKg = it
                    savedFlag = false
                },
                label = "Weight",
                suffixText = "kg",
                imeAction = ImeAction.Next,
                testTag = "bmi_weight_kg"
            )
            NumericInputField(
                value = heightCm,
                onValueChange = {
                    heightCm = it
                    savedFlag = false
                },
                label = "Height",
                suffixText = "cm",
                testTag = "bmi_height_cm"
            )
        } else {
            NumericInputField(
                value = weightLb,
                onValueChange = {
                    weightLb = it
                    savedFlag = false
                },
                label = "Weight",
                suffixText = "lb",
                imeAction = ImeAction.Next,
                testTag = "bmi_weight_lb"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumericInputField(
                    value = heightFt,
                    onValueChange = {
                        heightFt = it
                        savedFlag = false
                    },
                    label = "Feet",
                    suffixText = "ft",
                    allowDecimal = false,
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f),
                    testTag = "bmi_height_ft"
                )
                NumericInputField(
                    value = heightIn,
                    onValueChange = {
                        heightIn = it
                        savedFlag = false
                    },
                    label = "Inches",
                    suffixText = "in",
                    modifier = Modifier.weight(1f),
                    testTag = "bmi_height_in"
                )
            }
        }

        when (result) {
            is CalcResult.Success -> {
                val out = result.data
                val bmiFormatted = NumberFormatter.formatDecimal(out.bmi, 1)
                val healthyRange = if (out.unitSystem == BmiUnitSystem.METRIC) {
                    "${NumberFormatter.formatDecimal(out.healthyMinWeightKg, 1)} – ${NumberFormatter.formatDecimal(out.healthyMaxWeightKg, 1)} kg"
                } else {
                    "${NumberFormatter.formatDecimal(out.healthyMinWeightLb, 1)} – ${NumberFormatter.formatDecimal(out.healthyMaxWeightLb, 1)} lb"
                }

                PrimaryResultHeroCard(
                    title = "Body Mass Index (BMI)",
                    primaryValue = bmiFormatted,
                    subtitle = "${out.category.label} (${out.category.rangeText})",
                    metrics = listOf(
                        "Category" to out.category.label,
                        "Healthy Range" to healthyRange
                    ),
                    savedIndicator = savedFlag,
                    onSaveToHistory = {
                        val summary = if (out.unitSystem == BmiUnitSystem.METRIC) {
                            "${weightKg}kg, ${heightCm}cm"
                        } else {
                            "${weightLb}lb, ${heightFt}ft ${heightIn.ifEmpty { "0" }}in"
                        }
                        onSaveHistory("BMI ($summary)", "BMI $bmiFormatted — ${out.category.label}")
                        savedFlag = true
                    }
                )

                BmiScaleBar(currentCategory = out.category)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "What This Means",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = out.category.explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            is CalcResult.Error -> ErrorBanner(message = result.message)
            null -> {
                Text(
                    text = "Enter your weight and height to calculate BMI.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DisclaimerCard(text = stringResource(R.string.bmi_disclaimer))
    }
}

@Composable
private fun BmiScaleBar(currentCategory: BmiCategory) {
    val bands = listOf(
        Triple("Under", Color(0xFF38BDF8), BmiCategory.UNDERWEIGHT),
        Triple("Normal", SuccessGreen, BmiCategory.NORMAL),
        Triple("Over", WarningAmber, BmiCategory.OVERWEIGHT),
        Triple("Obese", Color(0xFFEF4444), BmiCategory.OBESITY_I)
    )
    val activeBand = when (currentCategory) {
        BmiCategory.UNDERWEIGHT -> BmiCategory.UNDERWEIGHT
        BmiCategory.NORMAL -> BmiCategory.NORMAL
        BmiCategory.OVERWEIGHT -> BmiCategory.OVERWEIGHT
        BmiCategory.OBESITY_I, BmiCategory.OBESITY_II, BmiCategory.OBESITY_III -> BmiCategory.OBESITY_I
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        bands.forEach { (label, color, bandCat) ->
            val isActive = bandCat == activeBand
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isActive) 12.dp else 7.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isActive) color else color.copy(alpha = 0.28f))
                )
                Surface(
                    color = if (isActive) color.copy(alpha = 0.16f) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
