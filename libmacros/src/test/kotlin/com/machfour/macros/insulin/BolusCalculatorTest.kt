package com.machfour.macros.insulin

import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.Nutrient
import com.machfour.macros.nutrients.CARBOHYDRATE
import com.machfour.macros.nutrients.FAT
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.nutrients.PROTEIN
import com.machfour.macros.units.GRAMS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BolusCalculatorTest {

    private val testData = mapOf(
        "icr10" to BolusCalculator(icRatio = 10.0),
        "icr10 f3" to BolusCalculator(icRatio = 10.0, fatF = 3.0),
        "icr10 p4" to BolusCalculator(icRatio = 10.0, proteinF = 4.0),
        "icr10 f3 p4" to BolusCalculator(icRatio = 10.0, fatF = 3.0, proteinF = 4.0),
        "icr100" to BolusCalculator(icRatio = 100.0),
    )

    @Test
    fun getIfRatio() {
        val expected = mapOf(
            "icr10" to null,
            "icr10 f3" to 30.0,
            "icr10 p4" to null,
            "icr10 f3 p4" to 30.0,
            "icr100" to null,
        )

        val got = testData.mapValues { it.value.ifRatio }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assertEquals(e, g, "ifRatio: expected $e for $name, got $g")
        }
    }

    @Test
    fun getIpRatio() {
        val expected = mapOf(
            "icr10" to null,
            "icr10 f3" to null,
            "icr10 p4" to 40.0,
            "icr10 f3 p4" to 40.0,
            "icr100" to null,
        )

        val got = testData.mapValues { it.value.ipRatio }

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
            "icr10" to 0.0,
            "icr10 f3" to 0.0,
            "icr10 p4" to 0.0,
            "icr10 f3 p4" to 0.0,
            "icr100" to 0.0,
        )

        val got = testData.mapValues { it.value.insulinForCarbs(carbs) }

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
            "icr10" to carbs / 10,
            "icr10 f3" to carbs / 10,
            "icr10 p4" to carbs / 10,
            "icr10 f3 p4" to carbs / 10,
            "icr100" to carbs / 100,
        )

        val got = testData.mapValues { it.value.insulinForCarbs(carbs) }

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
            "icr10" to null,
            "icr10 f3" to 0.0,
            "icr10 p4" to null,
            "icr10 f3 p4" to 0.0,
            "icr100" to null,
        )

        val got = testData.mapValues { it.value.insulinForFat(fat) }

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
            "icr10" to null,
            "icr10 f3" to 1.0,
            "icr10 p4" to null,
            "icr10 f3 p4" to 1.0,
            "icr100" to null,
        )

        val got = testData.mapValues { it.value.insulinForFat(fat) }

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
            "icr10" to null,
            "icr10 f3" to null,
            "icr10 p4" to 0.0,
            "icr10 f3 p4" to 0.0,
            "icr100" to null,
        )

        val got = testData.mapValues { it.value.insulinForProtein(protein) }

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
            "icr10" to null,
            "icr10 f3" to null,
            "icr10 p4" to 1.0,
            "icr10 f3 p4" to 1.0,
            "icr100" to null,
        )

        val got = testData.mapValues { it.value.insulinForProtein(protein) }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assertEquals(e, g, "calc40gProtein: expected $e for $name, got $g")
        }
    }

    @Test
    fun calcEmptyNutrientData() {
        val nd = FoodNutrientData()
        val nullMap = makeNdInsulinMap(null, null, null)

        val expected = mapOf(
            "icr10" to nullMap,
            "icr10 f3" to nullMap,
            "icr10 p4" to nullMap,
            "icr10 f3 p4" to nullMap,
            "icr100" to nullMap,
        )
        val got = testData.mapValues { it.value.insulinForNutrientData(nd) }

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
            "icr10" to makeNdInsulinMap(0.0, null, null),
            "icr10 f3" to makeNdInsulinMap(0.0, 0.0, null),
            "icr10 p4" to makeNdInsulinMap(0.0, null, 0.0),
            "icr10 f3 p4" to makeNdInsulinMap(0.0, 0.0, 0.0),
            "icr100" to makeNdInsulinMap(0.0, null, null),
        )
        val got = testData.mapValues { it.value.insulinForNutrientData(nd) }

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
            "icr10" to makeNdInsulinMap(10.0, null, null),
            "icr10 f3" to makeNdInsulinMap(10.0, 1.0, null),
            "icr10 p4" to makeNdInsulinMap(10.0, null, 1.0),
            "icr10 f3 p4" to makeNdInsulinMap(10.0, 1.0, 1.0),
            "icr100" to makeNdInsulinMap(1.0, null, null),
        )
        val got = testData.mapValues {
            it.value.insulinForNutrientData(nd)
        }

        for (name in expected.keys) {
            val e = expected.getValue(name)
            val g = got.getValue(name)
            assertEquals(e, g, "calcFullNutrientData: expected $e for $name, got $g")
        }
    }
}

private fun makeNdInsulinMap(forCarbs: Double?, forFat: Double?, forProtein: Double?): Map<Nutrient, Pair<Double?, Double?>>{
    val total = (forCarbs ?: 0.0) + (forFat ?: 0.0) + (forProtein ?: 0.0)
    val percentages = listOf(forCarbs, forFat, forProtein).map {
        it?.let { if (total > 0) it / total else null }
    }

    return mapOf(
        CARBOHYDRATE to (forCarbs to percentages[0]),
        FAT to (forFat to percentages[1]),
        PROTEIN to (forProtein to percentages[2]),
    )
}