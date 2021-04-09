package com.machfour.macros.queries

import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.schema.FoodPortionTable
import com.machfour.macros.core.schema.FoodTable
import com.machfour.macros.core.schema.IngredientTable
import com.machfour.macros.entities.*
import com.machfour.macros.persistence.DatabaseUtils.makeIdMap
import com.machfour.macros.persistence.MacrosDataSource
import java.sql.SQLException

object FoodQueries {

    // excluding index name
    val foodSearchColumns = listOf(
          FoodTable.NAME
        , FoodTable.VARIETY
        , FoodTable.BRAND
        , FoodTable.EXTRA_DESC
    )

    // returns results matching either all or any of the keywords
    fun foodSearch(ds: MacrosDataSource, keywords: List<String>, matchAll: Boolean = true): Set<Long> {
        return if (keywords.isEmpty()) {
            emptySet()
        } else {
            val resultsByKeyword = keywords.map { foodSearch(ds, it) }
            val combineOp: (Set<Long>, Set<Long>) -> Set<Long> = if (matchAll) {
                { s, t -> s.intersect(t) }
            } else {
                { s, t -> s.union(t) }
            }
            resultsByKeyword.reduce(combineOp)
        }
    }

    // Searches Index name, name, variety and brand for prefix, then index name for anywhere
    // empty list will be returned if keyword is empty string
    // If keyword is only 1 or 2 chars, search only by exact match and index name prefix
    // exact matches will come first, followed by prefix results, followed by matches on substrings of index names
    // TODO check for recent FoodQuantities entered, and use the frecencies to sort the list
    //  order: exact match, most frecent, other results
    //  also be adaptive - if there only few results, add more results from less selective searches
    //  e.g. not just prefix searches as strings get longer
    @Throws(SQLException::class)
    fun foodSearch(ds: MacrosDataSource, keyword: String): Set<Long> {
        if (keyword.isEmpty()) {
            return emptySet()
        }

        val indexNameCol = listOf(FoodTable.INDEX_NAME)
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
        val categoriesById = Queries.getAllRawObjects(ds, FoodCategory.table)
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
        return QueryHelpers.getIdsFromKeys(ds, Food.table, FoodTable.INDEX_NAME, indexNames)
    }
    
    @Throws(SQLException::class)
    fun getFoodIdByIndexName(ds: MacrosDataSource, indexName: String): Long? {
        val idMap = QueryHelpers.getIdsFromKeys(ds, Food.table, FoodTable.INDEX_NAME, listOf(indexName))
        assert(idMap.size <= 1) { "More than one ID with indexName $indexName" }
        return idMap.values.firstOrNull()
    }

    // The proper way to get all foods
    fun getAllFoodsMap(ds: MacrosDataSource): Map<Long, Food> {
        val allFoods = Queries.getAllRawObjects(ds, Food.table)
        val allServings = Queries.getAllRawObjects(ds, Serving.table)
        val allNutrientData = Queries.getAllRawObjects(ds, FoodNutrientValue.table)
        val allFoodCategories = getAllFoodCategories(ds)
        val allIngredients = Queries.getAllRawObjects(ds, Ingredient.table)
        QueryHelpers.processRawIngredients(ds, allIngredients)
        QueryHelpers.processRawFoodMap(allFoods, allServings, allNutrientData, allIngredients, allFoodCategories)
        return allFoods
    }

    @Throws(SQLException::class)
    fun forgetFood(ds: MacrosDataSource, f: Food) {
        require(f.objectSource === ObjectSource.DATABASE) { "Food ${f.indexName} is not in DB" }
        // delete nutrition data, foodQuantities, servings, then food

        // servings and nutrient values are deleted on cascade, so we only have to worry about foodquantities
        ds.deleteByColumn(FoodPortion.table, FoodPortionTable.FOOD_ID, listOf(f.id))
        ds.deleteByColumn(Ingredient.table, IngredientTable.FOOD_ID, listOf(f.id))
        Queries.deleteObject(ds, f)
    }


    @Throws(SQLException::class)
    fun getFoodsById(ds: MacrosDataSource, foodIds: Collection<Long>, preserveOrder: Boolean = false): Map<Long, Food> {
        // this map is unordered due to order of database results being unpredictable,
        // we can sort it later if necessary
        val unorderedFoods = QueryHelpers.getRawObjectsByIds(ds, Food.table, foodIds)
        QueryHelpers.processRawFoodMap(ds, unorderedFoods)
        return if (!preserveOrder) {
            unorderedFoods
        } else {
            // match order of ids in input
            val orderedFoods = LinkedHashMap<Long, Food>(unorderedFoods.size)
            for (id in foodIds) {
                unorderedFoods[id]?.let { orderedFoods[id] = it }
            }
            orderedFoods
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
        val foods = Queries.getRawObjectsByKeys(ds, FoodTable.instance, FoodTable.INDEX_NAME, indexNames)
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

    fun deleteAllCompositeFoods(ds: MacrosDataSource) : Int {
        return ds.deleteByColumn(Food.table, FoodTable.FOOD_TYPE, listOf(FoodType.COMPOSITE.niceName))
    }

    @Throws(SQLException::class)
    fun getParentFoodIdsContainingFoodIds(ds: MacrosDataSource, foodIds: List<Long>): List<Long> {
        return Queries.selectNonNullColumn(ds, Ingredient.table, IngredientTable.PARENT_FOOD_ID) {
            where(IngredientTable.FOOD_ID, foodIds)
            distinct()
        }
    }
}