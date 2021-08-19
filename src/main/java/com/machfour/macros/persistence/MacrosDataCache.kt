package com.machfour.macros.persistence

import com.machfour.macros.sql.SqlDatabase

class MacrosDataCache private constructor(private val upstream: SqlDatabase) {

    companion object {
        private var INSTANCE: MacrosDataCache? = null
        fun initialise(upstream: SqlDatabase) {
            INSTANCE = MacrosDataCache(upstream)
        }

        val instance: MacrosDataCache?
            get() {
                checkNotNull(INSTANCE) { "Not initialised with upstream data source" }
                return INSTANCE
            }
    }

}