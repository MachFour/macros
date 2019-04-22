package com.machfour.macros.storage;

import com.machfour.macros.objects.Food;
import com.machfour.macros.core.MacrosPersistable;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.util.DateStamp;

import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface MacrosDataSource {

    List<Long> foodSearch(String keyword) throws SQLException;

    List<Food> getAllFoods() throws SQLException;

    Food getFoodById(Long id) throws SQLException;

    Food getFoodByIndexName(String indexName) throws SQLException;

    Map<String, Food> getFoodsByIndexName(Collection<String> indexNames) throws SQLException;
    Map<Long, Food> getFoodsById(Collection<Long> foodIds) throws SQLException;

    // creates a map entries from SELECT index_name, id FROM Food WHERE index_name IN (indexNames)
    // items in indexNames that do not correspond to a food, will not appear in the output map
    Map<String, Long> getFoodIdsByIndexName(Collection<String> indexNames) throws SQLException;

    Meal getMealById(Long id) throws SQLException;

    List<Long> getMealIdsForDay(DateStamp day) throws SQLException;

    // finds whether there is a 'current meal', and returns it if so.
    // defined as the most recently modified meal created for the current date
    // if no meals exist for the current date, returns null
    @Nullable Meal getCurrentMeal() throws SQLException;

    /* These functions construct objects for all necessary entities that match the query,
     * as well as all other entities referenced by them.
     * For example, getMealsForDay constructs all of the MealTable objects for one particular day,
     * along with their FoodPortions, their Foods, and all of the Servings of those Foods.
     * It's probably worth caching the results of these!
     */
    Map<Long, Meal> getMealsById(List<Long> mealIds) throws SQLException;

    // key is the meal name, which is unique given a particular day
    Map<String, Meal> getMealsForDay(DateStamp day) throws SQLException;

    // returns number of objects saved correctly (i.e. 0 or 1)
    <M extends MacrosPersistable<M>> int saveObject(M object) throws SQLException;

    /* These functions save the objects given to them into the database, via INSERT or UPDATE.
     * The caller should ensure that objects with an id of null correspond to new entries
     * (INSERTs) into the database, while those with a non-null id correspond to UPDATES of existing
     * rows in the table.
     *
     * Any data that originated from the user should already have been validated
     */
    // Do we really need the list methods? The user will probably only edit one object at a time throws SQLException;
    // except for deleting a bunch of foodPortions from one meal, or servings from a food
    <M extends MacrosPersistable<M>> int insertObjects(Collection<M> objects, boolean withId) throws SQLException;
    <M extends MacrosPersistable<M>> int updateObjects(Collection<M> objects) throws SQLException;

    <M extends MacrosPersistable<M>> int deleteObject(M object) throws SQLException;

    <M extends MacrosPersistable<M>> int deleteObjects(List<M> objects) throws SQLException;

    /*
     * FoodTable search done by substring matching the searchString against any of the given columns
     * Use prefixOnly to only allow matches at the start of the matching string.
     */
    //List<FoodTable> getMatchingFoods(String searchString, String[] columnNames, boolean prefixOnly);


}
