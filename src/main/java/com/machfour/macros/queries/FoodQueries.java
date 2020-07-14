package com.machfour.macros.queries;

import com.machfour.macros.core.Column;
import com.machfour.macros.core.Schema;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.FoodCategory;
import com.machfour.macros.objects.Ingredient;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.Serving;
import com.machfour.macros.storage.DatabaseUtils;
import com.machfour.macros.storage.MacrosDataSource;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.machfour.macros.util.MiscUtils.toList;

public class FoodQueries {
    // Searches Index name, name, variety and brand for prefix, then index name for anywhere
    // empty list will be returned if keyword is empty string
    @NotNull
    public static Set<Long> foodSearch(MacrosDataSource ds, @NotNull String keyword) throws SQLException {
        List<Column<Food, String>> columns = Arrays.asList(
                Schema.FoodTable.INDEX_NAME
                , Schema.FoodTable.NAME
                , Schema.FoodTable.VARIETY
                , Schema.FoodTable.BRAND
        );
        // match any column prefix
        List<Long> prefixResults = Queries.prefixSearch(ds, Food.table(), columns, keyword);
        // or anywhere in index name
        List<Long> indexResults = Queries.substringSearch(ds, Food.table(), toList(Schema.FoodTable.INDEX_NAME), keyword);
        Set<Long> results = new LinkedHashSet<>(prefixResults.size() + indexResults.size());
        results.addAll(prefixResults);
        results.addAll(indexResults);

        return results;

    }

    @NotNull
    public static Map<String, FoodCategory> getAllFoodCategories(MacrosDataSource ds) throws SQLException {
        Map<Long, FoodCategory> categoriesById = ds.getAllRawObjects(FoodCategory.table());
        Map<String, FoodCategory> categoriesByString = new LinkedHashMap<>(categoriesById.size());
        for (FoodCategory c : categoriesById.values()) {
            categoriesByString.put(c.getName(), c);
        }
        return categoriesByString;
    }

    @Nullable
    public static Food getFoodByIndexName(MacrosDataSource ds, @NotNull String indexName) throws SQLException {
        Map<String, Food> resultFood = getFoodsByIndexName(ds, toList(indexName));
        return resultFood.getOrDefault(indexName, null);
    }

    @Nullable
    public static Food getFoodById(MacrosDataSource ds, @NotNull Long id) throws SQLException {
        Map<Long, Food> resultFood = getFoodsById(ds, toList(id));
        return resultFood.getOrDefault(id, null);
    }

    // creates a map of entries from SELECT index_name, id FROM Food WHERE index_name IN (indexNames)
    // items in indexNames that do not correspond to a food, will not appear in the output map
    @NotNull
    public static Map<String, Long> getFoodIdsByIndexName(MacrosDataSource ds, @NotNull Collection<String> indexNames) throws SQLException {
        return QueryHelpers.getIdsFromKeys(ds, Food.table(), Schema.FoodTable.INDEX_NAME, indexNames);
    }

    // The proper way to get all fooods
    @NotNull
    public static List<Food> getAllFoods(MacrosDataSource ds) throws SQLException {
        Map<Long, Food> allFoods = ds.getAllRawObjects(Food.table());
        Map<Long, Serving> allServings = ds.getAllRawObjects(Serving.table());
        Map<Long, NutritionData> allNutritionData = ds.getAllRawObjects(NutritionData.table());
        Map<String, FoodCategory> allFoodCategories = getAllFoodCategories(ds);
        Map<Long, Ingredient> allIngredients = ds.getAllRawObjects(Ingredient.table());
        QueryHelpers.processRawIngredients(ds, allIngredients);
        QueryHelpers.processRawFoodMap(allFoods, allServings, allNutritionData, allIngredients, allFoodCategories);
        return new ArrayList<>(allFoods.values());
    }

    @NotNull
    public static Map<Long, Food> getFoodsById(MacrosDataSource ds, @NotNull Collection<Long> foodIds) throws SQLException {
        Map<Long, Food> foods = QueryHelpers.getRawObjectsByIds(ds, Food.table(), foodIds);
        QueryHelpers.processRawFoodMap(ds, foods);
        return foods;
    }

    @NotNull
    public static Map<Long, Serving> getServingsById(MacrosDataSource ds, @NotNull Collection<Long> servingIds) throws SQLException {
        return QueryHelpers.getRawObjectsByIds(ds, Serving.table(), servingIds);
    }

    /*
     * Constructs full food objects by their index name
     * Returns a map of index name to food object
     */
    @NotNull
    public static Map<String, Food> getFoodsByIndexName(MacrosDataSource ds, @NotNull Collection<String> indexNames) throws SQLException {
        Map<String, Food> foods = QueryHelpers.getRawObjectsByKeys(ds, Schema.FoodTable.instance(), Schema.FoodTable.INDEX_NAME, indexNames);
        // TODO hmm this is kind of inefficient
        Map<Long, Food> idMap = DatabaseUtils.makeIdMap(foods.values());
        QueryHelpers.processRawFoodMap(ds, idMap);
        return foods;
    }

    /*
     * FoodTable search done by substring matching the searchString against any of the given columns
     * Use prefixOnly to only allow matches at the start of the matching string.
     */
    //List<FoodTable> getMatchingFoods(String searchString, String[] columnNames, boolean prefixOnly);
}
