package com.machfour.macros.json

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.Instant
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.Serving
import com.machfour.macros.queries.completeForeignKeys
import com.machfour.macros.queries.findUniqueColumnConflicts
import com.machfour.macros.queries.saveObjects
import com.machfour.macros.schema.FoodNutrientValueTable
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import kotlinx.datetime.Clock

// Note: for import, JSON ID, create time and modify time fields are ignored.
// Index name is used to associate foods and servings before IDs are allocated.
@Throws(SqlException::class)
fun importJsonFoods(
    db: SqlDatabase,
    jsonFoods: Collection<JsonFood>,
): Map<String, JsonFood> {
    val currentTime = Clock.System.now().epochSeconds
    return saveJsonFoods(
        db = db,
        jsonFoods = jsonFoods,
        overrideFoodId = MacrosEntity.NO_ID,
        overrideCreateTime = currentTime,
        overrideModifyTime = currentTime,
    )
}

@Throws(SqlException::class)
fun saveJsonFoods(
    db: SqlDatabase,
    jsonFoods: Collection<JsonFood>,
    overrideFoodId: EntityId? = null,
    overrideCreateTime: Instant? = null,
    overrideModifyTime: Instant? = null,
): Map<String, JsonFood> {
    val foods = jsonFoods.associate { jsonFood ->
        val data = jsonFood.toRowData().apply {
            overrideFoodId?.let { put(FoodTable.ID, it)}
            tweakTimes(overrideCreateTime, overrideModifyTime)
        }
        jsonFood.indexName to Food.factory.construct(data, ObjectSource.IMPORT)
    }

    // collect all the index names to be imported, and check if they're already in the DB.
    val conflictingFoods = findUniqueColumnConflicts(db, foods)
    val foodsToSave = foods.filterNot { conflictingFoods.contains(it.key) }

    val invalidFoods = ArrayList<JsonFood>()
    val nutrientValues = ArrayList<FoodNutrientValue>()
    val servings = ArrayList<Serving>()

    // get out the nutrition data

    for (jf in jsonFoods) {
        val food = foodsToSave[jf.indexName] ?: continue

        val servingData = jf.getServingData().onEach { data ->
            overrideFoodId?.let { data.put(ServingTable.FOOD_ID, it) }
            data.tweakTimes(overrideCreateTime, overrideModifyTime)
        }
        if (servingData.size != jf.servings.size) {
            // some nutrients were invalid
            invalidFoods.add(jf)
            continue
        }
        val nutrientValueData = jf.getNutrientValueData().onEach { (_, data) ->
            overrideFoodId?.let { data.put(FoodNutrientValueTable.FOOD_ID, it) }
            data.tweakTimes(overrideCreateTime, overrideModifyTime)
        }
        if (nutrientValueData.size != jf.nutrients.size) {
            // some nutrients were invalid
            invalidFoods.add(jf)
            continue
        }

        for (s in servingData) {
            Serving.factory.construct(s, ObjectSource.IMPORT).also {
                // link it to the food so that the DB can create the correct foreign key entries
                it.setFkParentKey(ServingTable.FOOD_ID, FoodTable.INDEX_NAME, food)
                servings.add(it)
            }
        }

        for (nv in nutrientValueData.values) {
            FoodNutrientValue.factory.construct(nv, ObjectSource.IMPORT).also {
                // link it to the food so that the DB can create the correct foreign key entries
                it.setFkParentKey(FoodNutrientValueTable.FOOD_ID, FoodTable.INDEX_NAME, food)
                nutrientValues.add(it)
            }
        }
    }

    saveObjects(db, foodsToSave.values, ObjectSource.IMPORT)
    completeForeignKeys(db, nutrientValues, FoodNutrientValueTable.FOOD_ID).also {
        saveObjects(db, it, ObjectSource.IMPORT)
    }
    completeForeignKeys(db, servings, ServingTable.FOOD_ID).also {
        saveObjects(db, it, ObjectSource.IMPORT)
    }

    if (conflictingFoods.isEmpty()) {
        return emptyMap()
    }

    return buildMap {
        for (jf in jsonFoods) {
            if (jf.indexName in conflictingFoods) {
                put(jf.indexName, jf)
            }
        }
    }
}

private fun <M: MacrosEntity<M>> RowData<M>.tweakTimes(
    overrideCreateTime: Instant? = null,
    overrideModifyTime: Instant? = null,
) {
    overrideCreateTime?.let { put(table.createTimeColumn, it) }
    overrideModifyTime?.let { put(table.modifyTimeColumn, it) }
}
