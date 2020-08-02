package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.datatype.Types

import com.machfour.macros.core.Schema.NutritionDataTable.Companion.ALCOHOL
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.CALCIUM
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.CALORIES
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.CARBOHYDRATE
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.CARBOHYDRATE_BY_DIFF
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.DATA_SOURCE
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.DENSITY
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.FAT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.FIBRE
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.FOOD_ID
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.IRON
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.KILOJOULES
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.MONOUNSATURATED_FAT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.OMEGA_3_FAT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.OMEGA_6_FAT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.POLYUNSATURATED_FAT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.POTASSIUM
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.PROTEIN
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.QUANTITY
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.QUANTITY_UNIT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.SALT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.SATURATED_FAT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.SODIUM
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.STARCH
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.SUGAR
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.SUGAR_ALCOHOL
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.WATER

// immutable class storing nutrition data for a food or meal
class NutritionData private constructor(dataMap: ColumnData<NutritionData>, objectSource: ObjectSource)
    : MacrosEntityImpl<NutritionData>(dataMap, objectSource) {

    companion object {
        internal const val CAL_TO_KJ_FACTOR = 4.186
        internal const val CALS_PER_G_PROTEIN = 17 / CAL_TO_KJ_FACTOR
        internal const val CALS_PER_G_FAT = 37 / CAL_TO_KJ_FACTOR
        internal const val CALS_PER_G_CARBOHYDRATE = 17 / CAL_TO_KJ_FACTOR
        internal const val CALS_PER_G_FIBRE = 8 / CAL_TO_KJ_FACTOR

        // measured in the relevant FoodTable's QuantityUnits
        const val DEFAULT_QUANTITY = 100.0

        // Factory has to be initialised first before table is referenced.
        // This is a problem only if the factory is cached as an instance variable
        val factory = Factory<NutritionData> { dataMap, objectSource -> NutritionData(dataMap, objectSource) }

        val table: Table<NutritionData>
            get() = Schema.NutritionDataTable.instance

        val NUTRIENT_COLUMNS = listOf(
                QUANTITY,
                KILOJOULES,
                CALORIES,
                PROTEIN,
                FAT,
                SATURATED_FAT,
                CARBOHYDRATE,
                SUGAR,
                FIBRE,
                SODIUM,
                CALCIUM,
                POTASSIUM,
                IRON,
                MONOUNSATURATED_FAT,
                POLYUNSATURATED_FAT,
                OMEGA_3_FAT,
                OMEGA_6_FAT,
                CARBOHYDRATE_BY_DIFF,
                WATER,
                STARCH,
                SALT,
                ALCOHOL,
                SUGAR_ALCOHOL
        )

        // For units
        private val ENERGY_COLS = listOf(CALORIES, KILOJOULES)

        val energyProportionCols = setOf(PROTEIN, FAT, SATURATED_FAT, CARBOHYDRATE, SUGAR, FIBRE)

    }

    // keeps track of missing data for adding different instances of NutritionDataTable together
    // only NUTRIENT_COLUMNS are present in this map
    private val completeData: MutableMap<Column<NutritionData, Double>, Boolean>

    // measured in grams, per specified quantity
    val qtyUnit: QtyUnit
    var food: Food? = null
        set(f) {
            assert(this.food == null && f != null && foreignKeyMatches(this, FOOD_ID, f))
            field = f
        }

    override val factory: Factory<NutritionData>
        get() = Companion.factory

    val foodId: Long
        get() = getData(FOOD_ID)!!

    override val table: Table<NutritionData>
        get() = Schema.NutritionDataTable.instance

    val dataSource: String?
        get() = getData(DATA_SOURCE)

    // hack for USDA foods
    // subtracts fibre from carbohydrate if necessary to produce a carbohydrate amount
    // If fibre is not present, returns just carbs by diff
    // If there is not enough data to do that, return 0.
    private val carbsBestEffort: Double
        get() = if (hasCompleteData(CARBOHYDRATE)) {
            amountOf(CARBOHYDRATE)!!
        } else if (hasCompleteData(CARBOHYDRATE_BY_DIFF) && hasCompleteData(FIBRE)) {
            amountOf(CARBOHYDRATE_BY_DIFF)!! - amountOf(FIBRE)!!
        } else if (hasCompleteData(CARBOHYDRATE_BY_DIFF)) {
            amountOf(CARBOHYDRATE_BY_DIFF)!!
        } else {
            0.0
        }


    val density: Double?
        get() = getData(DENSITY)

    val quantity: Double
        get() = getData(QUANTITY)!!

    // map of protein, fat, saturated fat, carbs, sugar, fibre to proportion of total energy
    private val energyProportionsMap: Map<Column<NutritionData, Double>, Double> by lazy {
        NutritionCalculations.makeEnergyProportionsMap(this)
    }


    init {
        // food ID is allowed to be null only if this NutritionData is computed from a sum
        assert(objectSource === ObjectSource.COMPUTED || dataMap[FOOD_ID] != null)
        completeData = HashMap(NUTRIENT_COLUMNS.size)
        // have to use temporary due to type parameterisation
        for (c in NUTRIENT_COLUMNS) {
            completeData[c] = dataMap.hasData(c)
        }
        // account for energy conversion
        val hasEnergy = completeData[CALORIES]!! || completeData[KILOJOULES]!!
        completeData[CALORIES] = hasEnergy
        completeData[KILOJOULES] = hasEnergy
        qtyUnit = QtyUnits.fromAbbreviation(dataMap[QUANTITY_UNIT]!!)
    }

    // allows keeping track of missing data when different nData instances are added up
    internal constructor(
            dataMap: ColumnData<NutritionData>,
            objectSource: ObjectSource,
            completeData: Map<Column<NutritionData, Double>, Boolean>) : this(dataMap, objectSource) {
        for (c in NUTRIENT_COLUMNS) {
            if (!completeData.getValue(c)) {
                this.completeData[c] = false
            } else {
                // we shouldn't need to correct this case, since if the thing is null (and so makeHasDataMap
                // said it was not present), then we shouldn't be correcting that to say that it is present!
                assert(this.completeData.getValue(c))
            }
        }
    }

    val qtyUnitAbbr: String
        get() = getData(QUANTITY_UNIT)!!

    private fun getEnergyAs(energyCol: Column<NutritionData, Double>): Double? {
        assert(ENERGY_COLS.contains(energyCol))
        return getData(energyCol)
                ?: if (energyCol == CALORIES) {
                    getData(KILOJOULES)?.div(CAL_TO_KJ_FACTOR)
                } else { // energyCol.equals(KILOJOULES)
                    getData(CALORIES)?.times(CAL_TO_KJ_FACTOR)
                }
    }

    fun hasCompleteData(col: Column<NutritionData, Double>): Boolean {
        assert(completeData.containsKey(col))
        return completeData.getValue(col)
    }

    // copies completeData
    fun completeDataMap(): MutableMap<Column<NutritionData, Double>, Boolean> {
        return HashMap(completeData)
    }

    fun getProteinEnergyPercentage() : Double {
        return getEnergyProportion(PROTEIN, true)
    }
    fun getProteinEnergyProportion() : Double {
        return getEnergyProportion(PROTEIN, false)
    }

    fun getAllCarbsEnergyPercentage() : Double {
        return getEnergyProportion(CARBOHYDRATE, true) +
                getEnergyProportion(SUGAR, true) +
                getEnergyProportion(FIBRE, true)
    }
    // Total (carbs + sugar + fibre) energy proportion
    fun getAllCarbsEnergyProportion(asPercentage: Boolean = false) : Double {
        return getEnergyProportion(CARBOHYDRATE, asPercentage) +
            getEnergyProportion(SUGAR, asPercentage) +
            getEnergyProportion(FIBRE, asPercentage)
    }

    fun getAllFatsEnergyPercentage() : Double {
        return getEnergyProportion(FAT, true) +
                getEnergyProportion(SATURATED_FAT, true)
    }

    fun getAllFatsEnergyProportion(asPercentage: Boolean = false) : Double {
        return getEnergyProportion(FAT, asPercentage) +
                getEnergyProportion(SATURATED_FAT, asPercentage)
    }

    fun getEnergyProportion(col: Column<NutritionData, Double>, asPercentage: Boolean = false) : Double {
        if (!energyProportionCols.contains(col)) {
            return 0.0
        }

        // the map is computed the first time this function is called
        val proportion = energyProportionsMap[col] ?: 0.0
        return if (asPercentage) proportion * 100 else proportion
    }

    fun hasFood(): Boolean {
        return food != null
    }


    override fun equals(other: Any?): Boolean {
        return other is NutritionData && super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    fun amountOf(col: Column<NutritionData, Double>): Double? {
        assert(NUTRIENT_COLUMNS.contains(col))
        return if (ENERGY_COLS.contains(col)) {
            // return any energy value, converting if necessary. Return null if neither column.
            getEnergyAs(col)
            // TODO sodium/salt
            // TODO carbs by difference / carbs
        } else {
            getData(col)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun hasData(col: Column<NutritionData, *>): Boolean {
        if (col.type === Types.REAL) {
            // ColumnType of REAL ensures the cast will work
            val doubleCol = col as Column<NutritionData, Double>
            if (NUTRIENT_COLUMNS.contains(doubleCol)) {
                return amountOf(doubleCol) != null
            }
        }
        // fall back to just checking the columnData
        return super.hasData(col)
    }

    fun amountOf(col: Column<NutritionData, Double>, defaultValue: Double): Double {
        val data = amountOf(col)
        return data ?: defaultValue
    }

}



