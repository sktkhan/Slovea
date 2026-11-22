package com.example.domain.calculators

import kotlin.math.abs

data class DateDiffOutput(
    val totalDays: Long,
    val absoluteDays: Long,
    val weeks: Long,
    val remainingDays: Long,
    val years: Int,
    val months: Int,
    val days: Int,
    val approxTotalMonths: Double,
    val weekdaysCount: Long
)

data class DateShiftOutput(
    val resultDate: CivilDate,
    val dayOfWeek: String,
    val shiftedDays: Long,
    val weeks: Long,
    val remainingDays: Long
)

object DateCalculatorEngine {
    fun calculateDifference(startDate: CivilDate, endDate: CivilDate): DateDiffOutput {
        val signedDays = CivilDate.daysBetween(startDate, endDate)
        val absDays = abs(signedDays)
        val (years, months, days) = CivilDate.calendarDifference(startDate, endDate)
        val weeks = absDays / 7L
        val remaining = absDays % 7L
        val approxMonths = years * 12.0 + months + (days / 30.4375)

        // Count weekdays (Mon..Fri) between start (inclusive) and end (exclusive)
        val (earlier, later) = if (startDate <= endDate) startDate to endDate else endDate to startDate
        val weekdays = countWeekdays(earlier, later)

        return DateDiffOutput(
            totalDays = signedDays,
            absoluteDays = absDays,
            weeks = weeks,
            remainingDays = remaining,
            years = years,
            months = months,
            days = days,
            approxTotalMonths = approxMonths,
            weekdaysCount = weekdays
        )
    }

    fun shiftDate(baseDate: CivilDate, daysInput: String, isAdd: Boolean): CalcResult<DateShiftOutput> {
        val trimmed = daysInput.trim()
        if (trimmed.isEmpty()) return CalcResult.Error("Enter number of days.")
        val days = trimmed.toLongOrNull() ?: return CalcResult.Error("Days must be a whole number.")
        if (days < 0) return CalcResult.Error("Days cannot be negative. Choose Add or Subtract.")
        if (days > 365_000L) return CalcResult.Error("Please enter 365,000 days or fewer.")

        val delta = if (isAdd) days else -days
        val resultDate = baseDate.plusDays(delta)
        return CalcResult.Success(
            DateShiftOutput(
                resultDate = resultDate,
                dayOfWeek = resultDate.dayOfWeekName(),
                shiftedDays = delta,
                weeks = days / 7L,
                remainingDays = days % 7L
            )
        )
    }

    private fun countWeekdays(start: CivilDate, end: CivilDate): Long {
        val total = CivilDate.daysBetween(start, end)
        if (total <= 0L) return 0L
        val fullWeeks = total / 7L
        var weekdays = fullWeeks * 5L
        val remainder = (total % 7L).toInt()
        val startDow = start.dayOfWeekIndex()
        for (i in 0 until remainder) {
            val dow = (startDow + i) % 7
            if (dow in 1..5) weekdays++
        }
        return weekdays
    }
}
