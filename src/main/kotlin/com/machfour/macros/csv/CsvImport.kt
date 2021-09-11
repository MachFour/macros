package com.machfour.macros.csv

import com.machfour.macros.core.FoodType
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.*
import com.machfour.macros.names.ENERGY_UNIT_NAME
import com.machfour.macros.names.QUANTITY_UNIT_NAME
import com.machfour.macros.nutrients.ENERGY
import com.machfour.macros.nutrients.QUANTITY
import com.machfour.macros.nutrients.nutrients
import com.machfour.macros.queries.completeForeignKeys
import com.machfour.macros.queries.findUniqueColumnConflicts
import com.machfour.macros.queries.getFoodByIndexName
import com.machfour.macros.queries.saveObjects
import com.machfour.macros.schema.FoodNutrientValueTable
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.IngredientTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.datatype.TypeCastException
import com.machfour.macros.units.LegacyNutrientUnits
import com.machfour.macros.units.unitWithAbbr
import com.machfour.macros.util.javaTrim
import com.machfour.macros.validation.SchemaViolation
import org.supercsv.io.CsvMapReader
import org.supercsv.io.ICsvMapReader
import org.supercsv.prefs.CsvPreference
import java.io.IOException
import java.io.Reader
import java.sql.SQLException

// don't edit csvRow keyset!
@Throws(TypeCastException::class)
internal fun <M> extractCsvData(csvRow: Map<String, String?>, table: Table<M>): RowData<M> {
    val relevantCols = table.columnsByName.filter { csvRow.keys.contains(it.key) }
    var extractedSomething = false
    val data = RowData(table).apply {
        for ((colName, col) in relevantCols) {
            when (val valueString = csvRow[colName]) {
                null -> putFromRaw(col, null)
                else -> putFromString(col, valueString.javaTrim())
            }
            // check if anything actually got through
            if (hasData(col)) {
                extractedSomething = true
            }
        }
    }
    assert(extractedSomething) { "No data extracted from CSV row: $csvRow. Has the file been saved with comma delimiters?" }
    return data
}

@Throws(TypeCastException::class)
private fun extractCsvNutrientData(csvRow: Map<String, String?>): List<RowData<FoodNutrientValue>> {
    val data = ArrayList<RowData<FoodNutrientValue>>()

    for (nutrient in nutrients) {
        val valueString = csvRow[nutrient.csvName]
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

        val nutrientValueData = RowData(FoodNutrientValue.table).apply {
            // TODO parse constraints
            putFromString(FoodNutrientValueTable.VALUE, valueString)
            put(FoodNutrientValueTable.UNIT_ID, unit.id)
            put(FoodNutrientValueTable.NUTRIENT_ID, nutrient.id)
        }
        assert(nutrientValueData.hasData(FoodNutrientValueTable.VALUE)) { "Value was null for line $csvRow" }

        data.add(nutrientValueData)
    }
    return data
}

// EXCEL_PREFERENCE sets newline character to '\n', quote character to '"' and delimiter to ','
internal fun getCsvMapReader(r: Reader): ICsvMapReader =
    CsvMapReader(r, CsvPreference.EXCEL_PREFERENCE)

// returns true if all fields in the CSV are blank AFTER IGNORING WHITESPACE
private fun allValuesEmpty(csvRow: Map<String, String?>) = csvRow.values.all { it.isNullOrBlank() }

// Returns map of food index name to parsed food and nutrition RowData objects
@Throws(IOException::class, TypeCastException::class)
private fun getFoodData(foodCsv: Reader): List<Pair<RowData<Food>, List<RowData<FoodNutrientValue>>>> {
    val data = ArrayList<Pair<RowData<Food>, List<RowData<FoodNutrientValue>>>>()
    getCsvMapReader(foodCsv).use { reader ->
        val header = reader.getHeader(true)
        while (true) {
            val csvRow = reader.read(*header) ?: break
            if (allValuesEmpty(csvRow)) {
                continue  // it's a blank row
            }
            val foodData = extractCsvData(csvRow, FoodTable)
            val ndData = extractCsvNutrientData(csvRow)

            data.add(Pair(foodData, ndData))
        }
    }
    return data
}

// map from composite food index name to list of ingredients
// XXX adding the db to get ingredient food objects looks ugly
@Throws(IOException::class, SQLException::class, TypeCastException::class)
private fun makeIngredients(ingredientCsv: Reader, ds: SqlDatabase): Map<String, List<Ingredient>> {
    val data = HashMap<String, MutableList<Ingredient>>()
    try {
        getCsvMapReader(ingredientCsv).use { reader ->
            val header = reader.getHeader(true)
            while (true) {
                val csvRow = reader.read(*header) ?: break
                if (allValuesEmpty(csvRow)) {
                    continue  // it's a blank row
                }
                // XXX CSV contains food index names, while the DB wants food IDs - how to convert?????
                val ingredientData = extractCsvData(csvRow, Ingredient.table)
                val compositeIndexName = csvRow["recipe_index_name"]
                    ?: throw CsvException("No value for field: recipe_index_name")
                val ingredientIndexName = csvRow["ingredient_index_name"]
                    ?: throw CsvException("No value for field: ingredient_index_name")
                // TODO error handling
                val ingredientFood = getFoodByIndexName(ds, ingredientIndexName)
                    ?: throw CsvException("No ingredient exists with index name: $ingredientIndexName")
                ingredientData.put(IngredientTable.PARENT_FOOD_ID, MacrosEntity.NO_ID)
                ingredientData.put(IngredientTable.FOOD_ID, ingredientFood.id)
                val i = Ingredient.factory.construct(ingredientData, ObjectSource.IMPORT)
                //ingredientData.putExtraData(Schema.IngredientTable.COMPOSITE_FOOD_ID, compositeFoodIndexName);
                i.setFkParentKey(
                    IngredientTable.PARENT_FOOD_ID,
                    FoodTable.INDEX_NAME,
                    compositeIndexName
                )
                i.initFoodAndNd(ingredientFood)

                // add new ingredient data to existing list in the map, or create one if it doesn't exist.
                data.getOrPut(compositeIndexName) { ArrayList() }.add(i)
            }
        }
    } catch (e: SQLException) {
        throw e // TODO throw new CSVImportException(csvData)
    }
    return data
}

// creates Composite food objects with ingredients lists (all with no IDs), but the ingredients are raw
// (don't have linked food objects of their own)
//
@Throws(IOException::class, TypeCastException::class)
fun buildCompositeFoodObjectTree(
    recipeCsv: Reader,
    ingredients: Map<String, List<Ingredient>>
): Map<String, CompositeFood> {
    // preserve insertion order
    val foodMap: MutableMap<String, CompositeFood> = LinkedHashMap()
    val ndMap: MutableMap<String, List<RowData<FoodNutrientValue>>> = LinkedHashMap()
    // nutrition data may not be complete, so we can't create it yet. Just create the foods
    for ((foodData, nutrientValues) in getFoodData(recipeCsv)) {
        foodData.put(FoodTable.FOOD_TYPE, FoodType.COMPOSITE.niceName)
        val f = Food.factory.construct(foodData, ObjectSource.IMPORT)
        assert(f is CompositeFood)
        if (foodMap.containsKey(f.indexName)) {
            throw CsvException("Imported recipes contained duplicate index name: " + f.indexName)
        }
        foodMap[f.indexName] = f as CompositeFood
        ndMap[f.indexName] = nutrientValues
    }
    for ((key, value) in ingredients) {
        val recipeFood = foodMap.getValue(key)
        value.forEach {
            it.initCompositeFood(recipeFood)
            recipeFood.addIngredient(it)
        }
    }
    // now we can finally create the nutrition data
    for ((indexName, nutrientValueData) in ndMap.entries) {
        val compositeFood = foodMap.getValue(indexName)
        for (nvData in nutrientValueData) {
            val nv = FoodNutrientValue.factory.construct(nvData, ObjectSource.IMPORT)
            compositeFood.addNutrientValue(nv)
        }
    }
    return foodMap
}

// returns a pair of maps from food index name to corresponding food objects and nutrition data objects respectively
// TODO can probably refactor this to just return one food
@Throws(IOException::class, TypeCastException::class)
fun buildFoodObjectTree(
    foodCsv: Reader,
    modifyFoodData: ((RowData<Food>) -> Unit)? = null,
    modifyNutrientValueData: ((RowData<FoodNutrientValue>) -> Unit)? = null,
): Map<String, Food> {
    // preserve insertion order
    val foodMap = LinkedHashMap<String, Food>()
    for ((foodData, nutrientValueData) in getFoodData(foodCsv)) {
        modifyFoodData?.let { modify -> modify(foodData) }

        val food = try {
            Food.factory.construct(foodData, ObjectSource.IMPORT)
        } catch (e: SchemaViolation) {
            throw CsvException("Schema violation detected in food: ${e.message} Data: $foodData")
            //continue;
        }
        nutrientValueData.forEach {
            modifyNutrientValueData?.let { modify -> modify(it) }
            try {
                val nv = FoodNutrientValue.factory.construct(it, ObjectSource.IMPORT)
                // without pairs, needed to recover nutrition data from return value
                food.addNutrientValue(nv)
            } catch (e: SchemaViolation) {
                throw CsvException("Schema violation detected in nutrition data: ${e.message} Data: $it")
                //continue;
            }
        }
        if (foodMap.containsKey(food.indexName)) {
            throw CsvException("Imported foods contained duplicate index name: " + food.indexName)
        }
        foodMap[food.indexName] = food
    }
    return foodMap
}

@Throws(IOException::class, TypeCastException::class)
fun <J> buildServings(
    servingCsv: Reader,
    foodKeyCol: Column<Food, J>
): List<Serving> {
    require(foodKeyCol.isUnique)

    val servings = ArrayList<Serving>()
    getCsvMapReader(servingCsv).use { reader ->
        val header = reader.getHeader(true)
        while (true) {
            val csvRow = reader.read(*header) ?: break
            val servingData = extractCsvData(csvRow, Serving.table)
            val rawFoodKey = csvRow[foodKeyCol.sqlName]
                ?: throw CsvException("Food $foodKeyCol was null for row: $csvRow")
            val foodKey = requireNotNull(foodKeyCol.type.fromRawString(rawFoodKey))
            val s = Serving.factory.construct(servingData, ObjectSource.IMPORT)
            // TODO move next line to be run immediately before saving
            s.setFkParentKey(ServingTable.FOOD_ID, foodKeyCol, foodKey)
            servings.add(s)
        }
    }
    return servings
}


// Returns list of foods that couldn't be saved due to unique column conflicts

// foods maps from index name to food object. Food object must have nutrition data attached
@Throws(SQLException::class)
private fun saveImportedFoods(ds: SqlDatabase, foods: Map<String, Food>): Map<String, Food> {
    // collect all of the index names to be imported, and check if they're already in the DB.
    val conflictingFoods = findUniqueColumnConflicts(ds, foods)

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

    // get out the nutrition data
    val nvObjects = foodsToSave.flatMap { (_, food) ->
        food.nutrientData.nutrientValues.onEach {
            // link it to the food so that the DB can create the correct foreign key entries
            it.setFkParentKey(FoodNutrientValueTable.FOOD_ID, FoodTable.INDEX_NAME, food)
        }
    }

    saveObjects(ds, foodsToSave.values, ObjectSource.IMPORT)
    val completedNv = completeForeignKeys(ds, nvObjects, FoodNutrientValueTable.FOOD_ID)
    saveObjects(ds, completedNv, ObjectSource.IMPORT)

    return conflictingFoods
}

@Suppress("UNUSED_EXPRESSION")
@Throws(IOException::class, SQLException::class, TypeCastException::class)
fun importFoodData(
    db: SqlDatabase,
    foodCsv: Reader,
    allowOverwrite: Boolean,
    modifyFoodData: ((RowData<Food>) -> Unit)? = null,
    modifyNutrientValueData: ((RowData<FoodNutrientValue>) -> Unit)? = null,
): Map<String, Food> {
    allowOverwrite // TODO use

    val csvFoods = buildFoodObjectTree(
        foodCsv,
        modifyFoodData = { data ->
            //assert(foodData.hasData(FoodTable.NAME)) { "Food is missing its name" }
            // set search relevance
            data[FoodTable.FOOD_TYPE]
                ?.let { FoodType.fromString(it) }
                ?.let { data.put(FoodTable.SEARCH_RELEVANCE, it.defaultSearchRelevance.value) }

            modifyFoodData?.let { it(data) }
        },
        modifyNutrientValueData
    )

    return saveImportedFoods(db, csvFoods)
}

@Suppress("UNUSED_EXPRESSION")
// TODO detect existing servings
@Throws(IOException::class, SQLException::class, TypeCastException::class)
fun <J> importServings(
    db: SqlDatabase,
    servingCsv: Reader,
    foodKeyCol: Column<Food, J>,
    allowOverwrite: Boolean,
) {
    allowOverwrite // TODO use
    val csvServings = buildServings(servingCsv, foodKeyCol)
    val completedServings = completeForeignKeys(db, csvServings, ServingTable.FOOD_ID)
    saveObjects(db, completedServings, ObjectSource.IMPORT)
}

@Throws(IOException::class, SQLException::class, TypeCastException::class)
fun importRecipes(ds: SqlDatabase, recipeCsv: Reader, ingredientCsv: Reader) {
    val ingredientsByRecipe = makeIngredients(ingredientCsv, ds)
    val csvRecipes = buildCompositeFoodObjectTree(recipeCsv, ingredientsByRecipe)
    //val duplicateRecipes : Set<String> = findExistingFoodIndexNames(csvRecipes.keys, ds)
    //duplicateRecipes.removeAll(ingredientsByRecipe.keys)
    // todo remove the extra duplicate check from inside this function, do it here
    saveImportedFoods(ds, csvRecipes)

    // add all the ingredients for non-duplicated recipes to one big list, then save them all
    val allIngredients = ingredientsByRecipe.flatMap { it.value }

    val completedIngredients =
        completeForeignKeys(ds, allIngredients, IngredientTable.PARENT_FOOD_ID)
    saveObjects(ds, completedIngredients, ObjectSource.IMPORT)
}