package com.machfour.macros.insulin

import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.Nutrient
import com.machfour.macros.nutrients.*
import com.machfour.macros.units.GRAMS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SimpleBolusCalculatorTest {

    private val testCalculators = mapOf(
        "icr10" to SimpleBolusCalculator(icRatio = 10.0),
        "icr10 f3" to SimpleBolusCalculator(icRatio = 10.0, fatF = 3.0),
        "icr10 p4" to SimpleBolusCalculator(icRatio = 10.0, proteinF = 4.0),
        "icr10 f3 p4" to SimpleBolusCalculator(icRatio = 10.0, fatF = 3.0, proteinF = 4.0),
        "icr100" to SimpleBolusCalculator(icRatio = 100.0),
    )

    @Test
    fun getIfRatio() {
        val expected = mapOf(
            "icr10" to 0.0,
            "icr10 f3" to 30.0,
            "icr10 p4" to 0.0,
            "icr10 f3 p4" to 30.0,
            "icr100" to 0.0,
        )

        val got = testCalculators.mapValues { it.value.ifRatio }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assertEquals(e, g, "ifRatio: expected $e for $name, got $g")
        }
    }

    @Test
    fun getIpRatio() {
        val expected = mapOf(
            "icr10" to 0.0,
            "icr10 f3" to 0.0,
            "icr10 p4" to 40.0,
            "icr10 f3 p4" to 40.0,
            "icr100" to 0.0,
        )

        val got = testCalculators.mapValues { it.value.ipRatio }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assertEquals(e, g, "ipRatio: expected $e for $name, got $g")
        }
    }

    @Test
    fun calc0gCarbs() {
        val carbs = 0.0

        val expected = mapOf(
            "icr10" to insulinAmounts(
                carbsUpfront = 0.0,
            ),
            "icr10 f3" to insulinAmounts(
                carbsUpfront = 0.0,
            ),
            "icr10 p4" to insulinAmounts(
                carbsUpfront = 0.0,
            ),
            "icr10 f3 p4" to insulinAmounts(
                carbsUpfront = 0.0,
            ),
            "icr100" to insulinAmounts(
                carbsUpfront = 0.0,
            ),
        )

        val got = testCalculators.mapValues { it.value.insulinFor(carbs = carbs) }

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
            ),
            "icr10 f3" to insulinAmounts(
                carbsUpfront = carbs / 10,
            ),
            "icr10 p4" to insulinAmounts(
                carbsUpfront = carbs / 10,
            ),
            "icr10 f3 p4" to insulinAmounts(
                carbsUpfront = carbs / 10,
            ),
            "icr100" to insulinAmounts(
                carbsUpfront = carbs / 100,
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
    fun calc0gFat() {
        val fat = 0.0

        val expected = mapOf(
            "icr10" to insulinAmounts(
                fatExtended = null,
            ),
            "icr10 f3" to insulinAmounts(
                fatExtended = 0.0,
            ),
            "icr10 p4" to insulinAmounts(
                fatExtended = null,
            ),
            "icr10 f3 p4" to insulinAmounts(
                fatExtended = 0.0,
            ),
            "icr100" to insulinAmounts(
                fatExtended = null,
            ),
        )

        val got = testCalculators.mapValues { it.value.insulinFor(fat = fat) }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assertEquals(e, g, "calc0gFat: expected $e for $name, got $g")
        }
    }

    @Test
    fun calc30gFat() {
        val fat = 30.0

        val expected = mapOf(
            "icr10" to insulinAmounts(
                fatExtended = null,
            ),
            "icr10 f3" to insulinAmounts(
                fatExtended = 1.0,
            ),
            "icr10 p4" to insulinAmounts(
                fatExtended = null,
            ),
            "icr10 f3 p4" to insulinAmounts(
                fatExtended = 1.0,
            ),
            "icr100" to insulinAmounts(
                fatExtended = null,
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
    fun calc0gProtein() {
        val protein = 0.0

        val expected = mapOf(
            "icr10" to insulinAmounts(
                proteinExtended = null,
            ),
            "icr10 f3" to insulinAmounts(
                proteinExtended = null,
            ),
            "icr10 p4" to insulinAmounts(
                proteinExtended = 0.0,
            ),
            "icr10 f3 p4" to insulinAmounts(
                proteinExtended = 0.0,
            ),
            "icr100" to insulinAmounts(
                proteinExtended = null,
            ),
        )

        val got = testCalculators.mapValues { it.value.insulinFor(protein = protein) }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assertEquals(e, g, "calc0gProtein: expected $e for $name, got $g")
        }
    }

    @Test
    fun calc40gProtein() {
        val protein = 40.0

        val expected = mapOf(
            "icr10" to insulinAmounts(
                proteinExtended = null,
            ),
            "icr10 f3" to insulinAmounts(
                proteinExtended = null,
            ),
            "icr10 p4" to insulinAmounts(
                proteinExtended = 1.0,
            ),
            "icr10 f3 p4" to insulinAmounts(
                proteinExtended = 1.0,
            ),
            "icr100" to insulinAmounts(
                proteinExtended = null,
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
        val nullMap = insulinAmounts(
            carbsUpfront = null,
            fatExtended = null,
            proteinExtended = null,
        )

        val expected = mapOf(
            "icr10" to nullMap,
            "icr10 f3" to nullMap,
            "icr10 p4" to nullMap,
            "icr10 f3 p4" to nullMap,
            "icr100" to nullMap,
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

        val expected = mapOf(
            "icr10" to insulinAmounts(
                carbsUpfront = 0.0,
                fatExtended = null,
                proteinExtended = null,
            ),
            "icr10 f3" to insulinAmounts(
                carbsUpfront = 0.0,
                fatExtended = 0.0,
                proteinExtended = null,
            ),
            "icr10 p4" to insulinAmounts(
                carbsUpfront = 0.0,
                fatExtended = null,
                proteinExtended = 0.0,
            ),
            "icr10 f3 p4" to insulinAmounts(
                carbsUpfront = 0.0,
                fatExtended = 0.0,
                proteinExtended = 0.0,
            ),
            "icr100" to insulinAmounts(
                carbsUpfront = 0.0,
                fatExtended = null,
                proteinExtended = null,
            ),
        )
        val got = testCalculators.mapValues { it.value.insulinFor(nd) }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assertEquals(e, g, "calcZeroNutrientData: expected $e for $name, got $g")
        }
    }
    @Test
    fun calcFullNutrientData() {
        val nd = FoodNutrientData().also {
            it[CARBOHYDRATE] = FoodNutrientValue.makeComputedValue(100.0, CARBOHYDRATE, GRAMS)
            it[FAT] = FoodNutrientValue.makeComputedValue(30.0, FAT, GRAMS)
            it[PROTEIN] = FoodNutrientValue.makeComputedValue(40.0, PROTEIN, GRAMS)
        }
        val expected = mapOf(
            "icr10" to insulinAmounts(
                carbsUpfront = 10.0,
                fatExtended = null,
                proteinExtended = null,
            ),
            "icr10 f3" to insulinAmounts(
                carbsUpfront = 10.0,
                fatExtended = 1.0,
                proteinExtended = null,
            ),
            "icr10 p4" to insulinAmounts(
                carbsUpfront = 10.0,
                fatExtended = null,
                proteinExtended = 1.0,
            ),
            "icr10 f3 p4" to insulinAmounts(
                carbsUpfront = 10.0,
                fatExtended = 1.0,
                proteinExtended = 1.0,
            ),
            "icr100" to insulinAmounts(
                carbsUpfront = 1.0,
                fatExtended = null,
                proteinExtended = null,
            ),
        )
        val got = testCalculators.mapValues {
            it.value.insulinFor(nd)
        }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assertEquals(e, g, "calcFullNutrientData: expected $e for $name, got $g")
        }
    }

    @Test
    fun calcFullNutrientMap() {
        val amounts = mapOf(
            CARBOHYDRATE to 100.0,
            FAT to 30.0,
            PROTEIN to 40.0,
        )
        val expected = mapOf(
            "icr10" to insulinAndProportionsMap(10.0, null, null),
            "icr10 f3" to insulinAndProportionsMap(10.0, 1.0, null),
            "icr10 p4" to insulinAndProportionsMap(10.0, null, 1.0),
            "icr10 f3 p4" to insulinAndProportionsMap(10.0, 1.0, 1.0),
            "icr100" to insulinAndProportionsMap(1.0, null, null),
        )
        val gotInsulin = testCalculators.mapValues {
            it.value.insulinFor(amounts)
        }

        for (name in expected.keys) {
            val e = expected.getValue(name).first
            val g = gotInsulin.getValue(name)
            assertEquals(e, g, "calcFullNutrientMap: expected $e for $name, got $g")
        }

        // test proportions
        val gotProportions = gotInsulin.mapValues {
            it.value.totalNutrientProportions()
        }

        for (name in expected.keys) {
            val e = expected.getValue(name).second
            val g = gotProportions.getValue(name)
            assertEquals(e, g, "calcFullNutrientMap proportions: expected $e for $name, got $g")
        }
    }

    @Test
    fun calcFullNutrientDataWithRounding() {
        val nd = FoodNutrientData().also {
            it[CARBOHYDRATE] = FoodNutrientValue.makeComputedValue(99.6, CARBOHYDRATE, GRAMS)
            it[FAT] = FoodNutrientValue.makeComputedValue(30.3, FAT, GRAMS)
            it[PROTEIN] = FoodNutrientValue.makeComputedValue(40.4, PROTEIN, GRAMS)
        }
        val expected = mapOf(
            "icr10" to insulinAmounts(
                carbsUpfront = 10.0,
                fatExtended = null,
                proteinExtended = null,
            ),
            "icr10 f3" to insulinAmounts(
                carbsUpfront = 10.0,
                fatExtended = 1.0,
                proteinExtended = null,
            ),
            "icr10 p4" to insulinAmounts(
                carbsUpfront = 10.0,
                fatExtended = null,
                proteinExtended = 1.0,
            ),
            "icr10 f3 p4" to insulinAmounts(
                carbsUpfront = 10.0,
                fatExtended = 1.0,
                proteinExtended = 1.0,
            ),
            "icr100" to insulinAmounts(
                carbsUpfront = 1.0,
                fatExtended = null,
                proteinExtended = null,
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

private fun proportionsMap(
    forCarbs: Double? = null,
    forFat: Double? = null,
    forProtein: Double? = null,
): Map<Nutrient, Double?>{
    val total = (forCarbs ?: 0.0) + (forFat ?: 0.0) + (forProtein ?: 0.0)

    return mapOf(
        CARBOHYDRATE to (forCarbs?.div(total) ?: 0.0),
        FAT to (forFat?.div(total) ?: 0.0),
        PROTEIN to (forProtein?.div(total) ?: 0.0),
        QUANTITY to 1.0,
    )
}

private fun insulinAndProportionsMap(
    forCarbs: Double? = null,
    forFat: Double? = null,
    forProtein: Double? = null,
): Pair<InsulinAmounts, Map<Nutrient, Double?>> {
    return insulinAmounts(
        carbsUpfront = forCarbs,
        fatExtended = forFat,
        proteinExtended = forProtein,
    ) to proportionsMap(
        forCarbs = forCarbs,
        forFat = forFat,
        forProtein = forProtein,
    )
}