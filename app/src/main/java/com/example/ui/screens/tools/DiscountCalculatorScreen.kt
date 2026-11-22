package com.example.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.domain.calculators.CalcResult
import com.example.domain.calculators.DiscountCalculatorEngine
import com.example.domain.calculators.NumberFormatter
import com.example.domain.model.DailyTool
import com.example.ui.components.ErrorBanner
import com.example.ui.components.NumericInputField
import com.example.ui.components.PresetChipRow
import com.example.ui.components.PrimaryResultHeroCard
import com.example.ui.components.ToolScaffold

@Composable
fun DiscountCalculatorScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSaveHistory: (String, String) -> Unit,
    onBack: () -> Unit,
    canRequestAds: Boolean,
    isSdkInitialized: Boolean
) {
    var originalPrice by rememberSaveable { mutableStateOf("") }
    var discountPercent by rememberSaveable { mutableStateOf("20") }
    var savedFlag by remember { mutableStateOf(false) }

    val result = remember(originalPrice, discountPercent) {
        if (originalPrice.isBlank() || discountPercent.isBlank()) null
        else DiscountCalculatorEngine.calculate(originalPrice, discountPercent)
    }

    val presets = listOf("10", "15", "20", "25", "30", "50", "75")

    ToolScaffold(
        tool = DailyTool.DISCOUNT_CALCULATOR,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        onBack = onBack,
        canRequestAds = canRequestAds,
        isSdkInitialized = isSdkInitialized,
        onReset = {
            originalPrice = ""
            discountPercent = "20"
            savedFlag = false
        }
    ) {
        NumericInputField(
            value = originalPrice,
            onValueChange = {
                originalPrice = it
                savedFlag = false
            },
            label = "Original price",
            placeholder = "e.g. 120.00",
            imeAction = ImeAction.Next,
            testTag = "discount_price_input"
        )

        NumericInputField(
            value = discountPercent,
            onValueChange = {
                discountPercent = it
                savedFlag = false
            },
            label = "Discount percentage",
            suffixText = "%",
            testTag = "discount_percent_input"
        )

        val sliderValue = (discountPercent.toFloatOrNull() ?: 0f).coerceIn(0f, 100f)
        Slider(
            value = sliderValue,
            onValueChange = {
                discountPercent = it.toInt().toString()
                savedFlag = false
            },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )

        PresetChipRow(
            options = presets.map { "$it%" },
            selectedOption = presets.firstOrNull { it == discountPercent }?.let { "$it%" },
            onSelect = { chosen ->
                discountPercent = chosen.removeSuffix("%")
                savedFlag = false
            }
        )

        when (result) {
            is CalcResult.Success -> {
                val out = result.data
                PrimaryResultHeroCard(
                    title = "Final Sale Price",
                    primaryValue = NumberFormatter.formatFixed2(out.finalPrice),
                    metrics = listOf(
                        "You Save" to NumberFormatter.formatFixed2(out.amountSaved),
                        "Discount" to "${NumberFormatter.formatDecimal(out.discountPercent, 1)}%"
                    ),
                    savedIndicator = savedFlag,
                    onSaveToHistory = {
                        onSaveHistory(
                            "${NumberFormatter.formatFixed2(out.originalPrice)} − ${NumberFormatter.formatDecimal(out.discountPercent, 1)}%",
                            "Final: ${NumberFormatter.formatFixed2(out.finalPrice)} (Saved ${NumberFormatter.formatFixed2(out.amountSaved)})"
                        )
                        savedFlag = true
                    }
                )

                val payFraction = if (out.originalPrice > 0.0) {
                    (out.finalPrice / out.originalPrice).toFloat().coerceIn(0f, 1f)
                } else 1f
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "You pay ${NumberFormatter.formatDecimal(payFraction * 100.0, 1)}% of the original price",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LinearProgressIndicator(
                        progress = { payFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(100.dp)),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
            is CalcResult.Error -> ErrorBanner(message = result.message)
            null -> {
                Text(
                    text = "Enter an original price to calculate your savings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
