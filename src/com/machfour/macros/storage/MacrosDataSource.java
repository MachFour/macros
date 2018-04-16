package com.machfour.macros.storage;

import com.machfour.macros.core.Food;
import com.machfour.macros.core.MacrosPersistable;
import com.machfour.macros.core.Meal;
import com.machfour.macros.util.DateStamp;

import java.util.List;

public interface MacrosDataSource {

    List<Long> foodSearch(String keyword);

    List<Food> getAllFoods();

    Food getFoodById(Long id);

    Food getFoodByIndexName(String indexName);

    List<Food> getFoodsById(List<Long> foodIds);

    Meal getMealById(Long id);

    List<Long> getMealIdsForDay(DateStamp day);

    /* These functions construct objects for all necessary entities that match the query,
     * as well as all other entities referenced by them.
     * For example, getMealsForDay constructs all of the MealTable objects for one particular day,
     * along with their FoodPortions, their Foods, and all of the Servings of those Foods.
     * It's probably worth caching the results of these!
     */
    List<Meal> getMealsById(List<Long> mealIds);

    List<Meal> getMealsForDay(DateStamp day);

    <M extends MacrosPersistable<M>> boolean saveObject(M object);

    /* These functions save the objects given to them into the database, via INSERT or UPDATE.
     * The caller should ensure that objects with an id of null correspond to new entries
     * (INSERTs) into the database, while those with a non-null id correspond to UPDATES of existing
     * rows in the table.
     *
     * Any data that originated from the user should already have been validated
     */
    // Do we really need the list methods? The user will probably only edit one object at a time;
    // except for deleting a bunch of foodPortions from one meal, or servings from a food
    <M extends MacrosPersistable<M>> void saveObjects(List<M> objects);

    <M extends MacrosPersistable<M>> boolean deleteObject(M object);

    <M extends MacrosPersistable<M>> void deleteObjects(List<M> objects);

    /*
     * FoodTable search done by substring matching the searchString against any of the given columns
     * Use prefixOnly to only allow matches at the start of the matching string.
     */
    //List<FoodTable> getMatchingFoods(String searchString, String[] columnNames, boolean prefixOnly);


}
