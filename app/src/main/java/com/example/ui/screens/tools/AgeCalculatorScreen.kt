package com.example.ui.screens.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.calculators.AgeCalculatorEngine
import com.example.domain.calculators.CalcResult
import com.example.domain.calculators.CivilDate
import com.example.domain.calculators.NumberFormatter
import com.example.domain.model.DailyTool
import com.example.ui.components.ErrorBanner
import com.example.ui.components.PrimaryResultHeroCard
import com.example.ui.components.ToolScaffold
import com.example.ui.theme.JetBrainsMonoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeCalculatorScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSaveHistory: (String, String) -> Unit,
    onBack: () -> Unit,
    canRequestAds: Boolean,
    isSdkInitialized: Boolean
) {
    val today = remember { CivilDate.todayLocal() }
    var birthEpochDays by rememberSaveable {
        mutableStateOf(CivilDate(1998, 5, 15).toEpochDays())
    }
    var refEpochDays by rememberSaveable {
        mutableStateOf(today.toEpochDays())
    }
    var pickingForDob by remember { mutableStateOf<Boolean?>(null) }
    var savedFlag by remember { mutableStateOf(false) }

    val birthDate = remember(birthEpochDays) { CivilDate.fromEpochDays(birthEpochDays) }
    val referenceDate = remember(refEpochDays) { CivilDate.fromEpochDays(refEpochDays) }

    val result = remember(birthDate, referenceDate) {
        AgeCalculatorEngine.calculate(birthDate, referenceDate)
    }

    ToolScaffold(
        tool = DailyTool.AGE_CALCULATOR,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        onBack = onBack,
        canRequestAds = canRequestAds,
        isSdkInitialized = isSdkInitialized,
        onReset = {
            birthEpochDays = CivilDate(1998, 5, 15).toEpochDays()
            refEpochDays = today.toEpochDays()
            savedFlag = false
        }
    ) {
        DatePickerRowCard(
            label = "Date of Birth",
            dateText = "${birthDate.formatted()} (${birthDate.dayOfWeekName()})",
            onClick = { pickingForDob = true },
            testTag = "age_dob_picker"
        )

        DatePickerRowCard(
            label = "Age at Date",
            dateText = "${referenceDate.formatted()} (${referenceDate.dayOfWeekName()})",
            onClick = { pickingForDob = false },
            testTag = "age_ref_picker"
        )

        when (result) {
            is CalcResult.Success -> {
                val out = result.data
                PrimaryResultHeroCard(
                    title = "Exact Age",
                    primaryValue = "${out.years}y ${out.months}m ${out.days}d",
                    subtitle = if (out.isBirthdayToday) {
                        "Happy Birthday! Turning ${out.turningAge} today"
                    } else {
                        "Next birthday in ${NumberFormatter.formatDecimal(out.nextBirthdayDays.toDouble(), 0)} days (${out.nextBirthdayDate.formatted()})"
                    },
                    metrics = listOf(
                        "Total Days" to NumberFormatter.formatDecimal(out.totalDaysLived.toDouble(), 0),
                        "Total Weeks" to NumberFormatter.formatDecimal(out.totalWeeksLived.toDouble(), 0)
                    ),
                    savedIndicator = savedFlag,
                    onSaveToHistory = {
                        onSaveHistory(
                            "DOB ${birthDate.formatted()}",
                            "${out.years}y ${out.months}m ${out.days}d (${out.totalDaysLived} days)"
                        )
                        savedFlag = true
                    }
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Life Milestones", style = MaterialTheme.typography.titleMedium)
                        AgeSummaryRow("Approx. Months Lived", NumberFormatter.formatDecimal(out.totalMonthsApprox.toDouble(), 0))
                        AgeSummaryRow("Born On", birthDate.dayOfWeekName())
                        AgeSummaryRow("Next Birthday", "${out.nextBirthdayDate.formatted()} (Age ${out.turningAge})")
                        AgeSummaryRow("Countdown", if (out.isBirthdayToday) "Today!" else "${out.nextBirthdayDays} days")
                    }
                }
            }
            is CalcResult.Error -> ErrorBanner(message = result.message)
        }
    }

    pickingForDob?.let { isDob ->
        val initialDate = if (isDob) birthDate else referenceDate
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate.toUtcMillis())
        DatePickerDialog(
            onDismissRequest = { pickingForDob = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val picked = CivilDate.fromUtcMillis(millis)
                            if (isDob) birthEpochDays = picked.toEpochDays()
                            else refEpochDays = picked.toEpochDays()
                            savedFlag = false
                        }
                        pickingForDob = null
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { pickingForDob = null }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
fun DatePickerRowCard(
    label: String,
    dateText: String,
    onClick: () -> Unit,
    testTag: String = ""
) {
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Icon(
                imageVector = Icons.Rounded.CalendarToday,
                contentDescription = "Choose $label",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AgeSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
