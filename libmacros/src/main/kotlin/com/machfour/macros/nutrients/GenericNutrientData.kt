package com.machfour.macros.nutrients

import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.NutrientValue
import com.machfour.macros.entities.Unit
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.KILOJOULES
import com.machfour.macros.units.NutrientUnits
import com.machfour.macros.units.UnitType

// class storing a set of nutrient values for any purpose.
// It could be for a food or meal, or for a nutrition goal

open class GenericNutrientData<M: NutrientValue<M>>(
    val dataCompleteIfNotNull: Boolean = true
) {
    companion object {
        private const val KJ_PER_G_PROTEIN = 17.0
        private const val KJ_PER_G_FAT = 37.0
        private const val KJ_PER_G_CARBOHYDRATE = 17.0
        private const val KJ_PER_G_FIBRE = 8.27
        private const val KJ_PER_G_ALCOHOL = 29.0

        private const val CAL_TO_KJ_FACTOR = 4.186

        private val Nutrient.index : Int
            get() = id.toInt()


        // lazy because otherwise having it here messes up static initialisation
        val energyProportionNutrients by lazy {
            setOf(
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
        }

        // lazy because otherwise having it here messes up static initialisation
        val totalEnergyNutrients by lazy {
            listOf(PROTEIN, FAT, CARBOHYDRATE, FIBRE)
        }
    }

    protected val data: ArrayList<M?> = ArrayList(NumNutrients)
    protected val isDataComplete: Array<Boolean> = Array(NumNutrients) { false }

    // initialise the data (which can't be done inline easily)
    init {
        repeat(NumNutrients) {
            data.add(null)
        }
    }

    // TODO should this be passed in explicitly to show dependence?
    var isImmutable: Boolean = false
        set(value) {
            // cannot make mutable after being set
            if (value) {
                field = value
            }
        }

    val nutrientValues: List<M>
        get() = data.filterNotNull()

    val nutrientValuesExcludingQuantity: List<M>
        get() = nutrientValues.filter { it.nutrientId != QUANTITY.id }

    // map of protein, fat, saturated fat, carbs, sugar, fibre to proportion of total energy
    private val energyProportionsMap: Map<Nutrient, Double> by lazy {
        makeEnergyProportionsMap()
    }
    // map of protein, fat, saturated fat, carbs, sugar, fibre to amount of energy in calories
    private val energyComponentsMapKj: Map<Nutrient, Double> by lazy {
        makeEnergyComponentsMapInKj()
    }


    override fun equals(other: Any?): Boolean {
        return other is GenericNutrientData<*>
                && data == other.data
                && isDataComplete.contentEquals(other.isDataComplete)
    }

    override fun hashCode(): Int = data.hashCode()

    override fun toString(): String {
        val str = StringBuilder("GenericNutrientData [")
        for (n in AllNutrients) {
            str.append("$n : ${get(n)}, ")
        }
        str.append("]")
        return str.toString()
    }

    // creates a mutable copy
    open fun copy() : GenericNutrientData<M> {
        return GenericNutrientData<M>(dataCompleteIfNotNull).also { copy ->
            for (i in data.indices) {
                copy.data[i] = data[i]
                copy.isDataComplete[i] = isDataComplete[i]
            }
        }
    }

    fun clear() {
        for (i in data.indices) {
            data[i] = null
            isDataComplete[i] = false
        }
    }

    private fun assertMutable() {
        assert(!isImmutable) { "NutrientData has been made immutable" }
    }
    
    operator fun get(n: Nutrient): M? = data[n.index]

    fun amountOf(n: Nutrient, unit: Unit? = null): Double? {
        return if (unit != null) {
            require(n.compatibleWith(unit)) { "Cannot convert nutrient $n to $unit" }
            this[n]?.convertValueTo(unit)
        } else {
            this[n]?.value
        }
    }

    fun amountOf(n: Nutrient, unit: Unit? = null, defaultValue: Double): Double {
        return amountOf(n, unit) ?: defaultValue
    }

    fun hasNutrient(n: Nutrient) : Boolean {
        return this[n] != null
    }

    operator fun set(n: Nutrient, value: M?) {
        assertMutable()
        data[n.index] = value
        if (dataCompleteIfNotNull) {
            isDataComplete[n.index] = value != null
        }
    }

    fun hasCompleteData(n: Nutrient) = isDataComplete[n.index]

    internal fun markCompleteData(n: Nutrient, complete: Boolean) {
        isDataComplete[n.index] = complete
    }


    fun getUnit(n: Nutrient, defaultUnits: NutrientUnits) : Unit {
        return this[n]?.unit ?: defaultUnits[n]
    }

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

    fun getEnergyProportion(n: Nutrient) : Double {
        if (!energyProportionNutrients.contains(n)) {
            return 0.0
        }
        // the map is computed the first time this function is called
        return energyProportionsMap[n] ?: 0.0
    }

    // calculations

    fun getEnergyAs(unit: Unit) : Double? {
        require(unit.type === UnitType.ENERGY) { "Unit $unit is not an energy unit "}
        return this[ENERGY]?.convertValueTo(unit)
    }


    // total energy predicted by macronutrient contents, rather than actual energy value
    private fun calculateMacroEnergy(unit: Unit = KILOJOULES): Double {
        require(unit.type == UnitType.ENERGY) { "Invalid energy unit" }
        // this is also ensured by database
        assert(unit.metricEquivalent != 0.0) { "Unit cannot have zero metric equivalent" }

        val totalEnergy =
            totalEnergyNutrients.fold(0.0) { s, n -> s + energyComponentsMapKj.getValue(n) }
        // have to divide by kJ/calories ratio if desired unit is calories
        return totalEnergy / unit.metricEquivalent
    }

    // energy from each individual macronutrient in kilojoules
    private fun makeEnergyComponentsMapInKj(): Map<Nutrient, Double> {
        // preserve iteration order
        val componentMap = LinkedHashMap<Nutrient, Double>()

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

        componentMap[PROTEIN] = protein
        componentMap[FAT] = fat
        componentMap[SATURATED_FAT] = satFat
        componentMap[MONOUNSATURATED_FAT] = monoFat
        componentMap[POLYUNSATURATED_FAT] = polyFat
        componentMap[CARBOHYDRATE] = carb
        componentMap[SUGAR] = sugar
        componentMap[STARCH] = starch
        componentMap[FIBRE] = fibre
        componentMap[ALCOHOL] = alcohol

        return componentMap
    }


    private fun makeEnergyProportionsMap() : Map<Nutrient, Double> {
        // shouldn't matter whether we use KJ or calories here, as long as the amountOf() call below
        // uses the same unit
        val componentMap = energyComponentsMapKj // evaluated lazily

        // XXX DECISION: ignore the actual energy value of the nutritionData, just use the sum
        val totalEnergy = calculateMacroEnergy()

        // previously: use total energy is missing, falling back to sum of energy components
        //val totalEnergy = nd.amountOf(ENERGY, unit, calculateMacrosEnergy())

        return if (totalEnergy > 0) {
            componentMap.mapValues { it.value/totalEnergy }
        } else {
            componentMap.mapValues { 0.0 }
        }
    }
}



