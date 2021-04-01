package com.machfour.macros.entities

import com.machfour.macros.core.*
import com.machfour.macros.core.schema.AttrMappingTable
import com.machfour.macros.entities.auxiliary.Factories

class AttrMapping internal constructor(data: ColumnData<AttrMapping>, objectSource: ObjectSource)
    : MacrosEntityImpl<AttrMapping>(data, objectSource) {

    companion object {
        val factory: Factory<AttrMapping>
            get() = Factories.attributeMapping

        val table: Table<AttrMapping>
            get() = AttrMappingTable.instance
    }

    override val factory: Factory<AttrMapping>
        get() = Companion.factory

    override val table: Table<AttrMapping>
        get() = Companion.table

}
