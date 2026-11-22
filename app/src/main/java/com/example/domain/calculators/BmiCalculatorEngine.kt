package com.example.domain.calculators

enum class BmiUnitSystem(val title: String) {
    METRIC("Metric (kg / cm)"),
    IMPERIAL("Imperial (lb / ft-in)")
}

enum class BmiCategory(
    val label: String,
    val rangeText: String,
    val explanation: String
) {
    UNDERWEIGHT(
        label = "Underweight",
        rangeText = "Below 18.5",
        explanation = "Your BMI falls below the typical range of 18.5–24.9. Consider discussing nutrition and healthy weight goals with a healthcare provider."
    ),
    NORMAL(
        label = "Normal Weight",
        rangeText = "18.5 – 24.9",
        explanation = "Your BMI falls within the standard healthy weight screening range for most adults."
    ),
    OVERWEIGHT(
        label = "Overweight",
        rangeText = "25.0 – 29.9",
        explanation = "Your BMI is above the standard 18.5–24.9 range. Note that BMI does not distinguish between muscle mass and body fat."
    ),
    OBESITY_I(
        label = "Obesity (Class I)",
        rangeText = "30.0 – 34.9",
        explanation = "Your BMI is in the Class I obesity screening range. A clinician can evaluate individual health markers beyond BMI."
    ),
    OBESITY_II(
        label = "Obesity (Class II)",
        rangeText = "35.0 – 39.9",
        explanation = "Your BMI is in the Class II obesity screening range. Discuss personalized health guidance with a qualified clinician."
    ),
    OBESITY_III(
        label = "Obesity (Class III)",
        rangeText = "40.0 and above",
        explanation = "Your BMI is in the Class III obesity screening range. A healthcare professional can provide individualized guidance."
    );

    companion object {
        fun fromBmi(bmi: Double): BmiCategory = when {
            bmi < 18.5 -> UNDERWEIGHT
            bmi < 25.0 -> NORMAL
            bmi < 30.0 -> OVERWEIGHT
            bmi < 35.0 -> OBESITY_I
            bmi < 40.0 -> OBESITY_II
            else -> OBESITY_III
        }
    }
}

data class BmiOutput(
    val bmi: Double,
    val category: BmiCategory,
    val healthyMinWeightKg: Double,
    val healthyMaxWeightKg: Double,
    val healthyMinWeightLb: Double,
    val healthyMaxWeightLb: Double,
    val unitSystem: BmiUnitSystem
)

object BmiCalculatorEngine {
    fun calculateMetric(weightKgInput: String, heightCmInput: String): CalcResult<BmiOutput> {
        val (weightKg, errW) = NumberFormatter.parsePositiveOrZero(weightKgInput, "Weight")
        if (errW != null || weightKg == null) return CalcResult.Error(errW ?: "Invalid weight")
        if (weightKg <= 0.0 || weightKg > 500.0) {
            return CalcResult.Error("Enter a weight between 1 and 500 kg.")
        }

        val (heightCm, errH) = NumberFormatter.parsePositiveOrZero(heightCmInput, "Height")
        if (errH != null || heightCm == null) return CalcResult.Error(errH ?: "Invalid height")
        if (heightCm < 30.0 || heightCm > 280.0) {
            return CalcResult.Error("Enter a height between 30 and 280 cm.")
        }

        val heightMeters = heightCm / 100.0
        return buildOutput(weightKg, heightMeters, BmiUnitSystem.METRIC)
    }

    fun calculateImperial(
        weightLbInput: String,
        feetInput: String,
        inchesInput: String
    ): CalcResult<BmiOutput> {
        val (weightLb, errW) = NumberFormatter.parsePositiveOrZero(weightLbInput, "Weight")
        if (errW != null || weightLb == null) return CalcResult.Error(errW ?: "Invalid weight")
        if (weightLb <= 0.0 || weightLb > 1100.0) {
            return CalcResult.Error("Enter a weight between 1 and 1,100 lb.")
        }

        val (feet, errFt) = NumberFormatter.parsePositiveOrZero(feetInput, "Feet")
        if (errFt != null || feet == null) return CalcResult.Error(errFt ?: "Invalid feet")

        val inchesParsed = if (inchesInput.trim().isEmpty()) {
            0.0 to null
        } else {
            NumberFormatter.parsePositiveOrZero(inchesInput, "Inches")
        }
        val (inches, errIn) = inchesParsed
        if (errIn != null || inches == null) return CalcResult.Error(errIn ?: "Invalid inches")

        val totalInches = feet * 12.0 + inches
        if (totalInches < 12.0 || totalInches > 110.0) {
            return CalcResult.Error("Enter a height between 1 ft and 9 ft 2 in.")
        }

        val weightKg = weightLb * 0.45359237
        val heightMeters = totalInches * 0.0254
        return buildOutput(weightKg, heightMeters, BmiUnitSystem.IMPERIAL)
    }

    private fun buildOutput(
        weightKg: Double,
        heightMeters: Double,
        unitSystem: BmiUnitSystem
    ): CalcResult<BmiOutput> {
        val bmi = weightKg / (heightMeters * heightMeters)
        val minKg = 18.5 * heightMeters * heightMeters
        val maxKg = 24.9 * heightMeters * heightMeters
        return CalcResult.Success(
            BmiOutput(
                bmi = bmi,
                category = BmiCategory.fromBmi(bmi),
                healthyMinWeightKg = minKg,
                healthyMaxWeightKg = maxKg,
                healthyMinWeightLb = minKg / 0.45359237,
                healthyMaxWeightLb = maxKg / 0.45359237,
                unitSystem = unitSystem
            )
        )
    }
}
