package com.machfour.macros.nutrients

import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.Nutrient
import com.machfour.macros.names.*
import com.machfour.macros.schema.NutrientTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.units.UnitType

private fun makeInbuiltNutrient(id: Long, name: String, vararg types: UnitType) : Nutrient {
    val typeFlags = UnitType.asFlags(types)
    val data = RowData(Nutrient.table).apply {
        put(NutrientTable.ID, id)
        put(NutrientTable.NAME, name)
        put(NutrientTable.UNIT_TYPES, typeFlags)
        put(NutrientTable.INBUILT, true)
    }
    return Nutrient.factory.construct(data, ObjectSource.INBUILT)
}

// the total amount of mass (or sometimes volume) of food which contains the other nutrients
val QUANTITY             = makeInbuiltNutrient( 0, QUANTITY_NAME, UnitType.MASS, UnitType.VOLUME)
val ENERGY               = makeInbuiltNutrient( 1, ENERGY_NAME, UnitType.ENERGY)
val PROTEIN              = makeInbuiltNutrient( 2, PROTEIN_NAME, UnitType.MASS)
val FAT                  = makeInbuiltNutrient( 3, FAT_NAME, UnitType.MASS)
val SATURATED_FAT        = makeInbuiltNutrient( 4, SATURATED_FAT_NAME, UnitType.MASS)
val CARBOHYDRATE         = makeInbuiltNutrient( 5, CARBOHYDRATE_NAME, UnitType.MASS)
val SUGAR                = makeInbuiltNutrient( 6, SUGAR_NAME, UnitType.MASS)
val FIBRE                = makeInbuiltNutrient( 7, FIBRE_NAME, UnitType.MASS)
val SODIUM               = makeInbuiltNutrient( 8, SODIUM_NAME, UnitType.MASS)
val POTASSIUM            = makeInbuiltNutrient( 9, POTASSIUM_NAME, UnitType.MASS)
val CALCIUM              = makeInbuiltNutrient(10, CALCIUM_NAME, UnitType.MASS)
val IRON                 = makeInbuiltNutrient(11, IRON_NAME, UnitType.MASS)
val MONOUNSATURATED_FAT  = makeInbuiltNutrient(12, MONOUNSATURATED_FAT_NAME, UnitType.MASS)
val POLYUNSATURATED_FAT  = makeInbuiltNutrient(13, POLYUNSATURATED_FAT_NAME, UnitType.MASS)
val OMEGA_3_FAT          = makeInbuiltNutrient(14, OMEGA_3_FAT_NAME, UnitType.MASS)
val OMEGA_6_FAT          = makeInbuiltNutrient(15, OMEGA_6_FAT_NAME, UnitType.MASS)
val STARCH               = makeInbuiltNutrient(16, STARCH_NAME, UnitType.MASS)
val SALT                 = makeInbuiltNutrient(17, SALT_NAME, UnitType.MASS)
val WATER                = makeInbuiltNutrient(18, WATER_NAME, UnitType.MASS, UnitType.VOLUME)
val CARBOHYDRATE_BY_DIFF = makeInbuiltNutrient(19, CARBOHYDRATE_BY_DIFF_NAME, UnitType.MASS)
val ALCOHOL              = makeInbuiltNutrient(20, ALCOHOL_NAME, UnitType.MASS)
val SUGAR_ALCOHOL        = makeInbuiltNutrient(21, SUGAR_ALCOHOL_NAME, UnitType.MASS)
val CAFFEINE             = makeInbuiltNutrient(22, CAFFEINE_NAME, UnitType.MASS)


private val inbuiltNutrients = listOf(
    QUANTITY,
    ENERGY,
    PROTEIN,
    FAT,
    SATURATED_FAT,
    CARBOHYDRATE,
    SUGAR,
    FIBRE,
    SODIUM,
    POTASSIUM,
    CALCIUM,
    IRON,
    MONOUNSATURATED_FAT,
    POLYUNSATURATED_FAT,
    OMEGA_3_FAT,
    OMEGA_6_FAT,
    STARCH,
    SALT,
    WATER,
    CARBOHYDRATE_BY_DIFF,
    ALCOHOL,
    SUGAR_ALCOHOL,
    CAFFEINE
)


private val minValidNextId = inbuiltNutrients.size

private val idMap: MutableMap<Long, Nutrient> by lazy { inbuiltNutrients.associateBy { it.id }.toMutableMap() }

// initialised when registration is turned off
private lateinit var nutrientSet: Set<Nutrient>
private lateinit var nutrientSetWithoutQuantity: Set<Nutrient>

private fun initNutrientSets() {
    nutrientSet = inbuiltNutrients.toSet()
    nutrientSetWithoutQuantity = nutrientSet.minusElement(QUANTITY)
}

// whether new Nutrients can be registered:
// Since the nutrients are a global state, once nutrients start being used in NutrientData objects,
// we can't be adding more.
// This is set to false by any accesses of nutrientIterator, numNutrients, or fromId
private var registrationAllowed = true
    set(value) {
        // only allow setting false
        if (!value) {
            field = value
            initNutrientSets()
        }
    }

fun registerNutrient(nutrient: Nutrient) {
    check (registrationAllowed) { "Cannot add more nutrients after nutrient sets have been initialised" }

    val id = nutrient.id
    require(id >= minValidNextId) { "Nutrient IDs below $minValidNextId are used for inbuilt nutrients"}

    idMap[id]?.let {
        error("Cannot register nutrient ${nutrient.csvName} - id $id is already used by ${idMap[id]}")
    }

    idMap[id] = nutrient
}

fun nutrientWithIdOrNull(id: Long): Nutrient? = idMap[id]

fun nutrientWithId(id: Long): Nutrient = requireNotNull(nutrientWithIdOrNull(id)) { "No nutrient found with id $id" }

val nutrients: Set<Nutrient>
    get() {
        registrationAllowed = false
        return nutrientSet
    }

val nutrientsExceptQuantity: Set<Nutrient>
    get() {
        registrationAllowed = false
        return nutrientSetWithoutQuantity
    }

val numNutrients: Int
    get() {
        registrationAllowed = false
        return idMap.size
    }
