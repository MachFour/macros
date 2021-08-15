package com.machfour.macros.persistence

class MacrosDataCache private constructor(private val upstream: MacrosDatabase) {

    companion object {
        private var INSTANCE: MacrosDataCache? = null
        fun initialise(upstream: MacrosDatabase) {
            INSTANCE = MacrosDataCache(upstream)
        }

        val instance: MacrosDataCache?
            get() {
                checkNotNull(INSTANCE) { "Not initialised with upstream data source" }
                return INSTANCE
            }
    }

}