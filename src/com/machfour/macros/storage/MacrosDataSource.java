package com.machfour.macros.storage;

import com.machfour.macros.core.Food;
import com.machfour.macros.core.MacrosPersistable;
import com.machfour.macros.core.Meal;
import com.machfour.macros.util.DateStamp;

import java.sql.SQLException;
import java.util.List;

public interface MacrosDataSource {

    List<Long> foodSearch(String keyword) throws SQLException;

    List<Food> getAllFoods() throws SQLException;

    Food getFoodById(Long id) throws SQLException;

    Food getFoodByIndexName(String indexName) throws SQLException;

    List<Food> getFoodsById(List<Long> foodIds) throws SQLException;

    Meal getMealById(Long id) throws SQLException;

    List<Long> getMealIdsForDay(DateStamp day) throws SQLException;

    /* These functions construct objects for all necessary entities that match the query,
     * as well as all other entities referenced by them.
     * For example, getMealsForDay constructs all of the MealTable objects for one particular day,
     * along with their FoodPortions, their Foods, and all of the Servings of those Foods.
     * It's probably worth caching the results of these!
     */
    List<Meal> getMealsById(List<Long> mealIds) throws SQLException;

    List<Meal> getMealsForDay(DateStamp day) throws SQLException;

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
    <M extends MacrosPersistable<M>> int saveObjects(List<M> objects) throws SQLException;

    <M extends MacrosPersistable<M>> int deleteObject(M object) throws SQLException;

    <M extends MacrosPersistable<M>> int deleteObjects(List<M> objects) throws SQLException;

    /*
     * FoodTable search done by substring matching the searchString against any of the given columns
     * Use prefixOnly to only allow matches at the start of the matching string.
     */
    //List<FoodTable> getMatchingFoods(String searchString, String[] columnNames, boolean prefixOnly);


}
