package com.example.domain.calculators

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

sealed interface CalcResult<out T> {
    data class Success<T>(val data: T) : CalcResult<T>
    data class Error(val message: String) : CalcResult<Nothing>
}

object NumberFormatter {
    private val usSymbols = DecimalFormatSymbols(Locale.US)

    fun formatDecimal(value: Double, maxFractionDigits: Int = 2, grouping: Boolean = true): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        val bd = BigDecimal.valueOf(value).setScale(maxFractionDigits, RoundingMode.HALF_UP).stripTrailingZeros()
        val clean = if (bd.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else bd
        val pattern = buildString {
            append(if (grouping) "#,##0" else "0")
            if (maxFractionDigits > 0) {
                append(".")
                repeat(maxFractionDigits) { append("#") }
            }
        }
        return DecimalFormat(pattern, usSymbols).format(clean)
    }

    fun formatFixed2(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        val bd = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP)
        return DecimalFormat("#,##0.00", usSymbols).format(bd)
    }

    fun formatBigDecimal(bd: BigDecimal, maxFractionDigits: Int = 10): String {
        val scaled = bd.setScale(maxFractionDigits, RoundingMode.HALF_UP).stripTrailingZeros()
        val normalized = if (scaled.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else scaled
        return if (normalized.abs() >= BigDecimal("1E12") ||
            (normalized.compareTo(BigDecimal.ZERO) != 0 && normalized.abs() < BigDecimal("1E-7"))
        ) {
            DecimalFormat("0.######E0", usSymbols).format(normalized)
        } else {
            normalized.toPlainString()
        }
    }

    fun parsePositiveOrZero(raw: String, fieldName: String): Pair<Double?, String?> {
        val trimmed = raw.trim().replace(",", "")
        if (trimmed.isEmpty()) return null to "Enter $fieldName."
        val num = trimmed.toDoubleOrNull() ?: return null to "$fieldName must be a valid number."
        if (num.isNaN() || num.isInfinite()) return null to "$fieldName is out of range."
        if (num < 0.0) return null to "$fieldName cannot be negative."
        return num to null
    }

    fun parseNumber(raw: String, fieldName: String): Pair<Double?, String?> {
        val trimmed = raw.trim().replace(",", "")
        if (trimmed.isEmpty()) return null to "Enter $fieldName."
        val num = trimmed.toDoubleOrNull() ?: return null to "$fieldName must be a valid number."
        if (num.isNaN() || num.isInfinite()) return null to "$fieldName is out of range."
        return num to null
    }
}
