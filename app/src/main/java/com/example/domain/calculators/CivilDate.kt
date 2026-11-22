package com.example.domain.calculators

import java.util.Calendar
import java.util.TimeZone

/**
 * Pure Kotlin Gregorian calendar date using Howard Hinnant's civil/days algorithms.
 * Works identically on Android API 24+ and JVM unit tests with no java.time desugaring needed.
 */
data class CivilDate(
    val year: Int,
    val month: Int, // 1..12
    val day: Int    // 1..31
) : Comparable<CivilDate> {

    init {
        require(month in 1..12) { "Month must be 1..12" }
        require(day in 1..daysInMonth(year, month)) { "Invalid day $day for $year-$month" }
    }

    fun toEpochDays(): Long {
        val y = year.toLong() - if (month <= 2) 1L else 0L
        val era = (if (y >= 0) y else y - 399) / 400
        val yoe = y - era * 400
        val m = month.toLong()
        val doy = (153 * (m + if (m > 2) -3 else 9) + 2) / 5 + day - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097 + doe - 719468
    }

    fun toUtcMillis(): Long = toEpochDays() * MILLIS_PER_DAY

    fun plusDays(days: Long): CivilDate = fromEpochDays(toEpochDays() + days)

    fun dayOfWeekIndex(): Int {
        // 1970-01-01 (epoch 0) was Thursday (index 4 if Sun=0..Sat=6)
        val raw = ((toEpochDays() + 4) % 7 + 7) % 7
        return raw.toInt() // 0=Sun, 1=Mon, ..., 6=Sat
    }

    fun dayOfWeekName(): String = when (dayOfWeekIndex()) {
        0 -> "Sunday"
        1 -> "Monday"
        2 -> "Tuesday"
        3 -> "Wednesday"
        4 -> "Thursday"
        5 -> "Friday"
        else -> "Saturday"
    }

    fun formatted(): String {
        val monthName = MONTH_NAMES[month - 1]
        return "$monthName $day, $year"
    }

    override fun compareTo(other: CivilDate): Int {
        return compareValuesBy(this, other, { it.year }, { it.month }, { it.day })
    }

    companion object {
        private const val MILLIS_PER_DAY = 86_400_000L
        private val MONTH_NAMES = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )

        fun isLeapYear(year: Int): Boolean {
            return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
        }

        fun daysInMonth(year: Int, month: Int): Int = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> throw IllegalArgumentException("Invalid month $month")
        }

        fun fromEpochDays(epochDays: Long): CivilDate {
            val z = epochDays + 719468
            val era = (if (z >= 0) z else z - 146096) / 146097
            val doe = z - era * 146097
            val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
            val y = yoe + era * 400
            val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
            val mp = (5 * doy + 2) / 153
            val d = (doy - (153 * mp + 2) / 5 + 1).toInt()
            val m = (mp + if (mp < 10) 3 else -9).toInt()
            val finalYear = (y + if (m <= 2) 1 else 0).toInt()
            return CivilDate(finalYear, m, d)
        }

        fun fromUtcMillis(millis: Long): CivilDate {
            val epochDays = Math.floorDiv(millis, MILLIS_PER_DAY)
            return fromEpochDays(epochDays)
        }

        fun todayLocal(): CivilDate {
            val cal = Calendar.getInstance(TimeZone.getDefault())
            return CivilDate(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun daysBetween(start: CivilDate, end: CivilDate): Long {
            return end.toEpochDays() - start.toEpochDays()
        }

        /**
         * Exact calendar difference (years, months, days) assuming start <= end.
         */
        fun calendarDifference(start: CivilDate, end: CivilDate): Triple<Int, Int, Int> {
            val (earlier, later) = if (start <= end) start to end else end to start
            var years = later.year - earlier.year
            var months = later.month - earlier.month
            var days = later.day - earlier.day

            if (days < 0) {
                months -= 1
                val prevMonth = if (later.month == 1) 12 else later.month - 1
                val prevMonthYear = if (later.month == 1) later.year - 1 else later.year
                days += daysInMonth(prevMonthYear, prevMonth)
            }
            if (months < 0) {
                years -= 1
                months += 12
            }
            return Triple(years, months, days)
        }
    }
}
