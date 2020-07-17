package com.machfour.macros.objects

import com.machfour.macros.core.*

class AttrMapping private constructor(data: ColumnData<AttrMapping>, objectSource: ObjectSource)
    : MacrosEntityImpl<AttrMapping>(data, objectSource) {

    override val table: Table<AttrMapping>
        get() = Schema.AttrMappingTable.instance

    override val factory: Factory<AttrMapping>
        get() = factory()

    companion object {
        fun factory(): Factory<AttrMapping> {
            return object : Factory<AttrMapping> {
                override fun construct(dataMap: ColumnData<AttrMapping>, objectSource: ObjectSource): AttrMapping {
                    return AttrMapping(dataMap, objectSource)
                }
            }
        }
    }
}
