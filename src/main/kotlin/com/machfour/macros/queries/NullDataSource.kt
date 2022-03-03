package com.machfour.macros.queries

import com.machfour.macros.sql.NullDatabase

open class NullDataSource protected constructor(): StaticDataSource(NullDatabase.Instance) {
    companion object {
        val Instance = NullDataSource()
    }
}


