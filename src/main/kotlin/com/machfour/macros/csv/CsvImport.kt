package com.machfour.macros.csv

import com.machfour.macros.core.FoodType
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.*
import com.machfour.macros.names.ENERGY_UNIT_NAME
import com.machfour.macros.names.QUANTITY_UNIT_NAME
import com.machfour.macros.nutrients.AllNutrients
import com.machfour.macros.nutrients.ENERGY
import com.machfour.macros.nutrients.QUANTITY
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
    return RowData(table).apply {
        var extractedSomething = false
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
        assert(extractedSomething) { "No data extracted from CSV row: $csvRow. Has the file been saved with comma delimiters?" }
    }
}

@Throws(TypeCastException::class)
private fun extractCsvNutrientData(csvRow: Map<String, String?>): List<RowData<FoodNutrientValue>> {
    val data = ArrayList<RowData<FoodNutrientValue>>()

    for (nutrient in AllNutrients) {
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
    return ArrayList<Pair<RowData<Food>, List<RowData<FoodNutrientValue>>>>().apply {
        getCsvMapReader(foodCsv).use { reader ->
            val header = reader.getHeader(true)
            while (true) {
                val csvRow = reader.read(*header) ?: break
                if (!allValuesEmpty(csvRow)) {
                    val foodData = extractCsvData(csvRow, FoodTable)
                    val nutrientData = extractCsvNutrientData(csvRow)
                    add(foodData to nutrientData)
                }
                // else it's a blank row
            }
        }
    }
}

private typealias ParsedFoodAndNutrientData = Pair<RowData<Food>, List<RowData<FoodNutrientValue>>>

// Returns map of food index name to parsed food and nutrition RowData objects
@Throws(IOException::class, TypeCastException::class, CsvException::class)
private fun <J> getFoodDataMap(
    foodCsv: Reader,
    foodKeyCol: Column<Food, J>
): Map<J, ParsedFoodAndNutrientData> {
    return HashMap<J, ParsedFoodAndNutrientData>().apply {
        getCsvMapReader(foodCsv).use { reader ->
            val header = reader.getHeader(true)
            while (true) {
                val csvRow = reader.read(*header) ?: break
                if (!allValuesEmpty(csvRow)) {
                    val foodData = extractCsvData(csvRow, FoodTable)
                    val foodKey = foodData[foodKeyCol]
                        ?: throw CsvException("null food key (col=$foodKeyCol) for data $foodData")
                    val nutrientData = extractCsvNutrientData(csvRow)
                    if (contains(foodKey)) {
                        throw CsvException("duplicate food key found: $foodKey (col=$foodKeyCol)")
                    }
                    put(foodKey, (foodData to nutrientData))
                }
                // else it's a blank row
            }
        }
    }
}

// map from composite food index name to list of ingredients
// XXX adding the db to get ingredient food objects looks ugly
@Throws(IOException::class, SQLException::class, TypeCastException::class)
private fun makeIngredients(
    db: SqlDatabase,
    ingredientCsv: Reader
): Map<String, List<Ingredient>> {
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
                val ingredientFood = getFoodByIndexName(db, ingredientIndexName)
                    ?: throw CsvException("No ingredient exists with index name: $ingredientIndexName")
                ingredientData.put(IngredientTable.PARENT_FOOD_ID, MacrosEntity.NO_ID)
                ingredientData.put(IngredientTable.FOOD_ID, ingredientFood.id)
                Ingredient.factory.construct(ingredientData, ObjectSource.IMPORT).let {
                    it.setFkParentKey(IngredientTable.PARENT_FOOD_ID, FoodTable.INDEX_NAME, compositeIndexName)
                    it.initFoodAndNd(ingredientFood)
                    // add new ingredient data to existing list in the map, or create one if it doesn't exist.
                    data.getOrPut(compositeIndexName) { ArrayList() }.add(it)
                }
            }
        }
    } catch (e: SQLException) {
        throw e // TODO throw new CSVImportException(csvData)
    }
    return data
}

// creates Composite food objects with ingredients lists (all with no IDs), but the ingredients
// are raw in the Object sense (don't have linked food objects of their own)
@Throws(IOException::class, TypeCastException::class)
private fun <J> buildCompositeFoodObjectTree(
    recipeCsv: Reader,
    foodKeyCol: Column<Food, J>,
    ingredientsMap: Map<J, List<Ingredient>>,
): Map<J, CompositeFood> {
    val builtFoods = buildFoodObjectTree(recipeCsv, foodKeyCol, modifyFoodData = {
        it.put(FoodTable.FOOD_TYPE, FoodType.COMPOSITE.niceName)
    }).mapValues { it.value as CompositeFood }

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

@Throws(IOException::class, TypeCastException::class)
internal fun <J> buildFoodObjectTree(
    foodCsv: Reader,
    foodKeyCol: Column<Food, J>,
    modifyFoodData: ((RowData<Food>) -> Unit)? = null,
    modifyNutrientValueData: ((RowData<FoodNutrientValue>) -> Unit)? = null,
): Map<J, Food> {
    val parsedDataMap = getFoodDataMap(foodCsv, foodKeyCol)
    return parsedDataMap.mapValues { (_, foodAndNutrientData) ->
        val (foodData, nutrientValueData) = foodAndNutrientData
        modifyFoodData?.let { modify -> modify(foodData) }

        try {
            Food.factory.construct(foodData, ObjectSource.IMPORT).also { food ->
                nutrientValueData.forEach {
                    modifyNutrientValueData?.let { modify -> modify(it) }
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
private fun <J> saveImportedFoods(
    db: SqlDatabase,
    foods: Map<J, Food>,
    foodKeyCol: Column<Food, J>,
): Map<J, Food> {
    // collect all of the index names to be imported, and check if they're already in the DB.
    val conflictingFoods = findUniqueColumnConflicts(db, foods)

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
            it.setFkParentKey(FoodNutrientValueTable.FOOD_ID, foodKeyCol, food)
        }
    }

    saveObjects(db, foodsToSave.values, ObjectSource.IMPORT)
    val completedNv = completeForeignKeys(db, nvObjects, FoodNutrientValueTable.FOOD_ID)
    saveObjects(db, completedNv, ObjectSource.IMPORT)

    return conflictingFoods
}

@Suppress("UNUSED_EXPRESSION")
@Throws(IOException::class, SQLException::class, TypeCastException::class)
fun <J> importFoodData(
    db: SqlDatabase,
    foodCsv: Reader,
    foodKeyCol: Column<Food, J>,
    allowOverwrite: Boolean,
    modifyFoodData: ((RowData<Food>) -> Unit)? = null,
    modifyNutrientValueData: ((RowData<FoodNutrientValue>) -> Unit)? = null,
): Map<J, Food> {
    allowOverwrite // TODO use

    val csvFoods = buildFoodObjectTree(
        foodCsv,
        foodKeyCol = foodKeyCol,
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

    return saveImportedFoods(db, csvFoods, foodKeyCol)
}

@Suppress("UNUSED_EXPRESSION")
// TODO detect existing servings
@Throws(IOException::class, SQLException::class, TypeCastException::class)
fun <J> importServings(
    db: SqlDatabase,
    servingCsv: Reader,
    foodKeyCol: Column<Food, J>,
    ignoreKeys: Set<J>,
    allowOverwrite: Boolean,
) {
    allowOverwrite // TODO use
    val csvServings = buildServings(servingCsv, foodKeyCol)
    val nonExcludedServings = csvServings.filterNot {
        ignoreKeys.contains(it.getFkParentKey(ServingTable.FOOD_ID)[foodKeyCol])
    }
    val completedServings = completeForeignKeys(db, nonExcludedServings, ServingTable.FOOD_ID)
    saveObjects(db, completedServings, ObjectSource.IMPORT)
}

@Throws(IOException::class, SQLException::class, TypeCastException::class)
fun importRecipes(
    db: SqlDatabase,
    recipeCsv: Reader,
    ingredientCsv: Reader
) {
    val ingredientsByRecipe = makeIngredients(db, ingredientCsv)
    val csvRecipes = buildCompositeFoodObjectTree(recipeCsv, FoodTable.INDEX_NAME, ingredientsByRecipe)
    
    saveImportedFoods(db, csvRecipes, FoodTable.INDEX_NAME)

    // add all the ingredients for non-duplicated recipes to one big list, then save them all
    val allIngredients = ingredientsByRecipe.flatMap { it.value }

    val completedIngredients = completeForeignKeys(db, allIngredients, IngredientTable.PARENT_FOOD_ID)
    saveObjects(db, completedIngredients, ObjectSource.IMPORT)
}