package com.machfour.macros.entities

import com.machfour.macros.sql.entities.Factory
import com.machfour.macros.sql.entities.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.sql.entities.Factories
import com.machfour.macros.schema.AttrMappingTable
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData

class AttrMapping internal constructor(data: RowData<AttrMapping>, objectSource: ObjectSource)
    : MacrosEntityImpl<AttrMapping>(data, objectSource) {

    companion object {
        val factory: Factory<AttrMapping, AttrMapping>
            get() = Factories.attributeMapping

    }

    override fun getTable(): Table<*, AttrMapping> {
        return AttrMappingTable
    }

}
