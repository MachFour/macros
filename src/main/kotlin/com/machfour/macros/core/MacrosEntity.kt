package com.machfour.macros.core

/**
 * Defines common methods for each object to be persisted
 */
interface MacrosEntity<M : MacrosEntity<M>> {
    companion object {
        const val NO_ID: Long = -100

        // special ID for the 'null' serving of just grams / mL
        const val UNIT_SERVING: Long = -101
        const val NO_DATE: Long = -99
        val UNSET = Double.NaN
    }

    val id: Long
    fun hasId(): Boolean {
        return id != NO_ID
    }

    val createTime: Long
    val modifyTime: Long
    val objectSource: ObjectSource

    // Used to get data by column objects
    fun <J> getData(col: Column<M, J>): J?
    fun hasData(col: Column<M, *>): Boolean

    // Creates a mapping of column objects to their values for this instance
    fun getAllData(readOnly: Boolean): ColumnData<M>

    // equivalent to getAllData(true)
    val allData: ColumnData<M>
    val table: Table<M>
    val factory: Factory<M>

    // ... Alternative methods that can be used with unique columns
    fun <N : MacrosEntity<N>, J> setFkParentNaturalKey(fkCol: Column.Fk<M, *, N>, parentNaturalKey: Column<N, J>, parent: N)
    fun <N, J> setFkParentNaturalKey(fkCol: Column.Fk<M, *, N>, parentNaturalKey: Column<N, J>, data: J)
    fun <N> getFkParentNaturalKey(fkCol: Column.Fk<M, *, N>): ColumnData<N>
    val fkNaturalKeyMap: Map<Column.Fk<M, *, *>, *>
    fun copyFkNaturalKeyMap(from: MacrosEntity<M>)

}