package com.example.domain.calculators

data class TipOutput(
    val billAmount: Double,
    val tipPercent: Double,
    val tipAmount: Double,
    val totalBill: Double,
    val splitCount: Int,
    val amountPerPerson: Double,
    val tipPerPerson: Double
)

object TipCalculatorEngine {
    fun calculate(
        billAmountInput: String,
        tipPercentInput: String,
        splitCount: Int
    ): CalcResult<TipOutput> {
        val (bill, errBill) = NumberFormatter.parsePositiveOrZero(billAmountInput, "Bill amount")
        if (errBill != null || bill == null) return CalcResult.Error(errBill ?: "Invalid bill amount")

        val (tipPct, errTip) = NumberFormatter.parsePositiveOrZero(tipPercentInput, "Tip percentage")
        if (errTip != null || tipPct == null) return CalcResult.Error(errTip ?: "Invalid tip percentage")
        if (tipPct > 200.0) return CalcResult.Error("Tip percentage cannot exceed 200%.")

        if (splitCount < 1) return CalcResult.Error("Split count must be at least 1 person.")

        val tipAmount = bill * (tipPct / 100.0)
        val totalBill = bill + tipAmount
        val perPerson = totalBill / splitCount
        val tipPerPerson = tipAmount / splitCount

        return CalcResult.Success(
            TipOutput(
                billAmount = bill,
                tipPercent = tipPct,
                tipAmount = tipAmount,
                totalBill = totalBill,
                splitCount = splitCount,
                amountPerPerson = perPerson,
                tipPerPerson = tipPerPerson
            )
        )
    }
}
