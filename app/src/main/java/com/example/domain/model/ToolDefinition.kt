package com.example.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory(val title: String) {
    ALL("All"),
    MATH("Math"),
    FINANCE("Finance"),
    CONVERTERS("Converters"),
    TIME_DATE("Date & Age"),
    HEALTH_TRAVEL("Health & Travel")
}

enum class DailyTool(
    val id: String,
    val title: String,
    val shortTitle: String,
    val description: String,
    val category: ToolCategory,
    val icon: ImageVector,
    val isFeatured: Boolean,
    val keywords: List<String>
) {
    SMART_CALCULATOR(
        id = "smart_calculator",
        title = "Smart Calculator",
        shortTitle = "Calculator",
        description = "Fast arithmetic, percentages & decimals",
        category = ToolCategory.MATH,
        icon = Icons.Rounded.Calculate,
        isFeatured = true,
        keywords = listOf("calc", "math", "add", "subtract", "multiply", "divide", "percent", "arithmetic")
    ),
    PERCENTAGE_CALCULATOR(
        id = "percentage_calculator",
        title = "Percentage Calculator",
        shortTitle = "Percentage",
        description = "Increase, decrease, ratio & share %",
        category = ToolCategory.MATH,
        icon = Icons.Rounded.Percent,
        isFeatured = true,
        keywords = listOf("percent", "ratio", "increase", "decrease", "markup", "margin")
    ),
    DISCOUNT_CALCULATOR(
        id = "discount_calculator",
        title = "Discount Calculator",
        shortTitle = "Discount",
        description = "Sale price & savings breakdown",
        category = ToolCategory.FINANCE,
        icon = Icons.Rounded.LocalOffer,
        isFeatured = true,
        keywords = listOf("sale", "off", "savings", "shopping", "price", "promo")
    ),
    UNIT_CONVERTER(
        id = "unit_converter",
        title = "Unit Converter",
        shortTitle = "Units",
        description = "Length, weight, temperature & volume",
        category = ToolCategory.CONVERTERS,
        icon = Icons.Rounded.Straighten,
        isFeatured = true,
        keywords = listOf("km", "mile", "kg", "pound", "celsius", "fahrenheit", "liter", "gallon", "convert")
    ),
    AGE_CALCULATOR(
        id = "age_calculator",
        title = "Age Calculator",
        shortTitle = "Age",
        description = "Exact age, days lived & birthday countdown",
        category = ToolCategory.TIME_DATE,
        icon = Icons.Rounded.Cake,
        isFeatured = false,
        keywords = listOf("birthday", "dob", "years", "months", "days", "countdown")
    ),
    DATE_CALCULATOR(
        id = "date_calculator",
        title = "Date Calculator",
        shortTitle = "Date Diff",
        description = "Days between dates, add or subtract days",
        category = ToolCategory.TIME_DATE,
        icon = Icons.Rounded.CalendarMonth,
        isFeatured = false,
        keywords = listOf("calendar", "days", "weeks", "duration", "deadline", "schedule")
    ),
    BMI_CALCULATOR(
        id = "bmi_calculator",
        title = "BMI Calculator",
        shortTitle = "BMI",
        description = "Body mass index & healthy weight guide",
        category = ToolCategory.HEALTH_TRAVEL,
        icon = Icons.Rounded.MonitorWeight,
        isFeatured = false,
        keywords = listOf("health", "weight", "height", "fitness", "body mass")
    ),
    TIP_CALCULATOR(
        id = "tip_calculator",
        title = "Tip Calculator",
        shortTitle = "Tip & Split",
        description = "Split bills & calculate gratuity easily",
        category = ToolCategory.FINANCE,
        icon = Icons.AutoMirrored.Rounded.ReceiptLong,
        isFeatured = false,
        keywords = listOf("bill", "restaurant", "split", "gratuity", "dining")
    ),
    LOAN_CALCULATOR(
        id = "loan_calculator",
        title = "Loan Calculator",
        shortTitle = "Loan EMI",
        description = "Monthly payment, total interest & payoff",
        category = ToolCategory.FINANCE,
        icon = Icons.Rounded.AccountBalance,
        isFeatured = false,
        keywords = listOf("mortgage", "emi", "interest", "finance", "repayment", "bank")
    ),
    FUEL_COST_CALCULATOR(
        id = "fuel_cost_calculator",
        title = "Fuel Cost Calculator",
        shortTitle = "Fuel Cost",
        description = "Trip fuel consumption & total gas cost",
        category = ToolCategory.HEALTH_TRAVEL,
        icon = Icons.Rounded.LocalGasStation,
        isFeatured = false,
        keywords = listOf("gas", "petrol", "diesel", "mileage", "mpg", "trip", "drive")
    );

    companion object {
        fun fromId(id: String): DailyTool? = entries.firstOrNull { it.id == id }
    }
}
