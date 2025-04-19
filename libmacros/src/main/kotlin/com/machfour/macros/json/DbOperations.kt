package com.machfour.macros.json

import com.machfour.macros.core.Instant
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.Serving
import com.machfour.macros.queries.findUniqueColumnConflicts
import com.machfour.macros.queries.saveObjects
import com.machfour.macros.queries.saveObjectsReturningIds
import com.machfour.macros.schema.FoodNutrientValueTable
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.*
import com.machfour.macros.sql.rowdata.RowData
import com.machfour.macros.sql.rowdata.foodToRowData
import com.machfour.macros.sql.rowdata.servingToRowData
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
        overrideCreateTime = currentTime,
        overrideModifyTime = currentTime,
    )
}

@Throws(SqlException::class)
fun saveJsonFoods(
    db: SqlDatabase,
    jsonFoods: Collection<JsonFood>,
    overrideCreateTime: Instant? = null,
    overrideModifyTime: Instant? = null,
): Map<String, JsonFood> {
    val rawFoodsToImport = jsonFoods.associate { jsonFood ->
        val data = foodToRowData(jsonFood).apply {
            tweakTimes(overrideCreateTime, overrideModifyTime)
        }
        jsonFood.indexName to Food.factory.construct(data, ObjectSource.IMPORT)
    }

    // collect all the index names to be imported, and check if they're already in the DB.
    val conflictingFoods = findUniqueColumnConflicts(db, FoodTable,rawFoodsToImport)
    val foodsToSave = rawFoodsToImport.values.filterNot { conflictingFoods.contains(it.indexName) }

    val invalidFoods = ArrayList<JsonFood>()
    val nutrientValues = ArrayList<FoodNutrientValue>()
    val servings = ArrayList<Serving>()

    // get out the nutrition data

    val newConnection = db.openConnection(getGeneratedKeys = true)
    db.beginTransaction()
    val foodIds = saveObjectsReturningIds(db, FoodTable, foodsToSave, ObjectSource.IMPORT)
    val indexNameToId = foodsToSave.withIndex()
        .associate { (index, food) -> food.indexName to foodIds[index] }

    for (jf in jsonFoods) {
        val savedId = indexNameToId[jf.indexName] ?: continue

        val servingData = jf.servings.map { s ->
            servingToRowData(s).also {
                it.put(ServingTable.FOOD_ID, savedId)
                it.tweakTimes(overrideCreateTime, overrideModifyTime)
            }
        }

        if (servingData.size != jf.servings.size) {
            // some servings were invalid
            invalidFoods.add(jf)
            continue
        }

        val nutrientValueData = jf.getNutrientValueData().onEach { (_, data) ->
            data.put(FoodNutrientValueTable.FOOD_ID, savedId)
            data.tweakTimes(overrideCreateTime, overrideModifyTime)
        }

        if (nutrientValueData.size != jf.nutrientData.nutrients.size) {
            // some nutrients were invalid
            invalidFoods.add(jf)
            continue
        }

        for (s in servingData) {
            servings.add(Serving.factory.construct(s, ObjectSource.IMPORT))
        }

        for (nv in nutrientValueData.values) {
            nutrientValues.add(FoodNutrientValue.factory.construct(nv, ObjectSource.IMPORT))
        }
    }

    saveObjects(db, FoodNutrientValueTable, nutrientValues, ObjectSource.IMPORT)
    saveObjects(db, ServingTable, servings, ObjectSource.IMPORT)

    db.endTransaction()
    if (newConnection) {
        db.closeConnection()
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

private fun <M> RowData<M>.tweakTimes(
    overrideCreateTime: Instant? = null,
    overrideModifyTime: Instant? = null,
) {
    overrideCreateTime?.let { put(table.createTimeColumn, it) }
    overrideModifyTime?.let { put(table.modifyTimeColumn, it) }
}
