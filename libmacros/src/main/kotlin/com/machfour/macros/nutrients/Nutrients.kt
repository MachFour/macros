package com.machfour.macros.nutrients

import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.Nutrient
import com.machfour.macros.names.*
import com.machfour.macros.schema.NutrientTable
import com.machfour.macros.sql.rowdata.RowData
import com.machfour.macros.units.UnitType

private fun makeInbuiltNutrient(id: Long, name: String, vararg types: UnitType): Nutrient {
    val typeFlags = UnitType.asFlags(types)
    val data = RowData(NutrientTable).apply {
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
val ALCOHOL              = makeInbuiltNutrient(20, ALCOHOL_NAME, UnitType.MASS, UnitType.VOLUME)
val SUGAR_ALCOHOL        = makeInbuiltNutrient(21, SUGAR_ALCOHOL_NAME, UnitType.MASS)
val CAFFEINE             = makeInbuiltNutrient(22, CAFFEINE_NAME, UnitType.MASS)
val ERYTHRITOL           = makeInbuiltNutrient(23, ERYTHRITOL_NAME, UnitType.MASS)
val GLYCEROL             = makeInbuiltNutrient(24, GLYCEROL_NAME, UnitType.MASS)
val ISOMALT              = makeInbuiltNutrient(25, ISOMALT_NAME, UnitType.MASS)
val LACTITOL             = makeInbuiltNutrient(26, LACTITOL_NAME, UnitType.MASS)
val MALTITOL             = makeInbuiltNutrient(27, MALTITOL_NAME, UnitType.MASS)
val MANNITOL             = makeInbuiltNutrient(28, MANNITOL_NAME, UnitType.MASS)
val SORBITOL             = makeInbuiltNutrient(29, SORBITOL_NAME, UnitType.MASS)
val XYLITOL              = makeInbuiltNutrient(30, XYLITOL_NAME, UnitType.MASS)
val POLYDEXTROSE         = makeInbuiltNutrient(31, POLYDEXTROSE_NAME, UnitType.MASS)

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
    CAFFEINE,
    ERYTHRITOL,
    GLYCEROL,
    ISOMALT,
    LACTITOL,
    MALTITOL,
    MANNITOL,
    SORBITOL,
    XYLITOL,
    POLYDEXTROSE,
)


private val idMap = inbuiltNutrients.associateBy { it.id }
private val nameMap = inbuiltNutrients.associateBy { it.name }

private val nutrientSet = inbuiltNutrients.toSet()
private val nutrientSetWithoutQuantity = nutrientSet.minusElement(QUANTITY)

fun nutrientWithIdOrNull(id: Long): Nutrient? = idMap[id]
fun nutrientWithId(id: Long): Nutrient = requireNotNull(nutrientWithIdOrNull(id)) { "No nutrient found with id $id" }

fun nutrientWithNameOrNull(name: String): Nutrient? = nameMap[name]
fun nutrientWithName(name: String): Nutrient = requireNotNull(nutrientWithNameOrNull(name)) { "No nutrient found with name $name" }

val AllNutrients: Set<Nutrient>
    get() = nutrientSet

val AllNutrientsExceptQuantity: Set<Nutrient>
    get() = nutrientSetWithoutQuantity

val NumNutrients: Int
    get() = inbuiltNutrients.size