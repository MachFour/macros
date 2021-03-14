package com.machfour.macros.persistence

import com.machfour.macros.core.*
import com.machfour.macros.core.datatype.TypeCastException
import com.machfour.macros.core.schema.*
import com.machfour.macros.names.ENERGY_UNIT_NAME
import com.machfour.macros.names.QUANTITY_UNIT_NAME
import com.machfour.macros.entities.*
import com.machfour.macros.entities.inbuilt.DefaultUnits
import com.machfour.macros.entities.inbuilt.Nutrients
import com.machfour.macros.entities.inbuilt.Units
import com.machfour.macros.queries.FkCompletion.completeForeignKeys
import com.machfour.macros.queries.FoodQueries.getFoodByIndexName
import com.machfour.macros.queries.Queries
import com.machfour.macros.queries.Queries.saveObjects
import com.machfour.macros.util.javaTrim
import com.machfour.macros.util.Pair
import com.machfour.macros.validation.SchemaViolation
import org.supercsv.io.CsvMapReader
import org.supercsv.io.ICsvMapReader
import org.supercsv.prefs.CsvPreference
import java.io.IOException
import java.io.Reader
import java.sql.SQLException

object CsvImport {
    // don't edit csvRow keyset!
    @Throws(TypeCastException::class)
    fun <M> extractData(csvRow: Map<String, String?>, table: Table<M>): ColumnData<M> {
        val relevantCols = table.columnsByName.filter { csvRow.keys.contains(it.key) }
        var extractedSomething = false
        val data = ColumnData(table).apply {
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
    fun extractNutrientData(csvRow: Map<String, String?>): List<ColumnData<FoodNutrientValue>> {
        val data = ArrayList<ColumnData<FoodNutrientValue>>()

        for (nutrient in Nutrients.nutrients) {
            // we skip adding the nutrient if it's not present in the CSV
            val valueString = csvRow[nutrient.csvName]?.takeIf { it.isNotBlank() } ?: continue

            val unitString = when(nutrient) {
                Nutrients.QUANTITY -> csvRow[QUANTITY_UNIT_NAME]
                Nutrients.ENERGY -> csvRow[ENERGY_UNIT_NAME]
                else -> null // default unit
            }
            val unit = unitString?.let { Units.fromAbbreviation(it) } ?: DefaultUnits.get(nutrient)

            val nutrientValueData = ColumnData(FoodNutrientValue.table).apply {
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
    fun getMapReader(r: Reader): ICsvMapReader = CsvMapReader(r, CsvPreference.EXCEL_PREFERENCE)

    // returns true if all fields in the CSV are blank AFTER IGNORING WHITESPACE
    private fun allValuesEmpty(csvRow: Map<String, String?>) = csvRow.values.all { it.isNullOrBlank() }
    //private fun allValuesEmpty(csvRow: Map<String, String?>): Boolean {
    //    return csvRow.values.firstOrNull { s -> !s.isNullOrBlank() } == null
    //}

    // Returns map of food index name to parsed food and nutrition columnData objects
    @Throws(IOException::class, TypeCastException::class)
    private fun getFoodData(foodCsv: Reader): List<Pair<ColumnData<Food>, List<ColumnData<FoodNutrientValue>>>> {
        val data: MutableList<Pair<ColumnData<Food>, List<ColumnData<FoodNutrientValue>>>> = ArrayList()
        getMapReader(foodCsv).use { mapReader ->
            val header = mapReader.getHeader(true)
            var csvRow: Map<String, String?> = emptyMap()
            while (mapReader.read(*header)?.also { csvRow = it } != null) {
                if (allValuesEmpty(csvRow)) {
                    continue  // it's a blank row
                }
                val foodData = extractData(csvRow, FoodTable.instance)
                val ndData = extractNutrientData(csvRow)

                //assert(foodData.hasData(FoodTable.NAME)) { "Food is missing its name" }

                data.add(Pair(foodData, ndData))
            }
        }
        return data
    }

    // map from composite food index name to list of ingredients
    // XXX adding the db to get ingredient food objects looks ugly
    @Throws(IOException::class, SQLException::class, TypeCastException::class)
    private fun makeIngredients(ingredientCsv: Reader, ds: MacrosDataSource): Map<String, MutableList<Ingredient>> {
        val data: MutableMap<String, MutableList<Ingredient>> = HashMap()
        try {
            getMapReader(ingredientCsv).use { mapReader ->
                val header = mapReader.getHeader(true)
                var csvRow: Map<String, String?> = emptyMap()
                while (mapReader.read(*header)?.also { csvRow = it } != null) {
                    if (allValuesEmpty(csvRow)) {
                        continue  // it's a blank row
                    }
                    // XXX CSV contains food index names, while the DB wants food IDs - how to convert?????
                    val ingredientData = extractData(csvRow, Ingredient.table)
                    val compositeIndexName = csvRow.getValue("recipe_index_name")
                            ?: throw CsvException("No value for field: composite_index_name")
                    val ingredientIndexName = csvRow["ingredient_index_name"]
                            ?: throw CsvException("No value for field: ingredient_index_name")
                    // TODO error handling
                    val ingredientFood = getFoodByIndexName(ds, ingredientIndexName)
                            ?: throw CsvException("No ingredient exists with index name: $ingredientIndexName")
                    ingredientData.put(IngredientTable.PARENT_FOOD_ID, MacrosEntity.NO_ID)
                    ingredientData.put(IngredientTable.FOOD_ID, ingredientFood.id)
                    val i = Ingredient.factory.construct(ingredientData, ObjectSource.IMPORT)
                    //ingredientData.putExtraData(Schema.IngredientTable.COMPOSITE_FOOD_ID, compositeFoodIndexName);
                    i.setFkParentNaturalKey(IngredientTable.PARENT_FOOD_ID, FoodTable.INDEX_NAME, compositeIndexName)
                    i.initFoodAndNd(ingredientFood)

                    // add the new ingredient data to the existing list in the map, or create one if it doesn't yet exist.
                    if (data.containsKey(compositeIndexName)) {
                        data.getValue(compositeIndexName).add(i)
                    } else {
                        val recipeIngredients: MutableList<Ingredient> = ArrayList()
                        recipeIngredients.add(i)
                        data[compositeIndexName] = recipeIngredients
                    }
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
    fun buildCompositeFoodObjectTree(recipeCsv: Reader, ingredients: Map<String, MutableList<Ingredient>>): Map<String, CompositeFood> {
        // preserve insertion order
        val foodMap: MutableMap<String, CompositeFood> = LinkedHashMap()
        val ndMap: MutableMap<String, List<ColumnData<FoodNutrientValue>>> = LinkedHashMap()
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
    fun buildFoodObjectTree(foodCsv: Reader): Map<String, Food> {
        // preserve insertion order
        val foodMap: MutableMap<String, Food> = LinkedHashMap()
        for ((foodData, nutrientValueData) in getFoodData(foodCsv)) {
            val f: Food
            f = try {
                Food.factory.construct(foodData, ObjectSource.IMPORT)
            } catch (e: SchemaViolation) {
                throw CsvException("Schema violation detected in food: ${e.message} Data: $foodData")
                //continue;
            }
            nutrientValueData.forEach {
                try {
                    val nv = FoodNutrientValue.factory.construct(it, ObjectSource.IMPORT)
                    // without pairs, needed to recover nutrition data from return value
                    f.addNutrientValue(nv)
                } catch (e: SchemaViolation) {
                    throw CsvException("Schema violation detected in nutrition data: ${e.message} Data: $it")
                    //continue;
                }
            }
            if (foodMap.containsKey(f.indexName)) {
                throw CsvException("Imported foods contained duplicate index name: " + f.indexName)
            }
            foodMap[f.indexName] = f
        }
        return foodMap
    }

    @Throws(IOException::class, TypeCastException::class)
    fun buildServings(servingCsv: Reader): List<Serving> {
        val servings: MutableList<Serving> = ArrayList()
        getMapReader(servingCsv).use { mapReader ->
            val header = mapReader.getHeader(true)
            var csvRow: Map<String, String> = emptyMap()
            while (mapReader.read(*header)?.also { csvRow = it } != null) {
                val servingData = extractData(csvRow, Serving.table)
                val foodIndexName = csvRow[FoodTable.INDEX_NAME.sqlName]
                        ?: throw CsvException("Food index name was null for row: $csvRow")
                val s = Serving.factory.construct(servingData, ObjectSource.IMPORT)
                // TODO move next line to be run immediately before saving
                s.setFkParentNaturalKey(ServingTable.FOOD_ID, FoodTable.INDEX_NAME, foodIndexName)
                servings.add(s)
            }
        }
        return servings
    }

    @Throws(SQLException::class)
    private fun findExistingFoodIndexNames(ds: MacrosDataSource, indexNames: Collection<String>): Set<String> {
        val queryResult = Queries.selectSingleColumn(ds, Food.table, FoodTable.INDEX_NAME) {
            where(FoodTable.INDEX_NAME, indexNames, iterate = true)
            distinct(false)
        }
        return queryResult.map { requireNotNull(it) { "Null food index name encountered: $it" } }.toSet()
    }

    // foods maps from index name to food object. Food object must have nutrition data attached
    @Throws(SQLException::class)
    private fun saveImportedFoods(ds: MacrosDataSource, foods: Map<String, Food>) {
        // collect all of the index names to be imported, and check if they're already in the DB.
        val existingIndexNames = findExistingFoodIndexNames(ds, foods.keys)
        // remove entries corresponding to existing foods; this actually modifies the original map
        val foodsToSave : Map<String, Food> = foods.filter { entry -> !existingIndexNames.contains(entry.key) }
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
            food.nutrientData.nutrientValues.also {
                for (nv in it) {
                    // link it to the food so that the DB can create the correct foreign key entries
                    nv.setFkParentNaturalKey(FoodNutrientValueTable.FOOD_ID, FoodTable.INDEX_NAME, food)
                }
            }
        }

        if (existingIndexNames.isNotEmpty()) {
            println("The following foods will be imported; others had index names already present in the database:")
            foodsToSave.keys.forEach { println(it) }
        }
        saveObjects(ds, foodsToSave.values, ObjectSource.IMPORT)
        val completedNv = completeForeignKeys(ds, nvObjects, FoodNutrientValueTable.FOOD_ID)
        saveObjects(ds, completedNv, ObjectSource.IMPORT)
    }

    @Throws(IOException::class, SQLException::class, TypeCastException::class)
    fun importFoodData(ds: MacrosDataSource, foodCsv: Reader, allowOverwrite: Boolean) {
        val csvFoods = buildFoodObjectTree(foodCsv)
        saveImportedFoods(ds, csvFoods)
    }

    // TODO detect existing servings
    @Throws(IOException::class, SQLException::class, TypeCastException::class)
    fun importServings(ds: MacrosDataSource, servingCsv: Reader, allowOverwrite: Boolean) {
        val csvServings = buildServings(servingCsv)
        val completedServings = completeForeignKeys(ds, csvServings, ServingTable.FOOD_ID)
        saveObjects(ds, completedServings, ObjectSource.IMPORT)
    }

    @Throws(IOException::class, SQLException::class, TypeCastException::class)
    fun importRecipes(ds: MacrosDataSource, recipeCsv: Reader, ingredientCsv: Reader) {
        val ingredientsByRecipe : Map<String, MutableList<Ingredient>> = makeIngredients(ingredientCsv, ds)
        val csvRecipes : Map<String, CompositeFood> = buildCompositeFoodObjectTree(recipeCsv, ingredientsByRecipe)
        //val duplicateRecipes : Set<String> = findExistingFoodIndexNames(csvRecipes.keys, ds)
        //duplicateRecipes.removeAll(ingredientsByRecipe.keys)
        // todo remove the extra duplicate check from inside this function, do it here
        saveImportedFoods(ds, csvRecipes)

        // add all the ingredients for non-duplicated recipes to one big list, then save them all
        val allIngredients: List<Ingredient> = ingredientsByRecipe.flatMap { it.value }

        val completedIngredients = completeForeignKeys(ds, allIngredients, IngredientTable.PARENT_FOOD_ID)
        saveObjects(ds, completedIngredients, ObjectSource.IMPORT)
    }
}