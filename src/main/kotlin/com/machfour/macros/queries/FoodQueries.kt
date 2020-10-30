package com.machfour.macros.queries

import com.machfour.macros.core.Schema
import com.machfour.macros.objects.*
import com.machfour.macros.storage.DatabaseUtils.makeIdMap
import com.machfour.macros.storage.MacrosDataSource
import java.sql.SQLException

object FoodQueries {

    // excluding index name
    val foodSearchColumns = listOf(
          Schema.FoodTable.NAME
        , Schema.FoodTable.VARIETY
        , Schema.FoodTable.BRAND
        , Schema.FoodTable.EXTRA_DESC
    )

    // Searches Index name, name, variety and brand for prefix, then index name for anywhere
    // empty list will be returned if keyword is empty string
    // If keyword is only 1 or 2 chars, search only by exact match and index name prefix
    // exact matches will come first, followed by prefix results, followed by matches on substrings of index names
    @Throws(SQLException::class)
    fun foodSearch(ds: MacrosDataSource, keyword: String): Set<Long> {
        if (keyword.isEmpty()) {
            return emptySet()
        }

        val indexNameCol = listOf(Schema.FoodTable.INDEX_NAME)
        val foodTable = Food.table

        val results = LinkedHashSet<Long>()
        results.addAll(
            // add exact matches on index name
            Queries.exactStringSearch(ds, foodTable, indexNameCol, keyword).toSet()
        )
        results.addAll(
            // add exact matches on any other column
            Queries.exactStringSearch(ds, foodTable, foodSearchColumns, keyword).toSet()
        )

        if (keyword.length <= 2) {
            results.addAll(
                // just match prefix of index name
                Queries.prefixSearch(ds, foodTable, indexNameCol, keyword)
            )
        } else {
            results.addAll(
                // match any column prefix
                Queries.prefixSearch(ds, foodTable, foodSearchColumns, keyword)
            )
            results.addAll(
                // match anywhere in index name
                Queries.substringSearch(ds, foodTable, indexNameCol, keyword)
            )
        }
        return results

    }

    @Throws(SQLException::class)
    fun getAllFoodCategories(ds: MacrosDataSource): Map<String, FoodCategory> {
        val categoriesById = ds.getAllRawObjects(FoodCategory.table)
        // change the map to have keys as category names
        return categoriesById.mapKeys { idMapping : Map.Entry<Long, FoodCategory> ->
            idMapping.value.name
        }
    }

    @Throws(SQLException::class)
    fun getFoodByIndexName(ds: MacrosDataSource, indexName: String): Food? {
        val resultFood = getFoodsByIndexName(ds, listOf(indexName))
        return resultFood[indexName]
    }

    @Throws(SQLException::class)
    fun getFoodById(ds: MacrosDataSource, id: Long): Food? {
        val resultFood = getFoodsById(ds, listOf(id))
        return resultFood[id]
    }

    // creates a map of entries from SELECT index_name, id FROM Food WHERE index_name IN (indexNames)
    // items in indexNames that do not correspond to a food, will not appear in the output map
    @Throws(SQLException::class)
    fun getFoodIdsByIndexName(ds: MacrosDataSource, indexNames: Collection<String>): Map<String, Long> {
        return QueryHelpers.getIdsFromKeys(ds, Food.table, Schema.FoodTable.INDEX_NAME, indexNames)
    }

    // The proper way to get all foods
    @Throws(SQLException::class)
    fun getAllFoods(ds: MacrosDataSource): List<Food> {
        val allFoods = ds.getAllRawObjects(Food.table)
        val allServings = ds.getAllRawObjects(Serving.table)
        val allNutritionData = ds.getAllRawObjects(NutritionData.table)
        val allFoodCategories = getAllFoodCategories(ds)
        val allIngredients = ds.getAllRawObjects(Ingredient.table)
        QueryHelpers.processRawIngredients(ds, allIngredients)
        QueryHelpers.processRawFoodMap(allFoods, allServings, allNutritionData, allIngredients, allFoodCategories)
        return ArrayList(allFoods.values)
    }

    @Throws(SQLException::class)
    fun getFoodsById(ds: MacrosDataSource, foodIds: Collection<Long>, preserveOrder: Boolean = false): Map<Long, Food> {
        // this map is unordered due to order of database results being unpredictable,
        // we can sort it later if necessary
        val unorderedFoods = QueryHelpers.getRawObjectsByIds(ds, Food.table, foodIds)
        QueryHelpers.processRawFoodMap(ds, unorderedFoods)
        if (!preserveOrder) {
            return unorderedFoods
        } else {
            // match order of ids in input
            val orderedFoods: MutableMap<Long, Food> = LinkedHashMap(unorderedFoods.size)
            for (id in foodIds) {
                unorderedFoods[id]?.let { orderedFoods[id] = it }
            }
            return orderedFoods
        }
    }

    @Throws(SQLException::class)
    fun getServingsById(ds: MacrosDataSource, servingIds: Collection<Long>): Map<Long, Serving> {
        return QueryHelpers.getRawObjectsByIds(ds, Serving.table, servingIds)
    }

    /*
     * Constructs full food objects by their index name
     * Returns a map of index name to food object
     */
    @Throws(SQLException::class)
    fun getFoodsByIndexName(ds: MacrosDataSource, indexNames: Collection<String>): Map<String, Food> {
        val foods = ds.getRawObjectsByKeys(Schema.FoodTable.instance, Schema.FoodTable.INDEX_NAME, indexNames)
        // TODO hmm this is kind of inefficient
        val idMap = makeIdMap(foods.values)
        QueryHelpers.processRawFoodMap(ds, idMap)
        return foods
    }

    /*
     * FoodTable search done by substring matching the searchString against any of the given columns
     * Use prefixOnly to only allow matches at the start of the matching string.
     */
    //List<FoodTable> getMatchingFoods(String searchString, String[] columnNames, boolean prefixOnly);
}