package com.machfour.macros.entities.inbuilt

import com.machfour.macros.orm.ColumnData
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.orm.schema.NutrientTable
import com.machfour.macros.names.*
import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.UnitType

object Nutrients {

    // the total amount of mass (or sometimes volume) of food which contains the other nutrients
    val QUANTITY: Nutrient
    val ENERGY: Nutrient
    val PROTEIN: Nutrient
    val FAT: Nutrient
    val SATURATED_FAT: Nutrient
    val CARBOHYDRATE: Nutrient
    val SUGAR: Nutrient
    val FIBRE: Nutrient
    val SODIUM: Nutrient
    val POTASSIUM: Nutrient
    val CALCIUM: Nutrient
    val IRON: Nutrient
    val MONOUNSATURATED_FAT: Nutrient
    val POLYUNSATURATED_FAT: Nutrient
    val OMEGA_3_FAT: Nutrient
    val OMEGA_6_FAT: Nutrient
    val STARCH: Nutrient
    val SALT: Nutrient
    val WATER: Nutrient
    val CARBOHYDRATE_BY_DIFF: Nutrient
    val ALCOHOL: Nutrient
    val SUGAR_ALCOHOL: Nutrient
    val CAFFEINE: Nutrient


    private val idMap: MutableMap<Long, Nutrient> = LinkedHashMap(30, .9f)

    // initialised when registration is turned off
    private lateinit var nutrientSet: Set<Nutrient>
    private lateinit var nutrientSetWithoutQuantity: Set<Nutrient>

    private fun initNutrientSets() {
        nutrientSet = idMap.values.toSet()
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

    // index of next registered nutrient
    private var nextIndex = 0L

    private fun registerNutrient(name: String, typeFlags: Int, inbuilt: Boolean) : Nutrient {
        val id = nextIndex++
        val data = ColumnData(Nutrient.table).apply {
            put(NutrientTable.ID, id)
            put(NutrientTable.NAME, name)
            put(NutrientTable.UNIT_TYPES, typeFlags)
            put(NutrientTable.INBUILT, inbuilt)
        }

        return Nutrient.factory.construct(data, ObjectSource.INBUILT).also {
            idMap[id] = it
        }
    }

    private fun registerInbuiltNutrient(name: String, vararg types: UnitType)
        = registerNutrient(name, UnitType.asFlags(types), inbuilt = true)

    fun fromId(id: Long): Nutrient = idMap.getValue(id)

    init {
        QUANTITY = registerInbuiltNutrient(QUANTITY_NAME, UnitType.MASS, UnitType.VOLUME)
        ENERGY = registerInbuiltNutrient(ENERGY_NAME, UnitType.ENERGY)
        PROTEIN = registerInbuiltNutrient(PROTEIN_NAME, UnitType.MASS)
        FAT = registerInbuiltNutrient(FAT_NAME, UnitType.MASS)
        SATURATED_FAT = registerInbuiltNutrient(SATURATED_FAT_NAME, UnitType.MASS)
        CARBOHYDRATE = registerInbuiltNutrient(CARBOHYDRATE_NAME, UnitType.MASS)
        SUGAR = registerInbuiltNutrient(SUGAR_NAME, UnitType.MASS)
        FIBRE = registerInbuiltNutrient(FIBRE_NAME, UnitType.MASS)
        SODIUM = registerInbuiltNutrient(SODIUM_NAME, UnitType.MASS)
        POTASSIUM = registerInbuiltNutrient(POTASSIUM_NAME, UnitType.MASS)
        CALCIUM = registerInbuiltNutrient(CALCIUM_NAME, UnitType.MASS)
        IRON = registerInbuiltNutrient(IRON_NAME, UnitType.MASS)
        MONOUNSATURATED_FAT = registerInbuiltNutrient(MONOUNSATURATED_FAT_NAME, UnitType.MASS)
        POLYUNSATURATED_FAT = registerInbuiltNutrient(POLYUNSATURATED_FAT_NAME, UnitType.MASS)
        OMEGA_3_FAT = registerInbuiltNutrient(OMEGA_3_FAT_NAME, UnitType.MASS)
        OMEGA_6_FAT = registerInbuiltNutrient(OMEGA_6_FAT_NAME, UnitType.MASS)
        STARCH = registerInbuiltNutrient(STARCH_NAME, UnitType.MASS)
        SALT = registerInbuiltNutrient(SALT_NAME, UnitType.MASS)
        WATER = registerInbuiltNutrient(WATER_NAME, UnitType.MASS, UnitType.VOLUME)
        CARBOHYDRATE_BY_DIFF = registerInbuiltNutrient(CARBOHYDRATE_BY_DIFF_NAME, UnitType.MASS)
        ALCOHOL = registerInbuiltNutrient(ALCOHOL_NAME, UnitType.MASS)
        SUGAR_ALCOHOL = registerInbuiltNutrient(SUGAR_ALCOHOL_NAME, UnitType.MASS)
        CAFFEINE = registerInbuiltNutrient(CAFFEINE_NAME, UnitType.MASS)
    }

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

    fun registerNutrient(name: String, type: UnitType) : Nutrient {
        check (registrationAllowed) { "Cannot add more nutrients after accessing" }
        return registerNutrient(name, type.id, inbuilt = false)
    }

}
