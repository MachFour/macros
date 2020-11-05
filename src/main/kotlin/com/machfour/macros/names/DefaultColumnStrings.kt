package com.machfour.macros.names

class DefaultColumnStrings private constructor(): ColumnStringsImpl(EnglishColumnNames.instance, EnglishUnitNames.instance) {
    companion object {
        val instance: DefaultColumnStrings = DefaultColumnStrings()
    }
}