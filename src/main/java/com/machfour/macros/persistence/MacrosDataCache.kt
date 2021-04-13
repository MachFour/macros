package com.machfour.macros.persistence

import com.machfour.macros.core.Column
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.schema.FoodTable
import com.machfour.macros.core.schema.MealTable
import com.machfour.macros.core.Table
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.entities.Meal
import com.machfour.macros.entities.Serving
import com.machfour.macros.queries.WriteQueries
import com.machfour.macros.queries.FoodQueries
import com.machfour.macros.queries.MealQueries.getMealsById
import java.sql.SQLException

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