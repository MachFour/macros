package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.objects.inbuilt.DefaultUnits
import com.machfour.macros.objects.inbuilt.Nutrients
import com.machfour.macros.objects.inbuilt.Units

// immutable class storing nutrition data for a food or meal
class NutritionData(val nutrientData: NutrientData = NutrientData(dataCompleteIfNotNull = true)) {

    companion object {

        // measured in the relevant FoodTable's QuantityUnits
        const val DEFAULT_QUANTITY = 100.0

        val energyProportionNutrients = setOf(
                Nutrients.PROTEIN,
                Nutrients.FAT,
                Nutrients.SATURATED_FAT,
                Nutrients.CARBOHYDRATE,
                Nutrients.SUGAR,
                Nutrients.FIBRE
        )

    }

    // hack for USDA foods
    // subtracts fibre from carbohydrate if necessary to produce a carbohydrate amount
    // If fibre is not present, returns just carbs by diff
    // If there is not enough data to do that, return 0.
    private val carbsBestEffort: Double
        get() = if (hasCompleteData(Nutrients.CARBOHYDRATE)) {
            amountOf(Nutrients.CARBOHYDRATE)!!
        } else if (hasCompleteData(Nutrients.CARBOHYDRATE_BY_DIFF) && hasCompleteData(Nutrients.FIBRE)) {
            amountOf(Nutrients.CARBOHYDRATE_BY_DIFF)!! - amountOf(Nutrients.FIBRE)!!
        } else if (hasCompleteData(Nutrients.CARBOHYDRATE_BY_DIFF)) {
            amountOf(Nutrients.CARBOHYDRATE_BY_DIFF)!!
        } else {
            0.0
        }


    // map of protein, fat, saturated fat, carbs, sugar, fibre to proportion of total energy
    private val energyProportionsMap: Map<Nutrient, Double> by lazy {
        NutritionCalculations.makeEnergyProportionsMap(this)
    }
    // map of protein, fat, saturated fat, carbs, sugar, fibre to amount of energy
    private val energyComponentsMap: Map<Nutrient, Double> by lazy {
        NutritionCalculations.makeEnergyComponentsMap(this, Units.CALORIES)
    }

    val quantity: Double
        get() = nutrientData.quantityObj.value

    val qtyUnit: Unit
        get() = nutrientData.quantityObj.unit

    val qtyUnitAbbr: String
        get() = nutrientData.quantityObj.unit.abbr


    fun hasCompleteData(n: Nutrient): Boolean {
        return nutrientData.hasCompleteData(n)
    }

    val proteinEnergyComponent: Double
        get() = getEnergyComponent(Nutrients.PROTEIN)

    val fatsEnergyComponent : Double
        get() = getEnergyComponent(Nutrients.FAT) + getEnergyComponent(Nutrients.SATURATED_FAT)

    val carbsEnergyComponent: Double
        get() = getEnergyComponent(Nutrients.CARBOHYDRATE) + getEnergyComponent(Nutrients.SUGAR)

    val fibreEnergyComponent: Double
        get() = getEnergyComponent(Nutrients.FIBRE)

    val proteinEnergyProportion : Double
        get() = getEnergyProportion(Nutrients.PROTEIN)

    val carbsEnergyProportion : Double
        get() = getEnergyProportion(Nutrients.CARBOHYDRATE) + getEnergyProportion(Nutrients.SUGAR)

    val fatsEnergyProportion : Double
        get() = getEnergyProportion(Nutrients.FAT) + getEnergyProportion(Nutrients.SATURATED_FAT)

    val fibreEnergyProportion: Double
        get() = getEnergyProportion(Nutrients.FIBRE)

    fun getEnergyProportion(n: Nutrient) : Double {
        if (!energyProportionNutrients.contains(n)) {
            return 0.0
        }
        // the map is computed the first time this function is called
        return energyProportionsMap[n] ?: 0.0
    }

    fun getEnergyComponent(n: Nutrient, unit: Unit = Units.CALORIES) : Double {
        if (!energyProportionNutrients.contains(n)) {
            return 0.0
        }

        require (unit.type == UnitType.ENERGY) { "Invalid energy unit" }

        // the map is computed the first time this function is called
        return (energyComponentsMap[n] ?: 0.0) * unit.metricEquivalent
    }

    fun getUnitOrDefault(n: Nutrient) : Unit {
        return nutrientData[n]?.unit ?: DefaultUnits.get(n)
    }

    override fun equals(other: Any?): Boolean {
        return other is NutritionData && super.equals(other)
    }



    fun withDefaultUnits() : NutritionData {
        return NutritionData(NutritionCalculations.convertToDefaultUnits(nutrientData))
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    fun amountOf(n: Nutrient, unit: Unit? = null): Double? {
        val nutrientValue = nutrientData[n] ?: return null

        return if (unit == null) {
            nutrientValue.value
        } else {
            require(n.isConvertibleTo(unit)) { "Cannot convert nutrient $n to $unit" }
            nutrientValue.convertValue(unit)
        }
    }

    fun hasNutrient(n: Nutrient) : Boolean {
        return nutrientData[n] != null
    }

    fun amountOf(n: Nutrient, defaultValue: Double, unit: Unit? = null): Double {
        val data = amountOf(n, unit)
        return data ?: defaultValue
    }

}



