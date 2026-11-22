package com.example.domain.calculators

data class DiscountOutput(
    val originalPrice: Double,
    val discountPercent: Double,
    val amountSaved: Double,
    val finalPrice: Double
)

object DiscountCalculatorEngine {
    fun calculate(originalPriceInput: String, discountPercentInput: String): CalcResult<DiscountOutput> {
        val (price, errPrice) = NumberFormatter.parsePositiveOrZero(originalPriceInput, "Original price")
        if (errPrice != null || price == null) return CalcResult.Error(errPrice ?: "Invalid price")

        val (discount, errDisc) = NumberFormatter.parsePositiveOrZero(discountPercentInput, "Discount percentage")
        if (errDisc != null || discount == null) return CalcResult.Error(errDisc ?: "Invalid discount")

        if (discount > 100.0) {
            return CalcResult.Error("Discount percentage must be between 0% and 100%.")
        }

        val saved = price * (discount / 100.0)
        val finalPrice = (price - saved).coerceAtLeast(0.0)
        return CalcResult.Success(
            DiscountOutput(
                originalPrice = price,
                discountPercent = discount,
                amountSaved = saved,
                finalPrice = finalPrice
            )
        )
    }
}
