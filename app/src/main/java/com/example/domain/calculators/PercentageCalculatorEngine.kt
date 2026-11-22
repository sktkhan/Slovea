package com.example.domain.calculators

import kotlin.math.abs

enum class PercentageMode(
    val title: String,
    val firstLabel: String,
    val secondLabel: String,
    val formulaHint: String
) {
    WHAT_PERCENT_OF(
        title = "What % of",
        firstLabel = "Part value (X)",
        secondLabel = "Whole value (Y)",
        formulaHint = "X is what % of Y?"
    ),
    PERCENT_INCREASE(
        title = "% Increase",
        firstLabel = "Original value",
        secondLabel = "New value",
        formulaHint = "Percentage increase from A to B"
    ),
    PERCENT_DECREASE(
        title = "% Decrease",
        firstLabel = "Original value",
        secondLabel = "New value",
        formulaHint = "Percentage decrease from A to B"
    ),
    ADD_PERCENTAGE(
        title = "Add %",
        firstLabel = "Base number",
        secondLabel = "Percentage to add (%)",
        formulaHint = "Base + (Base × P%)"
    ),
    SUBTRACT_PERCENTAGE(
        title = "Subtract %",
        firstLabel = "Base number",
        secondLabel = "Percentage to subtract (%)",
        formulaHint = "Base − (Base × P%)"
    )
}

data class PercentageOutput(
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String
)

object PercentageCalculatorEngine {
    fun calculate(mode: PercentageMode, firstInput: String, secondInput: String): CalcResult<PercentageOutput> {
        val (a, errA) = NumberFormatter.parseNumber(firstInput, mode.firstLabel)
        if (errA != null || a == null) return CalcResult.Error(errA ?: "Invalid input")

        val (b, errB) = NumberFormatter.parseNumber(secondInput, mode.secondLabel)
        if (errB != null || b == null) return CalcResult.Error(errB ?: "Invalid input")

        return when (mode) {
            PercentageMode.WHAT_PERCENT_OF -> {
                if (b == 0.0) return CalcResult.Error("Whole value cannot be zero.")
                val pct = (a / b) * 100.0
                CalcResult.Success(
                    PercentageOutput(
                        primaryLabel = "${NumberFormatter.formatDecimal(a, 4)} is",
                        primaryValue = "${NumberFormatter.formatDecimal(pct, 2)}%",
                        secondaryLabel = "of ${NumberFormatter.formatDecimal(b, 4)}",
                        secondaryValue = "Ratio: ${NumberFormatter.formatDecimal(a / b, 4)}"
                    )
                )
            }

            PercentageMode.PERCENT_INCREASE -> {
                if (a == 0.0) return CalcResult.Error("Original value cannot be zero.")
                val diff = b - a
                val pct = (diff / abs(a)) * 100.0
                val label = if (pct >= 0) "Percentage Increase" else "Net Change (Decrease)"
                CalcResult.Success(
                    PercentageOutput(
                        primaryLabel = label,
                        primaryValue = "${if (pct > 0) "+" else ""}${NumberFormatter.formatDecimal(pct, 2)}%",
                        secondaryLabel = "Absolute Difference",
                        secondaryValue = "${if (diff > 0) "+" else ""}${NumberFormatter.formatDecimal(diff, 2)}"
                    )
                )
            }

            PercentageMode.PERCENT_DECREASE -> {
                if (a == 0.0) return CalcResult.Error("Original value cannot be zero.")
                val diff = a - b
                val pct = (diff / abs(a)) * 100.0
                val label = if (pct >= 0) "Percentage Decrease" else "Net Change (Increase)"
                CalcResult.Success(
                    PercentageOutput(
                        primaryLabel = label,
                        primaryValue = "${NumberFormatter.formatDecimal(pct, 2)}%",
                        secondaryLabel = "Amount Reduced",
                        secondaryValue = NumberFormatter.formatDecimal(diff, 2)
                    )
                )
            }

            PercentageMode.ADD_PERCENTAGE -> {
                val delta = a * (b / 100.0)
                val result = a + delta
                CalcResult.Success(
                    PercentageOutput(
                        primaryLabel = "Result After +${NumberFormatter.formatDecimal(b, 2)}%",
                        primaryValue = NumberFormatter.formatDecimal(result, 2),
                        secondaryLabel = "Amount Added",
                        secondaryValue = "+${NumberFormatter.formatDecimal(delta, 2)}"
                    )
                )
            }

            PercentageMode.SUBTRACT_PERCENTAGE -> {
                val delta = a * (b / 100.0)
                val result = a - delta
                CalcResult.Success(
                    PercentageOutput(
                        primaryLabel = "Result After −${NumberFormatter.formatDecimal(b, 2)}%",
                        primaryValue = NumberFormatter.formatDecimal(result, 2),
                        secondaryLabel = "Amount Subtracted",
                        secondaryValue = "−${NumberFormatter.formatDecimal(delta, 2)}"
                    )
                )
            }
        }
    }
}
