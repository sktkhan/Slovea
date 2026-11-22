package com.example.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.calculators.CalcResult
import com.example.domain.calculators.LoanCalculatorEngine
import com.example.domain.calculators.NumberFormatter
import com.example.domain.model.DailyTool
import com.example.ui.components.DisclaimerCard
import com.example.ui.components.ErrorBanner
import com.example.ui.components.NumericInputField
import com.example.ui.components.PrimaryResultHeroCard
import com.example.ui.components.ToolScaffold

@Composable
fun LoanCalculatorScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSaveHistory: (String, String) -> Unit,
    onBack: () -> Unit,
    canRequestAds: Boolean,
    isSdkInitialized: Boolean
) {
    var loanAmount by rememberSaveable { mutableStateOf("10000") }
    var annualRate by rememberSaveable { mutableStateOf("6.5") }
    var duration by rememberSaveable { mutableStateOf("3") }
    var isYears by rememberSaveable { mutableStateOf(true) }
    var savedFlag by remember { mutableStateOf(false) }

    val result = remember(loanAmount, annualRate, duration, isYears) {
        if (loanAmount.isBlank() || annualRate.isBlank() || duration.isBlank()) null
        else LoanCalculatorEngine.calculate(loanAmount, annualRate, duration, isYears)
    }

    ToolScaffold(
        tool = DailyTool.LOAN_CALCULATOR,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        onBack = onBack,
        canRequestAds = canRequestAds,
        isSdkInitialized = isSdkInitialized,
        onReset = {
            loanAmount = ""
            annualRate = ""
            duration = ""
            savedFlag = false
        }
    ) {
        NumericInputField(
            value = loanAmount,
            onValueChange = {
                loanAmount = it
                savedFlag = false
            },
            label = "Loan amount",
            placeholder = "e.g. 10000",
            imeAction = ImeAction.Next,
            testTag = "loan_amount_input"
        )

        NumericInputField(
            value = annualRate,
            onValueChange = {
                annualRate = it
                savedFlag = false
            },
            label = "Annual interest rate",
            suffixText = "%",
            imeAction = ImeAction.Next,
            testTag = "loan_rate_input"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumericInputField(
                value = duration,
                onValueChange = {
                    duration = it
                    savedFlag = false
                },
                label = "Duration",
                suffixText = if (isYears) "yrs" else "mo",
                allowDecimal = isYears,
                modifier = Modifier.weight(1f),
                testTag = "loan_duration_input"
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                SegmentedButton(
                    selected = isYears,
                    onClick = {
                        isYears = true
                        savedFlag = false
                    },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    modifier = Modifier.testTag("loan_unit_years")
                ) { Text("Years") }

                SegmentedButton(
                    selected = !isYears,
                    onClick = {
                        isYears = false
                        savedFlag = false
                    },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    modifier = Modifier.testTag("loan_unit_months")
                ) { Text("Months") }
            }
        }

        when (result) {
            is CalcResult.Success -> {
                val out = result.data
                PrimaryResultHeroCard(
                    title = "Estimated Monthly Payment",
                    primaryValue = NumberFormatter.formatFixed2(out.monthlyPayment),
                    subtitle = "Across ${out.totalMonths} monthly payments at ${NumberFormatter.formatDecimal(out.annualInterestRate, 2)}% APR",
                    metrics = listOf(
                        "Total Repayment" to NumberFormatter.formatFixed2(out.totalRepayment),
                        "Total Interest" to NumberFormatter.formatFixed2(out.totalInterest)
                    ),
                    savedIndicator = savedFlag,
                    onSaveToHistory = {
                        onSaveHistory(
                            "Loan ${NumberFormatter.formatFixed2(out.loanAmount)} @ ${NumberFormatter.formatDecimal(out.annualInterestRate, 2)}% (${out.totalMonths}m)",
                            "${NumberFormatter.formatFixed2(out.monthlyPayment)}/mo • Interest ${NumberFormatter.formatFixed2(out.totalInterest)}"
                        )
                        savedFlag = true
                    }
                )

                // Principal vs Interest Visual Breakdown
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Principal (${NumberFormatter.formatDecimal(out.principalSharePercent, 1)}%)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Interest (${NumberFormatter.formatDecimal(out.interestSharePercent, 1)}%)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(100.dp)),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val principalWeight = (out.principalSharePercent.toFloat()).coerceAtLeast(1f)
                        val interestWeight = (out.interestSharePercent.toFloat()).coerceAtLeast(0.01f)
                        Box(
                            modifier = Modifier
                                .weight(principalWeight)
                                .height(12.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        if (out.totalInterest > 0.0) {
                            Box(
                                modifier = Modifier
                                    .weight(interestWeight)
                                    .height(12.dp)
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                        }
                    }
                }
            }
            is CalcResult.Error -> ErrorBanner(message = result.message)
            null -> {
                Text(
                    text = "Enter loan amount, annual interest rate, and duration to estimate payments.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DisclaimerCard(text = stringResource(R.string.loan_disclaimer))
    }
}
