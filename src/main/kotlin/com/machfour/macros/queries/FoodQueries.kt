package com.machfour.macros.queries

import com.machfour.macros.core.Schema
import com.machfour.macros.objects.*
import com.machfour.macros.storage.DatabaseUtils.makeIdMap
import com.machfour.macros.storage.MacrosDataSource
import java.sql.SQLException

object FoodQueries {
    // Searches Index name, name, variety and brand for prefix, then index name for anywhere
    // empty list will be returned if keyword is empty string
    @Throws(SQLException::class)
    fun foodSearch(ds: MacrosDataSource, keyword: String): Set<Long> {
        val columns = listOf(
                Schema.FoodTable.INDEX_NAME
                , Schema.FoodTable.NAME
                , Schema.FoodTable.VARIETY
                , Schema.FoodTable.BRAND
        )
        // match any column prefix
        val prefixResults = Queries.prefixSearch(ds, Food.table(), columns, keyword)
        // or anywhere in index name
        val indexResults = Queries.substringSearch(ds, Food.table(), listOf(Schema.FoodTable.INDEX_NAME), keyword)
        val results: MutableSet<Long> = LinkedHashSet(prefixResults.size + indexResults.size)
        results.addAll(prefixResults)
        results.addAll(indexResults)
        return results
    }

    @Throws(SQLException::class)
    fun getAllFoodCategories(ds: MacrosDataSource): Map<String, FoodCategory> {
        val categoriesById = ds.getAllRawObjects(FoodCategory.table())
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
        return QueryHelpers.getIdsFromKeys(ds, Food.table(), Schema.FoodTable.INDEX_NAME, indexNames)
    }

    // The proper way to get all foods
    @Throws(SQLException::class)
    fun getAllFoods(ds: MacrosDataSource): List<Food> {
        val allFoods = ds.getAllRawObjects(Food.table())
        val allServings = ds.getAllRawObjects(Serving.table())
        val allNutritionData = ds.getAllRawObjects(NutritionData.table())
        val allFoodCategories = getAllFoodCategories(ds)
        val allIngredients = ds.getAllRawObjects(Ingredient.table())
        QueryHelpers.processRawIngredients(ds, allIngredients)
        QueryHelpers.processRawFoodMap(allFoods, allServings, allNutritionData, allIngredients, allFoodCategories)
        return ArrayList(allFoods.values)
    }

    @Throws(SQLException::class)
    fun getFoodsById(ds: MacrosDataSource, foodIds: Collection<Long>): Map<Long, Food> {
        val foods = QueryHelpers.getRawObjectsByIds(ds, Food.table(), foodIds)
        QueryHelpers.processRawFoodMap(ds, foods)
        return foods
    }

    @Throws(SQLException::class)
    fun getServingsById(ds: MacrosDataSource, servingIds: Collection<Long>): Map<Long, Serving> {
        return QueryHelpers.getRawObjectsByIds(ds, Serving.table(), servingIds)
    }

    /*
     * Constructs full food objects by their index name
     * Returns a map of index name to food object
     */
    @Throws(SQLException::class)
    fun getFoodsByIndexName(ds: MacrosDataSource, indexNames: Collection<String>): Map<String, Food> {
        val foods = QueryHelpers.getRawObjectsByKeys(ds, Schema.FoodTable.instance, Schema.FoodTable.INDEX_NAME, indexNames)
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