package com.example.domain.calculators

import kotlin.math.pow

data class LoanOutput(
    val loanAmount: Double,
    val annualInterestRate: Double,
    val totalMonths: Int,
    val monthlyPayment: Double,
    val totalRepayment: Double,
    val totalInterest: Double,
    val principalSharePercent: Double,
    val interestSharePercent: Double
)

object LoanCalculatorEngine {
    fun calculate(
        loanAmountInput: String,
        annualRateInput: String,
        durationInput: String,
        isDurationInYears: Boolean
    ): CalcResult<LoanOutput> {
        val (principal, errP) = NumberFormatter.parsePositiveOrZero(loanAmountInput, "Loan amount")
        if (errP != null || principal == null) return CalcResult.Error(errP ?: "Invalid loan amount")
        if (principal <= 0.0) return CalcResult.Error("Loan amount must be greater than zero.")

        val (annualRate, errR) = NumberFormatter.parsePositiveOrZero(annualRateInput, "Annual interest rate")
        if (errR != null || annualRate == null) return CalcResult.Error(errR ?: "Invalid interest rate")
        if (annualRate > 100.0) return CalcResult.Error("Interest rate must be 100% or less.")

        val (durationRaw, errD) = NumberFormatter.parsePositiveOrZero(durationInput, "Loan duration")
        if (errD != null || durationRaw == null) return CalcResult.Error(errD ?: "Invalid duration")
        if (durationRaw <= 0.0) return CalcResult.Error("Loan duration must be greater than zero.")

        val totalMonths = if (isDurationInYears) {
            kotlin.math.round(durationRaw * 12.0).toInt()
        } else {
            kotlin.math.round(durationRaw).toInt()
        }

        if (totalMonths < 1 || totalMonths > 600) {
            return CalcResult.Error("Loan duration must be between 1 month and 50 years (600 months).")
        }

        val monthlyPayment = if (annualRate == 0.0) {
            principal / totalMonths
        } else {
            val monthlyRate = (annualRate / 100.0) / 12.0
            val factor = (1.0 + monthlyRate).pow(totalMonths.toDouble())
            principal * (monthlyRate * factor) / (factor - 1.0)
        }

        val totalRepayment = monthlyPayment * totalMonths
        val totalInterest = (totalRepayment - principal).coerceAtLeast(0.0)
        val principalShare = if (totalRepayment > 0.0) (principal / totalRepayment) * 100.0 else 100.0
        val interestShare = (100.0 - principalShare).coerceAtLeast(0.0)

        return CalcResult.Success(
            LoanOutput(
                loanAmount = principal,
                annualInterestRate = annualRate,
                totalMonths = totalMonths,
                monthlyPayment = monthlyPayment,
                totalRepayment = totalRepayment,
                totalInterest = totalInterest,
                principalSharePercent = principalShare,
                interestSharePercent = interestShare
            )
        )
    }
}
