package com.example

import com.example.ads.InterstitialFrequencyGuard
import com.example.domain.calculators.AgeCalculatorEngine
import com.example.domain.calculators.BmiCalculatorEngine
import com.example.domain.calculators.BmiCategory
import com.example.domain.calculators.CalcResult
import com.example.domain.calculators.CivilDate
import com.example.domain.calculators.DateCalculatorEngine
import com.example.domain.calculators.DiscountCalculatorEngine
import com.example.domain.calculators.FuelCostCalculatorEngine
import com.example.domain.calculators.FuelEfficiencyUnit
import com.example.domain.calculators.LoanCalculatorEngine
import com.example.domain.calculators.MeasureUnit
import com.example.domain.calculators.PercentageCalculatorEngine
import com.example.domain.calculators.PercentageMode
import com.example.domain.calculators.SmartCalculatorEngine
import com.example.domain.calculators.TipCalculatorEngine
import com.example.domain.calculators.UnitConverterEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEnginesUnitTest {

    @Test
    fun smartCalculator_evaluatesPemdasDecimalsAndPercents() {
        val addDec = SmartCalculatorEngine.evaluate("0.1+0.2")
        assertTrue(addDec is CalcResult.Success)
        assertEquals("0.3", (addDec as CalcResult.Success).data)

        val pemdas = SmartCalculatorEngine.evaluate("2+3×4")
        assertEquals("14", (pemdas as CalcResult.Success).data)

        val pctAdd = SmartCalculatorEngine.evaluate("200+10%")
        assertEquals("220", (pctAdd as CalcResult.Success).data)

        val pctSub = SmartCalculatorEngine.evaluate("200-15%")
        assertEquals("170", (pctSub as CalcResult.Success).data)

        val divZero = SmartCalculatorEngine.evaluate("10÷0")
        assertTrue(divZero is CalcResult.Error)

        val syntaxErr = SmartCalculatorEngine.evaluate("5+×2")
        assertTrue(syntaxErr is CalcResult.Error)

        val sciNotation = SmartCalculatorEngine.evaluate("1E12+5E11")
        assertTrue(sciNotation is CalcResult.Success)
        assertEquals("1.5E12", (sciNotation as CalcResult.Success).data)

        val sciDiv = SmartCalculatorEngine.evaluate("1E12÷2")
        assertTrue(sciDiv is CalcResult.Success)
        assertEquals("500000000000", (sciDiv as CalcResult.Success).data)

        val implicitAfterParen = SmartCalculatorEngine.appendDigitOrDot("(2+3)", "4")
        assertEquals("(2+3)×4", implicitAfterParen)
        assertEquals("20", (SmartCalculatorEngine.evaluate(implicitAfterParen) as CalcResult.Success).data)
    }

    @Test
    fun percentageCalculator_allFiveModesAndZeroValidation() {
        val whatPct = PercentageCalculatorEngine.calculate(PercentageMode.WHAT_PERCENT_OF, "25", "200")
        assertEquals("12.5%", (whatPct as CalcResult.Success).data.primaryValue)

        val inc = PercentageCalculatorEngine.calculate(PercentageMode.PERCENT_INCREASE, "100", "125")
        assertEquals("+25%", (inc as CalcResult.Success).data.primaryValue)

        val dec = PercentageCalculatorEngine.calculate(PercentageMode.PERCENT_DECREASE, "200", "150")
        assertEquals("25%", (dec as CalcResult.Success).data.primaryValue)

        val addPct = PercentageCalculatorEngine.calculate(PercentageMode.ADD_PERCENTAGE, "80", "25")
        assertEquals("100", (addPct as CalcResult.Success).data.primaryValue)

        val subPct = PercentageCalculatorEngine.calculate(PercentageMode.SUBTRACT_PERCENTAGE, "100", "15")
        assertEquals("85", (subPct as CalcResult.Success).data.primaryValue)

        val divZero = PercentageCalculatorEngine.calculate(PercentageMode.WHAT_PERCENT_OF, "10", "0")
        assertTrue(divZero is CalcResult.Error)
    }

    @Test
    fun discountCalculator_computesFinalAndRejectsInvalidDiscount() {
        val ok = DiscountCalculatorEngine.calculate("150", "20")
        val out = (ok as CalcResult.Success).data
        assertEquals(30.0, out.amountSaved, 0.001)
        assertEquals(120.0, out.finalPrice, 0.001)

        val tooHigh = DiscountCalculatorEngine.calculate("100", "120")
        assertTrue(tooHigh is CalcResult.Error)

        val negativePrice = DiscountCalculatorEngine.calculate("-50", "10")
        assertTrue(negativePrice is CalcResult.Error)
    }

    @Test
    fun unitConverter_convertsLengthWeightTempVolumeAndValidatesAbsoluteZero() {
        val kmToMi = UnitConverterEngine.convert("1", MeasureUnit.KILOMETER, MeasureUnit.MILE)
        assertEquals(0.621371, (kmToMi as CalcResult.Success).data.primaryValue, 0.0001)

        val kgToLb = UnitConverterEngine.convert("10", MeasureUnit.KILOGRAM, MeasureUnit.POUND)
        assertEquals(22.0462, (kgToLb as CalcResult.Success).data.primaryValue, 0.001)

        val cToF = UnitConverterEngine.convert("100", MeasureUnit.CELSIUS, MeasureUnit.FAHRENHEIT)
        assertEquals(212.0, (cToF as CalcResult.Success).data.primaryValue, 0.001)

        val galToL = UnitConverterEngine.convert("1", MeasureUnit.GALLON, MeasureUnit.LITER)
        assertEquals(3.78541, (galToL as CalcResult.Success).data.primaryValue, 0.001)

        val belowAbsZero = UnitConverterEngine.convert("-10", MeasureUnit.KELVIN, MeasureUnit.CELSIUS)
        assertTrue(belowAbsZero is CalcResult.Error)
    }

    @Test
    fun ageCalculator_calculatesExactAgeAndFutureError() {
        val dob = CivilDate(1995, 3, 10)
        val ref = CivilDate(2026, 11, 21)
        val res = AgeCalculatorEngine.calculate(dob, ref)
        val out = (res as CalcResult.Success).data
        assertEquals(31, out.years)
        assertEquals(8, out.months)
        assertEquals(11, out.days)
        assertTrue(out.totalDaysLived > 11000)

        val future = AgeCalculatorEngine.calculate(CivilDate(2030, 1, 1), ref)
        assertTrue(future is CalcResult.Error)
    }

    @Test
    fun dateCalculator_betweenAddAndSubtractDays() {
        val start = CivilDate(2026, 1, 1)
        val end = CivilDate(2026, 1, 15)
        val diff = DateCalculatorEngine.calculateDifference(start, end)
        assertEquals(14L, diff.absoluteDays)
        assertEquals(2L, diff.weeks)
        assertEquals(0L, diff.remainingDays)
        assertEquals(10L, diff.weekdaysCount)

        val added = DateCalculatorEngine.shiftDate(start, "31", isAdd = true)
        assertEquals(CivilDate(2026, 2, 1), (added as CalcResult.Success).data.resultDate)

        val subtracted = DateCalculatorEngine.shiftDate(CivilDate(2026, 3, 1), "1", isAdd = false)
        assertEquals(CivilDate(2026, 2, 28), (subtracted as CalcResult.Success).data.resultDate)
    }

    @Test
    fun bmiCalculator_metricAndImperialCategories() {
        val normal = BmiCalculatorEngine.calculateMetric("70", "175")
        val normalOut = (normal as CalcResult.Success).data
        assertEquals(22.86, normalOut.bmi, 0.05)
        assertEquals(BmiCategory.NORMAL, normalOut.category)

        val imperial = BmiCalculatorEngine.calculateImperial("200", "5", "9")
        assertTrue(imperial is CalcResult.Success)
        assertEquals(BmiCategory.OVERWEIGHT, (imperial as CalcResult.Success).data.category)

        val invalid = BmiCalculatorEngine.calculateMetric("0", "175")
        assertTrue(invalid is CalcResult.Error)
    }

    @Test
    fun tipCalculator_computesTotalAndSplit() {
        val res = TipCalculatorEngine.calculate("120", "15", 3)
        val out = (res as CalcResult.Success).data
        assertEquals(18.0, out.tipAmount, 0.001)
        assertEquals(138.0, out.totalBill, 0.001)
        assertEquals(46.0, out.amountPerPerson, 0.001)

        val invalidSplit = TipCalculatorEngine.calculate("100", "15", 0)
        assertTrue(invalidSplit is CalcResult.Error)
    }

    @Test
    fun loanCalculator_computesAmortizationAndZeroRate() {
        val zeroRate = LoanCalculatorEngine.calculate("12000", "0", "1", isDurationInYears = true)
        val zeroOut = (zeroRate as CalcResult.Success).data
        assertEquals(1000.0, zeroOut.monthlyPayment, 0.01)
        assertEquals(0.0, zeroOut.totalInterest, 0.01)

        val standard = LoanCalculatorEngine.calculate("10000", "6", "36", isDurationInYears = false)
        val stdOut = (standard as CalcResult.Success).data
        assertEquals(304.22, stdOut.monthlyPayment, 0.05)
        assertTrue(stdOut.totalInterest > 900.0)
    }

    @Test
    fun fuelCostCalculator_supportsAllEfficiencyModes() {
        val kmPerL = FuelCostCalculatorEngine.calculate("300", "15", "2.0", FuelEfficiencyUnit.KM_PER_LITER)
        val out1 = (kmPerL as CalcResult.Success).data
        assertEquals(20.0, out1.estimatedFuelConsumption, 0.001)
        assertEquals(40.0, out1.totalFuelCost, 0.001)

        val lPer100 = FuelCostCalculatorEngine.calculate("250", "8", "1.5", FuelEfficiencyUnit.LITERS_PER_100KM)
        val out2 = (lPer100 as CalcResult.Success).data
        assertEquals(20.0, out2.estimatedFuelConsumption, 0.001)
        assertEquals(30.0, out2.totalFuelCost, 0.001)

        val zeroEff = FuelCostCalculatorEngine.calculate("100", "0", "3.0", FuelEfficiencyUnit.MILES_PER_GALLON)
        assertTrue(zeroEff is CalcResult.Error)
    }

    @Test
    fun interstitialFrequencyGuard_enforcesGraceAndCooldown() {
        var fakeTime = 1_000L
        val guard = InterstitialFrequencyGuard(
            cooldownMs = 180_000L,
            launchGraceMs = 90_000L,
            clockMs = { fakeTime }
        )

        // Blocked right after app open
        assertFalse(guard.canShowInterstitialNow())

        // Still blocked at 60s
        fakeTime += 60_000L
        assertFalse(guard.canShowInterstitialNow())

        // Allowed after 90s grace
        fakeTime += 35_000L
        assertTrue(guard.canShowInterstitialNow())

        // Record shown -> blocked for 180s
        guard.recordShown()
        assertFalse(guard.canShowInterstitialNow())

        fakeTime += 179_000L
        assertFalse(guard.canShowInterstitialNow())

        fakeTime += 1_500L
        assertTrue(guard.canShowInterstitialNow())
    }

    @Test
    fun adConfigAndToolCatalog_matchSpecification() {
        assertEquals("ca-app-pub-3781293651763401~4637525567", com.example.ads.AdConfig.ADMOB_APP_ID)
        assertEquals("ca-app-pub-3781293651763401/4652556171", com.example.ads.AdConfig.PRODUCTION_BANNER_AD_UNIT_ID)
        assertEquals("ca-app-pub-3781293651763401/5270457094", com.example.ads.AdConfig.PRODUCTION_INTERSTITIAL_AD_UNIT_ID)
        assertTrue(com.example.ads.AdConfig.useTestAds)
        assertEquals(com.example.ads.AdConfig.TEST_BANNER_AD_UNIT_ID, com.example.ads.AdConfig.bannerAdUnitId)
        assertEquals(com.example.ads.AdConfig.TEST_INTERSTITIAL_AD_UNIT_ID, com.example.ads.AdConfig.interstitialAdUnitId)
        assertEquals(10, com.example.domain.model.DailyTool.entries.size)
    }
}
