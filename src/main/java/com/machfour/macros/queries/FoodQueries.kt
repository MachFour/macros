package com.machfour.macros.queries

import com.machfour.macros.orm.schema.*
import com.machfour.macros.entities.*
import com.machfour.macros.persistence.DatabaseUtils.makeIdMap
import com.machfour.macros.persistence.MacrosDatabase
import com.machfour.macros.queries.RawEntityQueries.getAllRawObjects
import com.machfour.macros.queries.RawEntityQueries.getRawObjectsByIds
import com.machfour.macros.queries.RawEntityQueries.getRawObjectsByKeys
import com.machfour.macros.queries.RawEntityQueries.getRawObjectsForParentFk
import com.machfour.macros.util.intersectAllOrNull
import com.machfour.macros.util.unionAllOrNull
import java.sql.SQLException

object FoodQueries {

    // excluding index name
    private val foodSearchCols = listOf(
          FoodTable.NAME
        , FoodTable.VARIETY
        , FoodTable.BRAND
        , FoodTable.EXTRA_DESC
    )

    // TODO
    //  - be adaptive - if there only few results, add more results from less selective searches
    //  e.g. not just prefix searches as strings get longer
    // returns results matching either all or any of the keywords
    @Throws(SQLException::class)
    fun foodSearch(ds: MacrosDatabase, keywords: List<String>, matchAll: Boolean = true): Set<Long> {
        return keywords.map { foodSearch(ds, it) }.let {
            // map will be empty if keywords is empty
            if (matchAll) {
                it.intersectAllOrNull()
            } else {
                it.unionAllOrNull()
            }
        } ?: emptySet()
    }

    // Searches Index name, name, variety and brand for prefix, then index name for anywhere
    // empty list will be returned if keyword is empty string
    // If keyword is only 1 or 2 chars, search only by exact match and index name prefix
    // exact matches will come first, followed by prefix results, followed by matches on substrings of index names
    // TODO
    //  - provide simple control of how exhaustive the search is
    //  - most exhaustive search: substring search of all columns
    //  - be adaptive - if there only few results, add more results from less selective searches
    //  e.g. not just prefix searches as strings get longer
    @Throws(SQLException::class)
    fun foodSearch(ds: MacrosDatabase, keyword: String): Set<Long> {
        if (keyword.isEmpty()) {
            return emptySet()
        }

        val indexNameCol = listOf(FoodTable.INDEX_NAME)
        val table = Food.table

        val results = LinkedHashSet<Long>()
        with (results) {
            // add exact matches on index name
            addAll(CoreQueries.exactStringSearch(ds, table, indexNameCol, keyword).toSet())
            // add exact matches on any other column
            addAll(CoreQueries.exactStringSearch(ds, table, foodSearchCols, keyword).toSet())

            if (keyword.length <= 2) {
                // just match prefix of index name
                addAll(CoreQueries.prefixSearch(ds, table, indexNameCol, keyword))
            } else {
                // match any column prefix
                addAll(CoreQueries.prefixSearch(ds, table, foodSearchCols, keyword))
                // match anywhere in index name
                addAll(CoreQueries.substringSearch(ds, table, indexNameCol, keyword))
            }
        }
        return results

    }

    @Throws(SQLException::class)
    fun getAllFoodCategories(ds: MacrosDatabase): Map<String, FoodCategory> {
        val categoriesById = getAllRawObjects(ds, FoodCategory.table)
        // change the map to have keys as category names
        return categoriesById.mapKeys { it.value.name }
    }

    @Throws(SQLException::class)
    fun getFoodByIndexName(ds: MacrosDatabase, indexName: String): Food? {
        val resultFood = getFoodsByIndexName(ds, listOf(indexName))
        return resultFood[indexName]
    }

    @Throws(SQLException::class)
    fun getFoodById(ds: MacrosDatabase, id: Long): Food? {
        val resultFood = getFoodsById(ds, listOf(id))
        return resultFood[id]
    }

    // creates a map of entries from SELECT index_name, id FROM Food WHERE index_name IN (indexNames)
    // items in indexNames that do not correspond to a food, will not appear in the output map
    @Throws(SQLException::class)
    fun getFoodIdsByIndexName(ds: MacrosDatabase, indexNames: Collection<String>): Map<String, Long> {
        return CoreQueries.getIdsFromKeys(ds, Food.table, FoodTable.INDEX_NAME, indexNames)
    }
    
    @Throws(SQLException::class)
    fun getFoodIdByIndexName(ds: MacrosDatabase, indexName: String): Long? {
        val idMap = CoreQueries.getIdsFromKeys(ds, Food.table, FoodTable.INDEX_NAME, listOf(indexName))
        assert(idMap.size <= 1) { "More than one ID with indexName $indexName" }
        return idMap.values.firstOrNull()
    }

    // The proper way to get all foods
    fun getAllFoodsMap(db: MacrosDatabase): Map<Long, Food> {
        val allFoods = getAllRawObjects(db, Food.table)
        val allServings = getAllRawObjects(db, Serving.table)
        val allNutrientData = getAllRawObjects(db, FoodNutrientValue.table)
        val allFoodCategories = getAllFoodCategories(db)
        val allIngredients = getAllRawObjects(db, Ingredient.table)
        processRawIngredients(db, allIngredients)
        processRawFoodMap(allFoods, allServings, allNutrientData, allIngredients, allFoodCategories)
        return allFoods
    }

    @Throws(SQLException::class)
    fun getFoodsById(db: MacrosDatabase, foodIds: Collection<Long>, preserveOrder: Boolean = false): Map<Long, Food> {
        // this map is unordered due to order of database results being unpredictable,
        // we can sort it later if necessary
        val unorderedFoods = getRawObjectsByIds(db, Food.table, foodIds)
        processRawFoodMap(db, unorderedFoods)
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
    fun getServingsById(db: MacrosDatabase, servingIds: Collection<Long>): Map<Long, Serving> {
        return getRawObjectsByIds(db, Serving.table, servingIds)
    }

    /*
     * Constructs full food objects by their index name
     * Returns a map of index name to food object
     */
    @Throws(SQLException::class)
    fun getFoodsByIndexName(db: MacrosDatabase, indexNames: Collection<String>): Map<String, Food> {
        val foods = getRawObjectsByKeys(db, FoodTable.instance, FoodTable.INDEX_NAME, indexNames)
        // TODO hmm this is kind of inefficient
        val idMap = makeIdMap(foods.values)
        processRawFoodMap(db, idMap)
        return foods
    }

    @Throws(SQLException::class)
    fun getParentFoodIdsContainingFoodIds(db: MacrosDatabase, foodIds: List<Long>): List<Long> {
        return CoreQueries.selectNonNullColumn(db, Ingredient.table, IngredientTable.PARENT_FOOD_ID) {
            where(IngredientTable.FOOD_ID, foodIds)
            distinct()
        }
    }

    @Throws(SQLException::class)
    internal fun processRawIngredients(ds: MacrosDatabase, ingredientMap: Map<Long, Ingredient>) {
        val foodIds = ArrayList<Long>(ingredientMap.size)
        val servingIds = ArrayList<Long>(ingredientMap.size)
        for (i in ingredientMap.values) {
            foodIds.add(i.foodId)
            i.servingId?.let { servingIds += it }
        }
        // XXX make sure this doesn't loop infinitely if two composite foods contain each other as ingredients
        // (or potentially via a longer chain -- A contains B, B contains C, C contains A)
        val ingredientFoods = getFoodsById(ds, foodIds)
        val ingredientServings = getServingsById(ds, servingIds)
        for (i in ingredientMap.values) {
            // applyFoodsToRawIngredients(ingredients, servings
            val f = ingredientFoods.getValue(i.foodId)
            i.initFoodAndNd(f)
            // applyServingsToRawIngredients(ingredients, servings)
            i.servingId?.let { id ->
                val s = ingredientServings.getValue(id)
                i.initServing(s)
            }
        }
    }

    private fun processRawFoodMap(
        foods: Map<Long, Food>,
        servings: Map<Long, Serving>,
        nutrientData: Map<Long, FoodNutrientValue>,
        ingredients: Map<Long, Ingredient>,
        categories: Map<String, FoodCategory>
    ) {
        applyServingsToRawFoods(foods, servings)
        applyNutrientValuesToRawFoods(foods, nutrientData)
        applyIngredientsToRawFoods(foods, ingredients)
        applyFoodCategoriesToRawFoods(foods, categories)
    }

    // foodMap is a map of food IDs to the raw (i.e. unlinked) object created from the database
    @Throws(SQLException::class)
    private fun processRawFoodMap(ds: MacrosDatabase, foodMap: Map<Long, Food>) {
        if (foodMap.isNotEmpty()) {
            //Map<Long, Serving> servings = getRawServingsForFoods(idMap);
            //Map<Long, NutrientData> nData = getRawNutrientDataForFoods(idMap);
            val servings = getRawObjectsForParentFk(ds, foodMap, Serving.table, ServingTable.FOOD_ID)
            val nutrientValues = getRawObjectsForParentFk(ds, foodMap, FoodNutrientValue.table, FoodNutrientValueTable.FOOD_ID)
            val ingredients = getRawObjectsForParentFk(ds, foodMap, Ingredient.table, IngredientTable.PARENT_FOOD_ID)
            val categories = getAllFoodCategories(ds)
            processRawIngredients(ds, ingredients)
            processRawFoodMap(foodMap, servings, nutrientValues, ingredients, categories)
        }
    }

    private fun applyServingsToRawFoods(foodMap: Map<Long, Food>, servingMap: Map<Long, Serving>) {
        for (s in servingMap.values) {
            // this query should never fail, due to database constraints
            val f = foodMap.getValue(s.foodId)
            s.initFood(f)
            f.addServing(s)
        }
    }

    private fun applyNutrientValuesToRawFoods(foodMap: Map<Long, Food>, nutrientValueMap: Map<Long, FoodNutrientValue>) {
        for (nv in nutrientValueMap.values) {
            // this lookup should never fail, due to database constraints
            foodMap.getValue(nv.foodId).addNutrientValue(nv)
        }
    }

    // note not all foods in the map will be composite
    private fun applyIngredientsToRawFoods(foodMap: Map<Long, Food>, ingredientMap: Map<Long, Ingredient>) {
        for (i in ingredientMap.values) {
            val f = foodMap[i.parentFoodId]
            require(f is CompositeFood && f.foodType === FoodType.COMPOSITE)
            i.initCompositeFood(f)
            f.addIngredient(i)
        }
    }

    private fun applyFoodCategoriesToRawFoods(foodMap: Map<Long, Food>, categories: Map<String, FoodCategory>) {
        for (f in foodMap.values) {
            val categoryName = f.getData(FoodTable.CATEGORY)!!
            val c = categories.getValue(categoryName)
            f.setFoodCategory(c)
        }
    }
}