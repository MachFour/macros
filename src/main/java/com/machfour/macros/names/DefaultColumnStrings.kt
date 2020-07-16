package com.machfour.macros.names

class DefaultColumnStrings : ColumnStringsImpl(
        EnglishColumnNames.instance,
        EnglishUnitNames.instance,
        DefaultColumnUnits.instance) {

    companion object {
        @JvmStatic
        val instance: DefaultColumnStrings = DefaultColumnStrings()
    }
}