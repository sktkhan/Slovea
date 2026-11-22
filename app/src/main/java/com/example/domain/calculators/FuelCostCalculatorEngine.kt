package com.example.domain.calculators

enum class FuelEfficiencyUnit(
    val title: String,
    val distanceUnitLabel: String,
    val volumeUnitLabel: String,
    val priceUnitLabel: String
) {
    KM_PER_LITER("km / L", "km", "L", "per Liter"),
    LITERS_PER_100KM("L / 100 km", "km", "L", "per Liter"),
    MILES_PER_GALLON("MPG (US)", "miles", "gal", "per Gallon")
}

data class FuelCostOutput(
    val distance: Double,
    val fuelEfficiency: Double,
    val fuelPrice: Double,
    val unit: FuelEfficiencyUnit,
    val estimatedFuelConsumption: Double,
    val totalFuelCost: Double,
    val costPerDistanceUnit: Double
)

object FuelCostCalculatorEngine {
    fun calculate(
        distanceInput: String,
        efficiencyInput: String,
        fuelPriceInput: String,
        unit: FuelEfficiencyUnit
    ): CalcResult<FuelCostOutput> {
        val (distance, errD) = NumberFormatter.parsePositiveOrZero(distanceInput, "Distance")
        if (errD != null || distance == null) return CalcResult.Error(errD ?: "Invalid distance")

        val (efficiency, errE) = NumberFormatter.parsePositiveOrZero(efficiencyInput, "Fuel efficiency")
        if (errE != null || efficiency == null) return CalcResult.Error(errE ?: "Invalid efficiency")
        if (efficiency <= 0.0) return CalcResult.Error("Fuel efficiency must be greater than zero.")

        val (price, errP) = NumberFormatter.parsePositiveOrZero(fuelPriceInput, "Fuel price")
        if (errP != null || price == null) return CalcResult.Error(errP ?: "Invalid fuel price")

        val fuelUsed = when (unit) {
            FuelEfficiencyUnit.KM_PER_LITER -> distance / efficiency
            FuelEfficiencyUnit.LITERS_PER_100KM -> (distance * efficiency) / 100.0
            FuelEfficiencyUnit.MILES_PER_GALLON -> distance / efficiency
        }

        val totalCost = fuelUsed * price
        val costPerUnit = if (distance > 0.0) totalCost / distance else 0.0

        return CalcResult.Success(
            FuelCostOutput(
                distance = distance,
                fuelEfficiency = efficiency,
                fuelPrice = price,
                unit = unit,
                estimatedFuelConsumption = fuelUsed,
                totalFuelCost = totalCost,
                costPerDistanceUnit = costPerUnit
            )
        )
    }
}
