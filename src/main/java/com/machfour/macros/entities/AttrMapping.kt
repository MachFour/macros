package com.machfour.macros.entities

import com.machfour.macros.core.Factory
import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.schema.AttrMappingTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table

class AttrMapping internal constructor(data: RowData<AttrMapping>, objectSource: ObjectSource)
    : MacrosEntityImpl<AttrMapping>(data, objectSource) {

    companion object {
        val factory: Factory<AttrMapping>
            get() = Factories.attributeMapping

        val table: Table<AttrMapping>
            get() = AttrMappingTable
    }

    override val factory: Factory<AttrMapping>
        get() = Companion.factory

    override val table: Table<AttrMapping>
        get() = Companion.table

}
