package com.machfour.macros.insulin

import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.INutrient
import com.machfour.macros.nutrients.CARBOHYDRATE
import com.machfour.macros.nutrients.FAT
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.nutrients.PROTEIN
import com.machfour.macros.units.GRAMS
import kotlin.test.Test
import kotlin.test.assertEquals

class ComplexBolusCalculatorTest {

    private val testCalculators = mapOf(
        "icr10" to ComplexBolusCalculator(icRatio = 10.0),
        "icr100" to ComplexBolusCalculator(icRatio = 100.0),
    )

    @Test
    fun getIcRatio() {
        val expected = mapOf(
            "icr10" to 10.0,
            "icr100" to 100.0,
        )

        val got = testCalculators.mapValues { it.value.icRatio }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assertEquals(e, g, "icRatio: expected $e for $name, got $g")
        }
    }

    @Test
    fun calcNothing() {
        val expected = mapOf(
            "icr10" to insulinAmounts(),
            "icr100" to insulinAmounts(),
        )

        val got = testCalculators.mapValues { it.value.insulinFor() }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assertEquals(e, g, "calc0gCarbs: expected $e for $name, got $g")
        }
    }

    @Test
    fun calc100gCarbs() {
        val carbs = 100.0

        val expected = mapOf(
            "icr10" to insulinAmounts(
                carbsUpfront = carbs / 10,
                carbsExtended = 0.0,
            ),
            "icr100" to insulinAmounts(
                carbsUpfront = carbs / 100,
                carbsExtended = 0.0,
            ),
        )

        val got = testCalculators.mapValues { it.value.insulinFor(carbs = carbs) }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assertEquals(e, g, "calc100gCarbs: expected $e for $name, got $g")
        }
    }

    @Test
    fun calc30gFat() {
        val fat = 30.0

        val expected = mapOf(
            "icr10" to insulinAmounts(
                fatUpfront = 0.0,
                fatExtended = 30 * 0.05 / 10,
            ),
            "icr100" to insulinAmounts(
                fatUpfront = 0.0,
                fatExtended = 30 * 0.05 / 100,
            ),
        )

        val got = testCalculators.mapValues { it.value.insulinFor(fat = fat) }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assertEquals(e, g, "calc100gFat: expected $e for $name, got $g")
        }
    }

    @Test
    fun calc40gProtein() {
        val protein = 40.0

        val expected = mapOf(
            "icr10" to insulinAmounts(
                proteinUpfront = 0.0,
                proteinExtended = 40 * 0.25 / 10,
            ),
            "icr100" to insulinAmounts(
                proteinUpfront = 0.0,
                proteinExtended = 40 * 0.25 / 100,
            ),
        )

        val got = testCalculators.mapValues { it.value.insulinFor(protein = protein) }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assertEquals(e, g, "calc40gProtein: expected $e for $name, got $g")
        }
    }

    @Test
    fun calcEmptyNutrientData() {
        val nd = FoodNutrientData()

        val expected = mapOf(
            "icr10" to insulinAmounts(),
            "icr100" to insulinAmounts(),
        )
        val got = testCalculators.mapValues { it.value.insulinFor(nd) }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assertEquals(e, g, "calcEmptyNutrientData: expected $e for $name, got $g")
        }
    }

    @Test
    fun calcZeroNutrientData() {
        val nd = FoodNutrientData().also {
            it[CARBOHYDRATE] = FoodNutrientValue.makeComputedValue(0.0, CARBOHYDRATE, GRAMS)
            it[FAT] = FoodNutrientValue.makeComputedValue(0.0, FAT, GRAMS)
            it[PROTEIN] = FoodNutrientValue.makeComputedValue(0.0, PROTEIN, GRAMS)
        }

        val allZero = insulinAmounts(
            carbsUpfront = 0.0,
            carbsExtended = 0.0,
            fatUpfront = 0.0,
            fatExtended = 0.0,
            proteinUpfront = 0.0,
            proteinExtended = 0.0,
        )

        val expected = mapOf(
            "icr10" to allZero,
            "icr100" to allZero,
        )
        val got = testCalculators.mapValues { it.value.insulinFor(nd) }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assert(e.equals(g))
        }
    }
    @Test
    fun calcHighCarbProteinFatNutrientData() {
        val nd = FoodNutrientData().also {
            it[CARBOHYDRATE] = FoodNutrientValue.makeComputedValue(100.0, CARBOHYDRATE, GRAMS)
            it[FAT] = FoodNutrientValue.makeComputedValue(40.0, FAT, GRAMS)
            it[PROTEIN] = FoodNutrientValue.makeComputedValue(60.0, PROTEIN, GRAMS)
        }
        val expected = mapOf(
            "icr10" to insulinAmounts(
                carbsUpfront = 20.0/3,
                carbsExtended = 10.0/3,
                fatUpfront = 0.0,
                fatExtended = 1.6,
                proteinUpfront = 12.0/5,
                proteinExtended = 3.0/5,
            ),
            "icr100" to insulinAmounts(
                carbsUpfront = 2.0/3,
                carbsExtended = 1.0/3,
                fatUpfront = 0.0,
                fatExtended = 0.16,
                proteinUpfront = 1.2/5,
                proteinExtended = 0.3/5,
            ),
        )
        val got = testCalculators.mapValues {
            it.value.insulinFor(nd)
        }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assert(e.equals(g)) { "expected $e for $name, got $g" }
        }
    }

    @Test
    fun calcLowCarbHighFatMap() {
        val amounts: Map<INutrient, Double> = mapOf(
            CARBOHYDRATE to 5.0,
            FAT to 100.0,
            PROTEIN to 20.0,
        )
        val expected = mapOf(
            "icr10" to insulinAmounts(
                carbsUpfront = 0.5,
                carbsExtended = 0.0,
                fatUpfront = 0.0,
                fatExtended = 0.5,
                proteinUpfront = 0.0,
                proteinExtended = 0.5,
            ),
            "icr100" to insulinAmounts(
                carbsUpfront = 0.05,
                carbsExtended = 0.0,
                fatUpfront = 0.0,
                fatExtended = 0.05,
                proteinUpfront = 0.0,
                proteinExtended = 0.05,
            ),
        )
        val gotInsulin = testCalculators.mapValues {
            it.value.insulinFor(amounts)
        }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = gotInsulin.getValue(name)
            assert(e.equals(g)) { "expected $e for $name, got $g" }
        }
    }

    @Test
    fun calcHighCarbHighProteinNutrientData() {
        val nd = FoodNutrientData().also {
            it[CARBOHYDRATE] = FoodNutrientValue.makeComputedValue(99.6, CARBOHYDRATE, GRAMS)
            it[FAT] = FoodNutrientValue.makeComputedValue(15.6, FAT, GRAMS)
            it[PROTEIN] = FoodNutrientValue.makeComputedValue(80.4, PROTEIN, GRAMS)
        }
        val expected = mapOf(
            "icr10" to insulinAmounts(
                carbsUpfront = 99.6 / 10 * (1 - 15.6/40/3),
                carbsExtended = 99.6 / 10 * (15.6/40/3),
                fatUpfront =  0.0,
                fatExtended = 15.6 * (15.6 / 100) / 10,
                proteinUpfront = 80.4 * 0.5 / 10 * 4 / 5,
                proteinExtended = 80.4 * 0.5 / 10 / 5,
            ),
            "icr100" to insulinAmounts(
                carbsUpfront = 99.6 / 100 * (1 - 15.6/40/3),
                carbsExtended = 99.6 / 100 * (15.6/40/3),
                fatUpfront =  0.0,
                fatExtended = 15.6 * (15.6 / 100) / 100,
                proteinUpfront = 80.4 * 0.5 / 100 * 4 / 5,
                proteinExtended = 80.4 * 0.5 / 100 / 5,
            ),
        )
        val got = testCalculators.mapValues {
            it.value.insulinFor(nd)
        }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assert(e.equals(g)) { "expected $e for $name, got $g" }
        }
    }

    @Test
    fun calcHighCarbHighProteinNutrientDataWithRounding() {
        val nd = FoodNutrientData().also {
            it[CARBOHYDRATE] = FoodNutrientValue.makeComputedValue(99.6, CARBOHYDRATE, GRAMS)
            it[FAT] = FoodNutrientValue.makeComputedValue(15.6, FAT, GRAMS)
            it[PROTEIN] = FoodNutrientValue.makeComputedValue(80.4, PROTEIN, GRAMS)
        }
        val expected = mapOf(
            "icr10" to insulinAmounts(
                carbsUpfront = 8.7,
                carbsExtended = 1.3,
                fatUpfront = 0.0,
                fatExtended = 0.2,
                proteinUpfront = 3.2,
                proteinExtended = 0.8,
            ),
            "icr100" to insulinAmounts(
                carbsUpfront = 0.9,
                carbsExtended = 0.1,
                fatUpfront = 0.0,
                fatExtended = 0.0,
                proteinUpfront = 0.3,
                proteinExtended = 0.1,
            ),
        )
        val got = testCalculators.mapValues {
            it.value.insulinFor(nd, 1)
        }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assertEquals(e, g, "calcFullNutrientDataWithRounding: expected $e for $name, got $g")
        }
    }
}