package com.machfour.macros.units

import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.Unit
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.orm.schema.UnitTable
import com.machfour.macros.sql.RowData

private fun makeInbuiltUnit(id: Int, name: String, abbr: String, metricEquivalent: Double, unitType: UnitType): Unit {
    return RowData(Unit.table).run {
        put(UnitTable.ID, id.toLong())
        put(UnitTable.NAME, name)
        put(UnitTable.ABBREVIATION, abbr)
        put(UnitTable.METRIC_EQUIVALENT, metricEquivalent)
        put(UnitTable.TYPE_ID, unitType.id)
        put(UnitTable.INBUILT, true)
        Unit.factory.construct(this, ObjectSource.INBUILT)
    }
}

// These definitions need to be outside the Unit class itself,
// to avoid static initialisation problems between the Unit and UnitTable classes

val GRAMS = makeInbuiltUnit(
    id = 0,
    name = "grams",
    abbr = "g",
    metricEquivalent = 1.0,
    unitType = UnitType.MASS
)
val MILLIGRAMS = makeInbuiltUnit(
    id = 1,
    name = "milligrams",
    abbr = "mg",
    metricEquivalent = 0.001,
    unitType = UnitType.MASS
)
val MILLILITRES = makeInbuiltUnit(
    id = 2,
    name = "milligrams",
    abbr = "ml",
    metricEquivalent = 1.0,
    unitType = UnitType.VOLUME
)
val LITRES = makeInbuiltUnit(
    id = 3,
    name = "litres",
    abbr = "L",
    metricEquivalent = 1000.0,
    unitType = UnitType.VOLUME
)
val KILOJOULES = makeInbuiltUnit(
    id = 4,
    name = "kilojoules",
    abbr = "kJ",
    metricEquivalent = 1.0,
    unitType = UnitType.ENERGY
)
val CALORIES = makeInbuiltUnit(
    id = 5,
    name = "calories",
    abbr = "kcal",
    metricEquivalent = 4.186,
    unitType = UnitType.ENERGY
)
// International avoirdupois ounce
val OUNCES = makeInbuiltUnit(
    id = 6,
    name = "ounces",
    abbr = "oz",
    metricEquivalent = 28.349523125,
    unitType = UnitType.MASS
)
// US fluid ounce
val FLUID_OUNCES = makeInbuiltUnit(
    id = 7,
    name = "fluid ounces",
    abbr = "fl oz",
    metricEquivalent = 30.0,
    unitType = UnitType.VOLUME
)

val inbuiltUnits = listOf(
    GRAMS,
    MILLIGRAMS,
    MILLILITRES,
    LITRES,
    KILOJOULES,
    CALORIES,
    OUNCES,
    FLUID_OUNCES,
)

private val abbrMap: MutableMap<String, Unit> by lazy { inbuiltUnits.associateBy { it.abbr }.toMutableMap() }

private val idMap: MutableMap<Long, Unit> by lazy { inbuiltUnits.associateBy { it.id }.toMutableMap() }

// this is lowercase so that CSV units can be lower case
private val String.toMapKey: String
    get() = this.lowercase()

fun registerUnit(unit: Unit) {
    val id = unit.id
    val abbrKey = unit.abbr.toMapKey

    require (!idMap.containsKey(id)) {
        "Cannot register unit ${unit.name} - id $id is already used by ${idMap[id]}"
    }
    require (!abbrMap.containsKey(abbrKey)) {
        "Cannot register unit ${unit.name} - abbr $abbrKey is already used by ${abbrMap[abbrKey]}"
    }

    idMap[id] = unit
    abbrMap[abbrKey] = unit
}

fun unitWithAbbrOrNull(abbr: String) = abbrMap[abbr.toMapKey]

@Suppress("private")
fun unitWithIdOrNull(id: Long) = idMap[id]

// case insensitive
fun unitWithAbbr(abbr: String): Unit {
    return requireNotNull(unitWithAbbrOrNull(abbr)) { "No unit found with abbreviation $abbr" }
}

fun unitWithId(id: Long): Unit {
    return requireNotNull(unitWithIdOrNull(id)) { "No unit found with id $id" }
}

fun unitsCompatibleWith(n: Nutrient): List<Unit> {
    return idMap.values.filter { n.compatibleWithUnit(it) }
}