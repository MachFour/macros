package com.machfour.macros.entities

import com.machfour.macros.core.Factory
import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.schema.AttrMappingTable
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData

class AttrMapping internal constructor(data: RowData<AttrMapping>, objectSource: ObjectSource)
    : MacrosEntityImpl<AttrMapping>(data, objectSource) {

    companion object {
        val factory: Factory<AttrMapping>
            get() = Factories.attributeMapping

    }

    override val factory: Factory<AttrMapping>
        get() = Companion.factory

    override val table: Table<AttrMapping>
        get() = AttrMappingTable

}
