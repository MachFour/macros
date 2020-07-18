package com.machfour.macros.names

class DefaultColumnStrings : ColumnStringsImpl(
        EnglishColumnNames.instance,
        EnglishUnitNames.instance,
        DefaultColumnUnits.instance) {

    companion object {
        val instance: DefaultColumnStrings = DefaultColumnStrings()
    }
}