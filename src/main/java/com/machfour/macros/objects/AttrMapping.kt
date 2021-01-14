package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.schema.AttrMappingTable
import com.machfour.macros.core.schema.SchemaHelpers

class AttrMapping private constructor(data: ColumnData<AttrMapping>, objectSource: ObjectSource)
    : MacrosEntityImpl<AttrMapping>(data, objectSource) {

    companion object {
        val factory: Factory<AttrMapping> = Factory { dataMap, objectSource -> AttrMapping(dataMap, objectSource) }
        val table: Table<AttrMapping>
            get() = AttrMappingTable.instance
    }

    override val factory: Factory<AttrMapping>
        get() = Companion.factory

    override val table: Table<AttrMapping>
        get() = Companion.table

}
