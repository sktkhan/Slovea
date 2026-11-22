package com.example.domain.calculators

import java.math.BigDecimal
import java.math.MathContext

/**
 * Pure Kotlin Smart Calculator engine supporting +, -, ×, ÷, %, parentheses, decimals,
 * sign toggle, backspace, and live evaluation preview with BigDecimal precision.
 */
object SmartCalculatorEngine {

    private val mathContext = MathContext.DECIMAL64
    private val oneHundred = BigDecimal(100)

    fun evaluate(rawExpression: String): CalcResult<String> {
        val cleaned = rawExpression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace(",", "")
            .trim()

        if (cleaned.isEmpty()) {
            return CalcResult.Error("Enter an expression")
        }

        return try {
            val parser = Parser(cleaned)
            val value = parser.parseExpression()
            if (!parser.isAtEnd()) {
                return CalcResult.Error("Invalid syntax")
            }
            CalcResult.Success(NumberFormatter.formatBigDecimal(value))
        } catch (e: ArithmeticException) {
            CalcResult.Error(e.message ?: "Cannot divide by zero")
        } catch (e: IllegalArgumentException) {
            CalcResult.Error(e.message ?: "Invalid expression")
        }
    }

    fun appendDigitOrDot(current: String, token: String): String {
        if (current == "0" && token != ".") {
            return token
        }
        val base = if (current.endsWith(")") || current.endsWith("%")) {
            "${current}×"
        } else {
            current
        }
        if (token == ".") {
            // Prevent multiple dots inside the active number segment
            val lastNumber = extractLastNumberSegment(base)
            if (lastNumber.contains('.')) return base
            return if (lastNumber.isEmpty()) "${base}0." else "$base."
        }
        return base + token
    }

    fun appendOperator(current: String, op: String): String {
        if (current.isEmpty()) {
            return if (op == "-") "-" else ""
        }
        val last = current.last()
        if (last in charArrayOf('+', '-', '×', '÷', '*', '/')) {
            if (op == "-" && last != '-') {
                return current + op
            }
            return current.dropLast(1) + op
        }
        if (last == '(' && op != "-") return current
        return current + op
    }

    fun appendPercent(current: String): String {
        if (current.isEmpty()) return current
        val last = current.last()
        if (last.isDigit() || last == ')') {
            return "$current%"
        }
        return current
    }

    fun toggleSign(current: String): String {
        if (current.isEmpty() || current == "0") return current
        // Find start of trailing number
        var idx = current.length - 1
        while (idx >= 0 && (current[idx].isDigit() || current[idx] == '.')) {
            idx--
        }
        val numberStart = idx + 1
        if (numberStart >= current.length) return current
        val prefix = current.substring(0, numberStart)
        val number = current.substring(numberStart)
        return when {
            prefix.endsWith("(-") -> prefix.dropLast(2) + number
            prefix == "-" -> number
            prefix.isEmpty() -> "-$number"
            prefix.last() in charArrayOf('+', '×', '÷', '*', '/', '(') -> "${prefix}(-$number"
            prefix.last() == '-' -> prefix.dropLast(1) + "+$number"
            else -> "${prefix}(-$number"
        }
    }

    fun appendParenthesis(current: String): String {
        val openCount = current.count { it == '(' }
        val closeCount = current.count { it == ')' }
        val last = current.lastOrNull()
        return if (openCount > closeCount && last != null && (last.isDigit() || last == ')' || last == '%')) {
            "$current)"
        } else if (last != null && (last.isDigit() || last == ')' || last == '%')) {
            "$current×("
        } else {
            "$current("
        }
    }

    fun backspace(current: String): String {
        return if (current.length <= 1) "" else current.dropLast(1)
    }

    private fun extractLastNumberSegment(expr: String): String {
        var idx = expr.length - 1
        while (idx >= 0 && (expr[idx].isDigit() || expr[idx] == '.')) {
            idx--
        }
        return expr.substring(idx + 1)
    }

    private class Parser(private val input: String) {
        private var pos = 0

        fun isAtEnd(): Boolean {
            skipWhitespace()
            return pos >= input.length
        }

        private fun skipWhitespace() {
            while (pos < input.length && input[pos].isWhitespace()) {
                pos++
            }
        }

        private fun peek(): Char? {
            skipWhitespace()
            return if (pos < input.length) input[pos] else null
        }

        private data class TermValue(val value: BigDecimal, val isPercent: Boolean)

        fun parseExpression(): BigDecimal {
            var left = parseTerm().value
            while (true) {
                val op = peek()
                if (op == '+' || op == '-') {
                    pos++
                    val right = parseTerm()
                    val adjustment = if (right.isPercent) {
                        left.multiply(right.value, mathContext)
                    } else {
                        right.value
                    }
                    left = if (op == '+') left.add(adjustment, mathContext) else left.subtract(adjustment, mathContext)
                } else {
                    break
                }
            }
            return left
        }

        private fun parseTerm(): TermValue {
            var left = parseFactor()
            while (true) {
                val op = peek()
                if (op == '*' || op == '/') {
                    pos++
                    val right = parseFactor()
                    val result = if (op == '*') {
                        left.value.multiply(right.value, mathContext)
                    } else {
                        if (right.value.compareTo(BigDecimal.ZERO) == 0) {
                            throw ArithmeticException("Cannot divide by zero")
                        }
                        left.value.divide(right.value, mathContext)
                    }
                    left = TermValue(result, isPercent = false)
                } else {
                    break
                }
            }
            return left
        }

        private fun parseFactor(): TermValue {
            val ch = peek() ?: throw IllegalArgumentException("Incomplete expression")
            if (ch == '+') {
                pos++
                return parseFactor()
            }
            if (ch == '-') {
                pos++
                val inner = parseFactor()
                return TermValue(inner.value.negate(), inner.isPercent)
            }

            var value: BigDecimal = if (ch == '(') {
                pos++
                val inner = parseExpression()
                if (peek() != ')') {
                    throw IllegalArgumentException("Missing closing parenthesis")
                }
                pos++
                inner
            } else {
                parseNumber()
            }

            var isPercent = false
            while (peek() == '%') {
                pos++
                value = value.divide(oneHundred, mathContext)
                isPercent = true
            }
            return TermValue(value, isPercent)
        }

        private fun parseNumber(): BigDecimal {
            skipWhitespace()
            val start = pos
            var dotCount = 0
            while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) {
                if (input[pos] == '.') dotCount++
                pos++
            }
            if (start == pos || dotCount > 1) {
                throw IllegalArgumentException("Invalid number")
            }
            val mantissa = input.substring(start, pos)
            if (mantissa == ".") throw IllegalArgumentException("Invalid decimal")

            // Optional scientific notation exponent (e.g., 1E12 or 2.5E-7)
            if (pos < input.length && (input[pos] == 'E' || input[pos] == 'e')) {
                val expMarker = pos
                pos++
                if (pos < input.length && (input[pos] == '+' || input[pos] == '-')) {
                    pos++
                }
                val expDigitsStart = pos
                while (pos < input.length && input[pos].isDigit()) {
                    pos++
                }
                if (expDigitsStart == pos) {
                    pos = expMarker
                }
            }

            val token = input.substring(start, pos)
            return try {
                BigDecimal(token)
            } catch (_: NumberFormatException) {
                throw IllegalArgumentException("Invalid number")
            }
        }
    }
}
