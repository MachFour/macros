package com.machfour.macros.csv

import com.machfour.ksv.CsvConfig
import com.machfour.ksv.CsvParseException
import com.machfour.ksv.CsvParser
import com.machfour.macros.core.EntityId
import com.machfour.macros.core.FoodType
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.*
import com.machfour.macros.names.ENERGY_UNIT_NAME
import com.machfour.macros.names.QUANTITY_UNIT_NAME
import com.machfour.macros.nutrients.AllNutrients
import com.machfour.macros.nutrients.ENERGY
import com.machfour.macros.nutrients.QUANTITY
import com.machfour.macros.queries.*
import com.machfour.macros.schema.FoodNutrientValueTable
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.IngredientTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.*
import com.machfour.macros.sql.datatype.TypeCastException
import com.machfour.macros.sql.rowdata.RowData
import com.machfour.macros.units.LegacyNutrientUnits
import com.machfour.macros.units.unitWithAbbr
import com.machfour.macros.validation.SchemaViolation

// don't edit csvRow keyset!
@Throws(TypeCastException::class)
internal fun <M> extractCsvData(csvRow: Map<String, String?>, table: Table<*, M>): RowData<M> {
    val relevantCols = table.columnsByName.filter { csvRow.keys.contains(it.key) }
    return RowData(table).apply {
        var extractedSomething = false
        for ((colName, col) in relevantCols) {
            when (val valueString = csvRow[colName]) {
                null -> putFromRaw(col, null)
                else -> putFromString(col, valueString.trim())
            }
            // check if anything actually got through
            if (hasValue(col)) {
                extractedSomething = true
            }
        }
        check(extractedSomething) { "No data extracted from CSV row: $csvRow. Has the file been saved with comma delimiters?" }
    }
}

@Throws(TypeCastException::class)
private fun extractCsvNutrientData(csvRow: Map<String, String?>): List<RowData<FoodNutrientValue>> {
    val data = ArrayList<RowData<FoodNutrientValue>>()

    for (nutrient in AllNutrients) {
        val valueString = csvRow[nutrient.name]
        // we skip adding the nutrient if it's not present in the CSV
        if (valueString.isNullOrBlank()) {
            continue
        }

        val unitString = when (nutrient) {
            QUANTITY -> csvRow[QUANTITY_UNIT_NAME]
            ENERGY -> csvRow[ENERGY_UNIT_NAME]
            else -> null // default unit
        }
        val unit = unitString?.let { unitWithAbbr(it) } ?: LegacyNutrientUnits[nutrient]

        val nutrientValueData = RowData(FoodNutrientValueTable).apply {
            // TODO parse constraints
            putFromString(FoodNutrientValueTable.VALUE, valueString)
            put(FoodNutrientValueTable.UNIT_ID, unit.id)
            put(FoodNutrientValueTable.NUTRIENT_ID, nutrient.id)
        }
        check(nutrientValueData.hasValue(FoodNutrientValueTable.VALUE)) { "Value was null for line $csvRow" }

        data.add(nutrientValueData)
    }
    return data
}

internal fun getCsvParser() = CsvParser(CsvConfig.DEFAULT)

// returns true if all fields in the CSV are blank AFTER IGNORING WHITESPACE
private fun allValuesEmpty(csvRow: Map<String, String?>) = csvRow.values.all { it.isNullOrBlank() }

private typealias ParsedFoodAndNutrientData = Pair<RowData<Food>, List<RowData<FoodNutrientValue>>>

// Returns map of food index name to parsed food and nutrition RowData objects
@Throws(CsvParseException::class, TypeCastException::class, CsvException::class)
private fun <J: Any> getFoodDataMap(foodCsv: String, foodKeyCol: Column<Food, J>): Map<J, ParsedFoodAndNutrientData> {
    val csvRows = getCsvParser().parse(foodCsv)

    return buildMap {
        val header = csvRows[0]
        for (i in 1 until csvRows.size) {
            val column = csvRows[i]
            val csvRowMap = header.indices.associate { j -> header[j] to column[j] }
            if (!allValuesEmpty(csvRowMap)) {
                val foodData = extractCsvData(csvRowMap, FoodTable)
                val foodKey = foodData[foodKeyCol]
                    ?: throw CsvException("null food key (col=$foodKeyCol) for data $foodData")
                val nutrientData = extractCsvNutrientData(csvRowMap)
                if (contains(foodKey)) {
                    throw CsvException("duplicate food key found: $foodKey (col=$foodKeyCol)")
                }
                put(foodKey, (foodData to nutrientData))
            }
            // else it's a blank row
        }
    }
}

// map from composite food index name to list of ingredients
// XXX adding the db to get ingredient food objects looks ugly
@Throws(CsvParseException::class, SqlException::class, TypeCastException::class)
private fun makeIngredients(
    db: SqlDatabase,
    ingredientCsv: String
): Map<String, List<Ingredient>> {
    val csvRows = getCsvParser().parse(ingredientCsv)
    return buildMap<String, ArrayList<Ingredient>> {
        try {
            for (i in 1 until csvRows.size) {
                val header = csvRows[0]
                val column = csvRows[i]
                val csvRow = header.indices.associate { j -> header[j] to column[j] }
                if (allValuesEmpty(csvRow)) {
                    continue  // it's a blank row
                }
                // XXX CSV contains food index names, while the DB wants food IDs - how to convert?????
                val ingredientData = extractCsvData(csvRow, IngredientTable)
                val compositeIndexName = csvRow["recipe_index_name"]
                    ?: throw CsvException("No value for field: recipe_index_name")
                val ingredientIndexName = csvRow["ingredient_index_name"]
                    ?: throw CsvException("No value for field: ingredient_index_name")
                // TODO error handling
                val ingredientFood = getFoodByIndexName(db, ingredientIndexName)
                    ?: throw CsvException("No ingredient exists with index name: $ingredientIndexName")
                ingredientData.put(IngredientTable.PARENT_FOOD_ID, MacrosEntity.NO_ID)
                ingredientData.put(IngredientTable.FOOD_ID, ingredientFood.id)
                Ingredient.factory.construct(ingredientData, ObjectSource.IMPORT).let {
                    it.initFoodAndNd(ingredientFood)
                    // add new ingredient data to existing list in the map, or create one if it doesn't exist.
                    getOrPut(compositeIndexName) { ArrayList() }.add(it)
                }
            }
        } catch (e: SqlException) {
            throw CsvException(e.message ?: "")
        }
    }
}

// creates Composite food objects with ingredients lists (all with no IDs), but the ingredients
// are raw in the Object sense (don't have linked food objects of their own)
@Throws(TypeCastException::class)
private fun <J: Any> buildCompositeFoodObjectTree(
    recipeCsv: String,
    foodKeyCol: Column<Food, J>,
    ingredientsMap: Map<J, List<Ingredient>>,
): Map<J, CompositeFood> {
    val builtFoods = buildFoodObjectTree(
        foodCsv = recipeCsv,
        foodKeyCol = foodKeyCol,
        modifyFoodData = { it.put(FoodTable.FOOD_TYPE, FoodType.COMPOSITE.niceName) }
    ).mapValues { it.value as CompositeFood }

    for ((parentFoodIndexName, ingredients) in ingredientsMap) {
        val parentFood = builtFoods[parentFoodIndexName]
            ?: throw CsvException("No parent food found with index name $parentFoodIndexName")
        for (i in ingredients) {
            i.initCompositeFood(parentFood)
            parentFood.addIngredient(i)
        }
    }
    return builtFoods
}

@Throws(TypeCastException::class)
fun <J: Any> buildFoodObjectTree(
    foodCsv: String,
    foodKeyCol: Column<Food, J>,
    modifyFoodData: ((RowData<Food>) -> Unit)? = null,
    modifyNutrientValueData: ((RowData<FoodNutrientValue>) -> Unit)? = null,
): Map<J, Food> {
    val parsedDataMap = getFoodDataMap(foodCsv, foodKeyCol)
    return parsedDataMap.mapValues { (_, foodAndNutrientData) ->
        val (foodData, nutrientValueData) = foodAndNutrientData
        modifyFoodData?.invoke(foodData)

        try {
            Food.factory.construct(foodData, ObjectSource.IMPORT).also { food ->
                nutrientValueData.forEach {
                    modifyNutrientValueData?.invoke(it)
                    val nv = FoodNutrientValue.factory.construct(it, ObjectSource.IMPORT)
                    food.addNutrientValue(nv)
                }
            }
        } catch (e: SchemaViolation) {
            throw CsvException("Schema violation detected in food or nutrient data for $foodData")
        }
        // returns food
    }
}

@Throws(TypeCastException::class)
fun <J: Any> buildServingsWithFoodKeys(
    servingCsv: String,
    foodKeyCol: Column<Food, J>
): List<Pair<Serving, J>> {
    require(foodKeyCol.isUnique)

    val csvRows = getCsvParser().parse(servingCsv)
    val header = csvRows[0]
    return buildList {
        for (i in 1 until csvRows.size) {
            val columns = csvRows[i]
            val csvRowMap = header.indices.associate { j -> header[j] to columns[j] }
            val servingData = extractCsvData(csvRowMap, ServingTable)
            val rawFoodKey = csvRowMap[foodKeyCol.sqlName]
                ?: throw CsvException("Food $foodKeyCol was null for row: $csvRowMap")
            val s = Serving.factory.construct(servingData, ObjectSource.IMPORT)
            val foodKey = requireNotNull(foodKeyCol.type.fromRawString(rawFoodKey))
            add(s to foodKey)
        }
    }
}


// Returns list of foods that couldn't be saved due to unique column conflicts

// foods maps from index name to food object. Food object must have nutrition data attached
@Throws(SqlException::class)
fun <J: Any> saveImportedFoods(db: SqlDatabase, foods: Map<J, Food>): Pair<Map<J, EntityId>, Map<J, Food>> {
    // collect all the index names to be imported, and check if they're already in the DB.
    val conflictingFoods = findUniqueColumnConflicts(db, FoodTable, foods)
    val foodsToSave = foods.filterNot { conflictingFoods.contains(it.key) }
    /*
    if (allowOverwrite) {
        Map<String, Food> overwriteFoods = new HashMap<>();
    }
    TODO since we only have the index name, need to write another update function to use the secondary key.
    But also, doing a whole bunch of individual writes to the DB (one for each update is comparatively slow
    Maybe it's easier to forget about importing with overwrite. But deleting and re-importing *currently* is an issue
    because FoodPortions are stored using the food ID... Maybe we should switch to using index name.
     */

    val newConnectionOpened = db.openConnection(getGeneratedKeys = true)
    db.beginTransaction()

    val foodIds = saveObjectsReturningIds(db, Food.factory, foodsToSave.values, ObjectSource.IMPORT)
    // TODO ensure that .keys and .values order is the same for the map (it should be)
    val keyToId = foodsToSave.keys.withIndex().associate { (index, key) -> key to foodIds[index] }

    val completedNutrientValues = buildList {
        for ((key, food) in foodsToSave) {
            val id = keyToId.getValue(key)
            for ((_, nv) in food.nutrientData.nutrientValues()) {
                // link it to the food so that the DB can create the correct foreign key entries
                val completedData = nv.toRowData().apply {
                    put(FoodNutrientValueTable.FOOD_ID, id)
                }
                add(FoodNutrientValue.factory.construct(completedData, ObjectSource.IMPORT))
            }
        }
    }
    saveObjects(db, FoodNutrientValue.factory, completedNutrientValues, ObjectSource.IMPORT)

    db.endTransaction()

    if (newConnectionOpened) {
        db.closeConnection()
    }

    return keyToId to conflictingFoods
}

@Throws(SqlException::class, TypeCastException::class)
fun <J : Any> readFoodData(
    csv: String,
    keyCol: Column<Food, J>,
    modifyFoodData: ((RowData<Food>) -> Unit)? = null,
    modifyNutrientValueData: ((RowData<FoodNutrientValue>) -> Unit)? = null,
): Map<J, Food> {
    return buildFoodObjectTree(
        foodCsv = csv,
        foodKeyCol = keyCol,
        modifyFoodData = modifyFoodData,
        modifyNutrientValueData = modifyNutrientValueData
    )
}

// Returns map of existing serving IDs / servings that were matched with ones in the CSV that were skipped,
// or empty list if skipExisting was false
@Throws(SqlException::class, TypeCastException::class)
fun <J: Any> importServings(
    db: SqlDatabase,
    servingCsv: String,
    foodKeyCol: Column<Food, J>,
    foodKeyToId: Map<J, EntityId>,
    skipExisting: Boolean,
    ignoreKeys: Set<J> = emptySet(),
): Map<EntityId, Serving> {
    val csvServings = buildServingsWithFoodKeys(servingCsv, foodKeyCol)
    val nonExcludedServings = csvServings.filterNot {
        ignoreKeys.contains(it.second)
    }
    val completedServings = nonExcludedServings.map { (serving, foodKey) ->
        val foodId = requireNotNull(foodKeyToId[foodKey]) { "no food ID for key $foodKey" }
        val completedData = serving.toRowData().apply {
            put(ServingTable.FOOD_ID, foodId)
        }
        Serving.factory.construct(completedData, ObjectSource.IMPORT)
    }
    val servingsToSave: List<Serving>
    val matchedDuplicateServings: Map<EntityId, Serving>
    if (skipExisting) {
        // non-duplicated servings
        val newServingsByFoodId = completedServings.multiAssociateBy { it.foodId }
        // Maps food ID and name to list of servings with that name and food ID.
        // Generally we have one or two servings per food, so it's a bit overkill, but oh well.
        val existingServingsByNameByFoodId =
            getRawObjectsForParentFk(db, newServingsByFoodId.keys, ServingTable, ServingTable.FOOD_ID)
                .values
                .multiAssociateBy { it.foodId }
                .mapValues { it.value.multiAssociateBy { s -> s.name } }

        servingsToSave = ArrayList()
        matchedDuplicateServings = HashMap()
        // If a completed serving has same name quantity as an existing serving with the same food ID, ignore it
        for ((foodId, newServingsWithId) in newServingsByFoodId) {
            val existingServingsByName = existingServingsByNameByFoodId.getOrDefault(foodId, emptyMap())

            for (s in newServingsWithId) {
                // see if name and quantity match
                val existingServingMatch = existingServingsByName[s.name]?.firstOrNull { it.amount == s.amount }
                if (existingServingMatch == null) {
                    servingsToSave.add(s)
                } else {
                    matchedDuplicateServings[existingServingMatch.id] = existingServingMatch
                }

            }
        }
    } else {
        servingsToSave = completedServings
        matchedDuplicateServings = emptyMap()
    }
    saveObjects(db, Serving.factory, servingsToSave, ObjectSource.IMPORT)
    return matchedDuplicateServings

}

@Throws(SqlException::class, TypeCastException::class)
fun importRecipes(
    db: SqlDatabase,
    recipeCsv: String,
    ingredientCsv: String
) {
    val ingredientsByRecipe = makeIngredients(db, ingredientCsv)
    val csvRecipes = buildCompositeFoodObjectTree(recipeCsv, FoodTable.INDEX_NAME, ingredientsByRecipe)

    val (indexNameToId, _) = saveImportedFoods(db, csvRecipes)

    val completedIngredients = ingredientsByRecipe.mapValues { (recipeIndexName, ingredients) ->
        val id = indexNameToId.getValue(recipeIndexName)
        ingredients.map {
            val completedData = it.toRowData().apply { put(IngredientTable.PARENT_FOOD_ID, id) }
            Ingredient.factory.construct(completedData, ObjectSource.IMPORT)
        }
    }.flatMap { it.value }

    saveObjects(db, Ingredient.factory, completedIngredients, ObjectSource.IMPORT)
}

// Groups a collection of items into a map of lists using the given key function,
// such that items in each list have a common key
internal fun <E, K> Collection<E>.multiAssociateBy(key: (E) -> K): Map<K, List<E>> {
    return buildMap {
        for (e in this@multiAssociateBy) {
            (getOrPut(key(e)) { ArrayList() } as MutableList).add(e)
        }
    }
}