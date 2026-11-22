package com.example.domain.calculators

data class AgeOutput(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDaysLived: Long,
    val totalWeeksLived: Long,
    val totalMonthsApprox: Int,
    val nextBirthdayDays: Long,
    val nextBirthdayDate: CivilDate,
    val isBirthdayToday: Boolean,
    val turningAge: Int
)

object AgeCalculatorEngine {
    fun calculate(
        birthDate: CivilDate,
        referenceDate: CivilDate = CivilDate.todayLocal()
    ): CalcResult<AgeOutput> {
        if (birthDate > referenceDate) {
            return CalcResult.Error("Date of birth cannot be in the future.")
        }

        val (years, months, days) = CivilDate.calendarDifference(birthDate, referenceDate)
        val totalDays = CivilDate.daysBetween(birthDate, referenceDate)
        val totalWeeks = totalDays / 7L
        val totalMonthsApprox = years * 12 + months

        val nextBirthdayThisYear = safeBirthdayInYear(birthDate.month, birthDate.day, referenceDate.year)
        val nextBirthday = if (nextBirthdayThisYear >= referenceDate) {
            nextBirthdayThisYear
        } else {
            safeBirthdayInYear(birthDate.month, birthDate.day, referenceDate.year + 1)
        }

        val countdown = CivilDate.daysBetween(referenceDate, nextBirthday)
        val isBirthdayToday = countdown == 0L
        val turningAge = if (isBirthdayToday) years else years + 1

        return CalcResult.Success(
            AgeOutput(
                years = years,
                months = months,
                days = days,
                totalDaysLived = totalDays,
                totalWeeksLived = totalWeeks,
                totalMonthsApprox = totalMonthsApprox,
                nextBirthdayDays = countdown,
                nextBirthdayDate = nextBirthday,
                isBirthdayToday = isBirthdayToday,
                turningAge = turningAge
            )
        )
    }

    private fun safeBirthdayInYear(month: Int, day: Int, year: Int): CivilDate {
        val maxDay = CivilDate.daysInMonth(year, month)
        return CivilDate(year, month, minOf(day, maxDay))
    }
}
