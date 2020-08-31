package com.machfour.macros.storage

import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.Schema.FoodTable
import com.machfour.macros.core.Schema.NutritionDataTable
import com.machfour.macros.core.Schema.IngredientTable
import com.machfour.macros.core.Schema.ServingTable
import com.machfour.macros.core.Table
import com.machfour.macros.core.datatype.TypeCastException
import com.machfour.macros.objects.*
import com.machfour.macros.queries.FkCompletion.completeForeignKeys
import com.machfour.macros.queries.FoodQueries.getFoodByIndexName
import com.machfour.macros.queries.Queries.saveObjects
import com.machfour.macros.queries.Queries.selectColumn
import com.machfour.macros.util.MiscUtils.javaTrim
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
        val relevantCols = csvRow.keys.filter { table.columnsByName.keys.contains(it) }
        return ColumnData(table).apply {
            relevantCols.forEach { colName ->
                val value = csvRow[colName]
                val col = table.columnsByName.getValue(colName)
                // map empty strings in CSV to null
                if (value == null) {
                    putFromRaw(col, null)
                } else {
                    putFromString(col, value.javaTrim())
                }
            }
        }
    }

    // EXCEL_PREFERENCE sets newline character to '\n', quote character to '"' and delimiter to ','
    fun getMapReader(r: Reader): ICsvMapReader = CsvMapReader(r, CsvPreference.EXCEL_PREFERENCE)

    // returns true if all fields in the CSV are blank AFTER IGNORING WHITESPACE
    private fun allValuesEmpty(csvRow: Map<String, String?>): Boolean {
        return csvRow.values.firstOrNull { s -> !s.isNullOrBlank() } == null
    }

    // Returns map of food index name to parsed food and nutrition columnData objects
    @Throws(IOException::class, TypeCastException::class)
    private fun getFoodData(foodCsv: Reader): List<Pair<ColumnData<Food>, ColumnData<NutritionData>>> {
        val data: MutableList<Pair<ColumnData<Food>, ColumnData<NutritionData>>> = ArrayList()
        getMapReader(foodCsv).use { mapReader ->
            val header = mapReader.getHeader(true)
            var csvRow: Map<String, String?>?
            while (mapReader.read(*header).also { csvRow = it } != null) {
                if (allValuesEmpty(csvRow!!)) {
                    continue  // it's a blank row
                }
                val foodData = extractData(csvRow!!, FoodTable.instance)
                val ndData = extractData(csvRow!!, NutritionDataTable.instance)
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
                var csvRow: Map<String, String?>?
                while (mapReader.read(*header).also { csvRow = it } != null) {
                    if (allValuesEmpty(csvRow!!)) {
                        continue  // it's a blank row
                    }
                    // XXX CSV contains food index names, while the DB wants food IDs - how to convert?????
                    val ingredientData = extractData(csvRow!!, IngredientTable.instance)
                    val compositeIndexName = csvRow!!.getValue("recipe_index_name")
                            ?: throw CsvException("No value for field: composite_index_name")
                    val ingredientIndexName = csvRow!!["ingredient_index_name"]
                            ?: throw CsvException("No value for field: ingredient_index_name")
                    // TODO error handling
                    val ingredientFood = getFoodByIndexName(ds, ingredientIndexName)
                            ?: throw CsvException("No ingredient exists with index name: $ingredientIndexName")
                    ingredientData.put(IngredientTable.INGREDIENT_FOOD_ID, ingredientFood.id)
                    val i = Ingredient.factory.construct(ingredientData, ObjectSource.IMPORT)
                    //ingredientData.putExtraData(Schema.IngredientTable.COMPOSITE_FOOD_ID, compositeFoodIndexName);
                    i.setFkParentNaturalKey(IngredientTable.COMPOSITE_FOOD_ID, FoodTable.INDEX_NAME, compositeIndexName)
                    i.initIngredientFood(ingredientFood)

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
        val ndMap: MutableMap<String, ColumnData<NutritionData>> = LinkedHashMap()
        // nutrition data may not be complete, so we can't create it yet. Just create the foods
        for ((foodData, ndData) in getFoodData(recipeCsv)) {
            foodData.put(FoodTable.FOOD_TYPE, FoodType.COMPOSITE.niceName)
            val f = Food.factory().construct(foodData, ObjectSource.IMPORT)
            assert(f is CompositeFood)
            if (foodMap.containsKey(f.indexName)) {
                throw CsvException("Imported recipes contained duplicate index name: " + f.indexName)
            }
            foodMap[f.indexName] = f as CompositeFood
            ndMap[f.indexName] = ndData
        }
        for ((key, value) in ingredients) {
            val recipeFood = foodMap.getValue(key)
            value.forEach {
                it.initCompositeFood(recipeFood)
                recipeFood.addIngredient(it)
            }
        }
        // now we can finally create the nutrition data
        for (cf in foodMap.values) {
            val csvNutritionData = ndMap[cf.indexName]
            if (csvNutritionData!!.hasData(NutritionDataTable.QUANTITY)) {
                // assume that there is overriding data
                val overridingData = NutritionData.factory.construct(csvNutritionData, ObjectSource.IMPORT)
                cf.setNutritionData(overridingData)
                // calling cf.getnData will now correctly give all the values
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
        for ((foodData, ndData) in getFoodData(foodCsv)) {
            val f: Food
            val nd: NutritionData
            f = try {
                Food.factory().construct(foodData, ObjectSource.IMPORT)
            } catch (e: SchemaViolation) {
                throw CsvException("Schema violation detected in food: ${e.message} Data: $foodData")
                //continue;
            }
            nd = try {
                NutritionData.factory.construct(ndData, ObjectSource.IMPORT)
            } catch (e: SchemaViolation) {
                throw CsvException("Schema violation detected in nutrition data: ${e.message} Data: $foodData")
                //continue;
            }
            f.setNutritionData(nd) // without pairs, needed to recover nutrition data from return value
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
            var csvRow: Map<String, String>?
            while (mapReader.read(*header).also { csvRow = it } != null) {
                val servingData = extractData(csvRow!!, Serving.table())
                val foodIndexName = csvRow!![FoodTable.INDEX_NAME.sqlName]
                        ?: throw CsvException("Food index name was null for row: $csvRow")
                val s = Serving.factory().construct(servingData, ObjectSource.IMPORT)
                // TODO move next line to be run immediately before saving
                s.setFkParentNaturalKey(ServingTable.FOOD_ID, FoodTable.INDEX_NAME, foodIndexName)
                servings.add(s)
            }
        }
        return servings
    }

    @Throws(SQLException::class)
    private fun findExistingFoodIndexNames(ds: MacrosDataSource, indexNames: Collection<String>): Set<String> {
        return selectColumn(ds, Food.table(), FoodTable.INDEX_NAME, FoodTable.INDEX_NAME, indexNames, false)
                .map { requireNotNull(it) { "Null food index name encountered: $it" } }
                .toSet()
    }

    // foods maps from index name to food object. Food object must have nutrition data attached by way of getnData()
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
        val ndObjects = foodsToSave.map { (_, food) ->
            food.getNutritionData().also {
                // link it to the food so that the DB can create the correct foreign key entries
                it.setFkParentNaturalKey(NutritionDataTable.FOOD_ID, FoodTable.INDEX_NAME, food)
            }
        }

        if (existingIndexNames.isNotEmpty()) {
            println("The following foods will be imported; others had index names already present in the database:")
            foodsToSave.keys.forEach { println(it) }
        }
        saveObjects(ds, foodsToSave.values, ObjectSource.IMPORT)
        val completedNd = completeForeignKeys(ds, ndObjects, NutritionDataTable.FOOD_ID)
        saveObjects(ds, completedNd, ObjectSource.IMPORT)
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

        val completedIngredients = completeForeignKeys(ds, allIngredients, IngredientTable.COMPOSITE_FOOD_ID)
        saveObjects(ds, completedIngredients, ObjectSource.IMPORT)
    }
}