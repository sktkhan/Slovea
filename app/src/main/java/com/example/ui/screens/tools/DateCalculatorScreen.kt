package com.example.ui.screens.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.calculators.CalcResult
import com.example.domain.calculators.CivilDate
import com.example.domain.calculators.DateCalculatorEngine
import com.example.domain.calculators.NumberFormatter
import com.example.domain.model.DailyTool
import com.example.ui.components.ErrorBanner
import com.example.ui.components.NumericInputField
import com.example.ui.components.PresetChipRow
import com.example.ui.components.PrimaryResultHeroCard
import com.example.ui.components.ToolScaffold
import com.example.ui.theme.JetBrainsMonoFontFamily

private enum class DateCalcTab(val label: String) {
    BETWEEN("Between Dates"),
    ADD_DAYS("Add Days"),
    SUBTRACT_DAYS("Subtract Days")
}

private enum class DateTarget { START, END, BASE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateCalculatorScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSaveHistory: (String, String) -> Unit,
    onBack: () -> Unit,
    canRequestAds: Boolean,
    isSdkInitialized: Boolean
) {
    val today = remember { CivilDate.todayLocal() }
    var activeTab by rememberSaveable { mutableStateOf(DateCalcTab.BETWEEN) }
    var startEpochDays by rememberSaveable { mutableStateOf(today.toEpochDays()) }
    var endEpochDays by rememberSaveable { mutableStateOf(today.plusDays(30).toEpochDays()) }
    var baseEpochDays by rememberSaveable { mutableStateOf(today.toEpochDays()) }
    var daysInput by rememberSaveable { mutableStateOf("30") }
    var pickerTarget by remember { mutableStateOf<DateTarget?>(null) }
    var savedFlag by remember { mutableStateOf(false) }

    val startDate = remember(startEpochDays) { CivilDate.fromEpochDays(startEpochDays) }
    val endDate = remember(endEpochDays) { CivilDate.fromEpochDays(endEpochDays) }
    val baseDate = remember(baseEpochDays) { CivilDate.fromEpochDays(baseEpochDays) }

    ToolScaffold(
        tool = DailyTool.DATE_CALCULATOR,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        onBack = onBack,
        canRequestAds = canRequestAds,
        isSdkInitialized = isSdkInitialized,
        onReset = {
            startEpochDays = today.toEpochDays()
            endEpochDays = today.plusDays(30).toEpochDays()
            baseEpochDays = today.toEpochDays()
            daysInput = "30"
            savedFlag = false
        }
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            DateCalcTab.entries.forEachIndexed { idx, tab ->
                SegmentedButton(
                    selected = activeTab == tab,
                    onClick = {
                        activeTab = tab
                        savedFlag = false
                    },
                    shape = SegmentedButtonDefaults.itemShape(idx, DateCalcTab.entries.size),
                    modifier = Modifier.testTag("date_tab_${tab.name}")
                ) {
                    Text(tab.label, maxLines = 1)
                }
            }
        }

        if (activeTab == DateCalcTab.BETWEEN) {
            DatePickerRowCard(
                label = "Start Date",
                dateText = "${startDate.formatted()} (${startDate.dayOfWeekName()})",
                onClick = { pickerTarget = DateTarget.START },
                testTag = "date_start_picker"
            )
            DatePickerRowCard(
                label = "End Date",
                dateText = "${endDate.formatted()} (${endDate.dayOfWeekName()})",
                onClick = { pickerTarget = DateTarget.END },
                testTag = "date_end_picker"
            )

            val diff = remember(startDate, endDate) {
                DateCalculatorEngine.calculateDifference(startDate, endDate)
            }

            PrimaryResultHeroCard(
                title = "Days Between Dates",
                primaryValue = "${NumberFormatter.formatDecimal(diff.absoluteDays.toDouble(), 0)} days",
                subtitle = "${diff.years}y ${diff.months}m ${diff.days}d • ${diff.weeks}w ${diff.remainingDays}d",
                metrics = listOf(
                    "Approx. Months" to NumberFormatter.formatDecimal(diff.approxTotalMonths, 1),
                    "Weekdays" to NumberFormatter.formatDecimal(diff.weekdaysCount.toDouble(), 0)
                ),
                savedIndicator = savedFlag,
                onSaveToHistory = {
                    onSaveHistory(
                        "${startDate.formatted()} → ${endDate.formatted()}",
                        "${diff.absoluteDays} days (${diff.weeks}w ${diff.remainingDays}d)"
                    )
                    savedFlag = true
                }
            )
        } else {
            val isAdd = activeTab == DateCalcTab.ADD_DAYS
            DatePickerRowCard(
                label = "Starting Date",
                dateText = "${baseDate.formatted()} (${baseDate.dayOfWeekName()})",
                onClick = { pickerTarget = DateTarget.BASE },
                testTag = "date_base_picker"
            )

            NumericInputField(
                value = daysInput,
                onValueChange = {
                    daysInput = it
                    savedFlag = false
                },
                label = if (isAdd) "Days to add" else "Days to subtract",
                allowDecimal = false,
                testTag = "date_days_input"
            )

            PresetChipRow(
                options = listOf("7", "14", "30", "60", "90", "365").map { "$it d" },
                selectedOption = "$daysInput d",
                onSelect = {
                    daysInput = it.removeSuffix(" d")
                    savedFlag = false
                }
            )

            val shiftResult = remember(baseDate, daysInput, isAdd) {
                if (daysInput.isBlank()) null
                else DateCalculatorEngine.shiftDate(baseDate, daysInput, isAdd)
            }

            when (shiftResult) {
                is CalcResult.Success -> {
                    val out = shiftResult.data
                    PrimaryResultHeroCard(
                        title = if (isAdd) "Date After +$daysInput Days" else "Date After −$daysInput Days",
                        primaryValue = out.resultDate.formatted(),
                        subtitle = "${out.dayOfWeek} • ${out.weeks} weeks & ${out.remainingDays} days",
                        savedIndicator = savedFlag,
                        onSaveToHistory = {
                            val sign = if (isAdd) "+" else "−"
                            onSaveHistory(
                                "${baseDate.formatted()} $sign $daysInput days",
                                "${out.resultDate.formatted()} (${out.dayOfWeek})"
                            )
                            savedFlag = true
                        }
                    )
                }
                is CalcResult.Error -> ErrorBanner(message = shiftResult.message)
                null -> Unit
            }
        }
    }

    pickerTarget?.let { target ->
        val initial = when (target) {
            DateTarget.START -> startDate
            DateTarget.END -> endDate
            DateTarget.BASE -> baseDate
        }
        val state = rememberDatePickerState(initialSelectedDateMillis = initial.toUtcMillis())
        DatePickerDialog(
            onDismissRequest = { pickerTarget = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            val picked = CivilDate.fromUtcMillis(millis)
                            when (target) {
                                DateTarget.START -> startEpochDays = picked.toEpochDays()
                                DateTarget.END -> endEpochDays = picked.toEpochDays()
                                DateTarget.BASE -> baseEpochDays = picked.toEpochDays()
                            }
                            savedFlag = false
                        }
                        pickerTarget = null
                    }
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { pickerTarget = null }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}
