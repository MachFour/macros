package com.machfour.macros.nutrients

import com.machfour.macros.entities.INutrient
import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.Unit
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.KILOJOULES
import com.machfour.macros.units.StandardNutrientUnits
import com.machfour.macros.units.UnitType


const val KJ_PER_G_PROTEIN = 17.0
const val KJ_PER_G_FAT = 37.0
const val KJ_PER_G_CARBOHYDRATE = 17.0
const val KJ_PER_G_FIBRE = 8.27
const val KJ_PER_G_ALCOHOL = 29.0

// const val CAL_TO_KJ_FACTOR = 4.186

val energyProportionNutrients = setOf(
    PROTEIN,
    FAT,
    SATURATED_FAT,
    MONOUNSATURATED_FAT,
    POLYUNSATURATED_FAT,
    CARBOHYDRATE,
    SUGAR,
    STARCH,
    FIBRE,
    ALCOHOL
)

val totalEnergyNutrients = listOf(PROTEIN, FAT, CARBOHYDRATE, FIBRE)

fun BasicNutrientData<*>.makeEnergyProportionsMap(
    calculationUnit: Unit = KILOJOULES,
    energyComponentsMap: Map<INutrient, Double> = makeEnergyComponentsMap(calculationUnit),
): Map<INutrient, Double> {
    // XXX DECISION: ignore the actual energy value of the nutritionData, just use the sum
    val totalEnergy = calculateMacroEnergy(energyComponentsMap)

    // previously: use total energy is missing, falling back to sum of energy components
    // Note, it shouldn't matter whether we use KJ or calories for the component map,
    // as long as the amountOf() call below uses the same unit
    //val totalEnergy = nd.amountOf(ENERGY, calculationUnit, calculateMacrosEnergy())

    return energyComponentsMap.mapValues {
        if (totalEnergy > 0) it.value / totalEnergy else 0.0
    }
}

// energy from each individual macronutrient
fun BasicNutrientData<*>.makeEnergyComponentsMap(unit: Unit = KILOJOULES): Map<INutrient, Double> {
    require(unit.type == UnitType.ENERGY) { "Invalid energy unit" }
    require(unit.metricEquivalent != 0.0) { "Unit cannot have zero metric equivalent" }
    val g = GRAMS
    // energy from...
    val satFat = amountOf(SATURATED_FAT, g, 0.0) * KJ_PER_G_FAT
    val monoFat = amountOf(MONOUNSATURATED_FAT, g, 0.0) * KJ_PER_G_FAT
    val polyFat = amountOf(POLYUNSATURATED_FAT, g, 0.0) * KJ_PER_G_FAT
    val sugar = amountOf(SUGAR, g, 0.0) * KJ_PER_G_CARBOHYDRATE
    val starch = amountOf(STARCH, g, 0.0) * KJ_PER_G_CARBOHYDRATE

    val protein = amountOf(PROTEIN, g, 0.0) * KJ_PER_G_PROTEIN
    val fibre = amountOf(FIBRE, g, 0.0) * KJ_PER_G_FIBRE
    val alcohol = amountOf(ALCOHOL, g, 0.0) * KJ_PER_G_ALCOHOL

    // fat must be >= sat + mono + poly
    val fat = (amountOf(FAT, g, 0.0) * KJ_PER_G_FAT)
        .coerceAtLeast(satFat + monoFat + polyFat)
    // correct subtypes: carbs must be >= sugar + starch
    val carb = (amountOf(CARBOHYDRATE, g, 0.0) * KJ_PER_G_CARBOHYDRATE)
        .coerceAtLeast(sugar + starch)

    // preserve iteration order
    val kjMap: Map<INutrient, Double> = mapOf(
        PROTEIN to protein,
        FAT to fat,
        SATURATED_FAT to satFat,
        MONOUNSATURATED_FAT to monoFat,
        POLYUNSATURATED_FAT to polyFat,
        CARBOHYDRATE to carb,
        SUGAR to sugar,
        STARCH to starch,
        FIBRE to fibre,
        ALCOHOL to alcohol,
    )
    return if (unit == KILOJOULES) {
        kjMap
    } else {
        kjMap.mapValues { it.value / unit.metricEquivalent }
    }
}


// Returns amount of nutrient in this NutrientData divided by target amount,
// if both are present and target amount is nonzero.
// If data value is missing, returns zero.
// If target value is missing or zero, returns null.
fun BasicNutrientData<*>.getProportionOfTarget(target: BasicNutrientData<*>, n: Nutrient): Double? {
    val unit = getUnit(n, StandardNutrientUnits)
    return when (val targetValue = target.amountOf(n, unit)) {
        null, 0.0 -> null
        else -> amountOf(n, unit, 0.0) / targetValue
    }
}



// total energy predicted by macronutrient contents, rather than actual energy value
fun calculateMacroEnergy(energyComponentsMap: Map<INutrient, Double>): Double {
    return totalEnergyNutrients.sumOf { energyComponentsMap.getValue(it) }
}

private const val WATER_DENSITY = 1.0 // g/mL
private const val ALCOHOL_DENSITY = 0.789 // g/mL

// Converts this value into the given unit, if possible.
// Density is only used when converting quantity (usually in the context of a single Food)
// An exception is thrown if the conversion is not possible
fun convertNutrient(n: INutrient, value: Double, oldUnit: Unit, newUnit: Unit): Double {
    require (n !== QUANTITY) { "use convertQuantity() for quantity conversions"}

    // Can involve density to convert nutrients which support liquid units.
    // Currently, alcohol and water are the only such nutrients, and they have
    // assumed fixed densities.
    // Water density is assumed to be 1 g/mL, while alcohol is 0.789 g/mL.
    val density: Double = when(n) {
        WATER -> WATER_DENSITY
        ALCOHOL -> ALCOHOL_DENSITY
        else -> 1.0 // not considered for other units
    }

    return convertUnit(n, value, oldUnit, newUnit, density)
}

// Converts this quantity into the given unit, if possible.
// Density is required if converting between mass and volume units.
// An exception is thrown if the conversion is not possible
fun convertQuantity(amount: Double, oldUnit: Unit, newUnit: Unit, density: Double? = null): Double {
    return convertUnit(QUANTITY, amount, oldUnit, newUnit, density)
}


private fun convertUnit(nutrient: INutrient, amount: Double, oldUnit: Unit, newUnit: Unit, density: Double? = null): Double {
    if (oldUnit == newUnit) {
        return amount
    }

    require(nutrient.compatibleWith(newUnit)) { "cannot convert: $newUnit is incompatible with ${nutrient.name}" }

    val conversionRatio = oldUnit.metricEquivalent / newUnit.metricEquivalent

    if (oldUnit.type === newUnit.type) {
        return amount * conversionRatio
    }

    requireNotNull(density) { "Density required to convert quantity across mass and volume units" }
    require(density != 0.0) { "Density cannot be zero" }

    return if (!oldUnit.isVolumeMeasurement && newUnit.isVolumeMeasurement) {
        // solid units to liquid units
        amount * conversionRatio / density
    } else if (oldUnit.isVolumeMeasurement && !newUnit.isVolumeMeasurement) {
        // liquid units to solid units
        return amount * conversionRatio * density
    } else {
        error { "Units are of different type but neither one is volume" }
    }
}