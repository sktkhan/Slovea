package com.example.domain.calculators

enum class UnitCategory(val title: String) {
    LENGTH("Length"),
    WEIGHT("Weight"),
    TEMPERATURE("Temperature"),
    VOLUME("Volume")
}

enum class MeasureUnit(
    val category: UnitCategory,
    val label: String,
    val symbol: String,
    val toBaseFactor: Double = 1.0 // Base: meter, kilogram, liter (unused for Temperature)
) {
    // Length (Base: Meter)
    KILOMETER(UnitCategory.LENGTH, "Kilometer", "km", 1000.0),
    METER(UnitCategory.LENGTH, "Meter", "m", 1.0),
    CENTIMETER(UnitCategory.LENGTH, "Centimeter", "cm", 0.01),
    MILLIMETER(UnitCategory.LENGTH, "Millimeter", "mm", 0.001),
    MILE(UnitCategory.LENGTH, "Mile", "mi", 1609.344),
    YARD(UnitCategory.LENGTH, "Yard", "yd", 0.9144),
    FOOT(UnitCategory.LENGTH, "Foot", "ft", 0.3048),
    INCH(UnitCategory.LENGTH, "Inch", "in", 0.0254),

    // Weight (Base: Kilogram)
    KILOGRAM(UnitCategory.WEIGHT, "Kilogram", "kg", 1.0),
    GRAM(UnitCategory.WEIGHT, "Gram", "g", 0.001),
    MILLIGRAM(UnitCategory.WEIGHT, "Milligram", "mg", 0.000001),
    POUND(UnitCategory.WEIGHT, "Pound", "lb", 0.45359237),
    OUNCE(UnitCategory.WEIGHT, "Ounce", "oz", 0.028349523125),

    // Temperature
    CELSIUS(UnitCategory.TEMPERATURE, "Celsius", "°C"),
    FAHRENHEIT(UnitCategory.TEMPERATURE, "Fahrenheit", "°F"),
    KELVIN(UnitCategory.TEMPERATURE, "Kelvin", "K"),

    // Volume (Base: Liter)
    LITER(UnitCategory.VOLUME, "Liter", "L", 1.0),
    MILLILITER(UnitCategory.VOLUME, "Milliliter", "mL", 0.001),
    GALLON(UnitCategory.VOLUME, "Gallon (US)", "gal", 3.785411784);

    companion object {
        fun unitsForCategory(category: UnitCategory): List<MeasureUnit> =
            entries.filter { it.category == category }
    }
}

data class UnitConversionItem(
    val unit: MeasureUnit,
    val convertedValue: Double,
    val formattedValue: String
)

data class UnitConversionOutput(
    val primaryValue: Double,
    val primaryFormatted: String,
    val allConversions: List<UnitConversionItem>
)

object UnitConverterEngine {
    fun convert(
        rawValue: String,
        fromUnit: MeasureUnit,
        toUnit: MeasureUnit
    ): CalcResult<UnitConversionOutput> {
        if (fromUnit.category != toUnit.category) {
            return CalcResult.Error("Cannot convert between different categories.")
        }
        val (value, err) = NumberFormatter.parseNumber(rawValue, "Value")
        if (err != null || value == null) return CalcResult.Error(err ?: "Invalid value")

        if (fromUnit.category != UnitCategory.TEMPERATURE && value < 0.0) {
            return CalcResult.Error("${fromUnit.category.title} cannot be negative.")
        }

        if (fromUnit == MeasureUnit.KELVIN && value < 0.0) {
            return CalcResult.Error("Kelvin cannot be below 0 K (absolute zero).")
        }
        if (fromUnit == MeasureUnit.CELSIUS && value < -273.15) {
            return CalcResult.Error("Celsius cannot be below -273.15 °C (absolute zero).")
        }
        if (fromUnit == MeasureUnit.FAHRENHEIT && value < -459.67) {
            return CalcResult.Error("Fahrenheit cannot be below -459.67 °F (absolute zero).")
        }

        val primary = convertSingle(value, fromUnit, toUnit)
        val all = MeasureUnit.unitsForCategory(fromUnit.category).map { target ->
            val converted = convertSingle(value, fromUnit, target)
            UnitConversionItem(
                unit = target,
                convertedValue = converted,
                formattedValue = NumberFormatter.formatDecimal(converted, maxFractionDigits = 6)
            )
        }

        return CalcResult.Success(
            UnitConversionOutput(
                primaryValue = primary,
                primaryFormatted = NumberFormatter.formatDecimal(primary, maxFractionDigits = 6),
                allConversions = all
            )
        )
    }

    fun convertSingle(value: Double, from: MeasureUnit, to: MeasureUnit): Double {
        if (from == to) return value
        return if (from.category == UnitCategory.TEMPERATURE) {
            val celsius = when (from) {
                MeasureUnit.CELSIUS -> value
                MeasureUnit.FAHRENHEIT -> (value - 32.0) * 5.0 / 9.0
                MeasureUnit.KELVIN -> value - 273.15
                else -> value
            }
            when (to) {
                MeasureUnit.CELSIUS -> celsius
                MeasureUnit.FAHRENHEIT -> celsius * 9.0 / 5.0 + 32.0
                MeasureUnit.KELVIN -> celsius + 273.15
                else -> celsius
            }
        } else {
            val baseValue = value * from.toBaseFactor
            baseValue / to.toBaseFactor
        }
    }
}
