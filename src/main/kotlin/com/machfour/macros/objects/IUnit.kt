package com.machfour.macros.objects

/*
 * Unit of measuring something, be that quantity, or a nutrient.
 */
interface IUnit {
    val name: String
    val abbr: String
    val unitType: UnitType
    val metricEquivalent: Double
}
