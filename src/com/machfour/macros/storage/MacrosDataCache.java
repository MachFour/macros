package com.machfour.macros.storage;

import com.machfour.macros.core.*;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Tables;
import com.machfour.macros.util.DateStamp;
import com.sun.istack.internal.NotNull;

import java.sql.SQLException;
import java.util.*;

public class MacrosDataCache implements MacrosDataSource {
    private static MacrosDataCache INSTANCE;
    private final MacrosDataSource upstream;

    /*
     * Object caches
     */
    private final List<Food> allFoodsCache;
    private final Map<Long, Meal> mealCache;
    private final Map<Long, Food> foodCache;
    private boolean allFoodsNeedsRefresh;

    private MacrosDataCache() {
        upstream = MacrosLinuxDatabase.getInstance();
        mealCache = new HashMap<>(100);
        foodCache = new LinkedHashMap<>(100);
        allFoodsCache = new ArrayList<>(100);
        allFoodsNeedsRefresh = true;

    }

    public static MacrosDataCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MacrosDataCache();
        }
        return INSTANCE;
    }

    @Override
    public <M extends MacrosPersistable<M>> int deleteObject(M object) throws SQLException {
        onDbWrite(object);
        return upstream.deleteObject(object);
    }

    @Override
    public <M extends MacrosPersistable<M>> int deleteObjects(List<M> objects) throws SQLException {
        for (M object : objects) {
            onDbWrite(object);
        }
        return upstream.deleteObjects(objects);
    }

    @Override
    public List<Long> foodSearch(String keyword) throws SQLException {
        return upstream.foodSearch(keyword);
    }

    @Override
    public List<Food> getAllFoods() throws SQLException {
        if (allFoodsNeedsRefresh) {
            List<Food> allFoods = upstream.getAllFoods();
            foodCache.clear();
            for (Food f : allFoods) {
                foodCache.put(f.getId(), f);
            }
            allFoodsNeedsRefresh = false;
        }

        return new ArrayList<>(foodCache.values());
    }

    @Override
    public Food getFoodById(Long id) throws SQLException {
        return upstream.getFoodById(id);
    }

    @Override
    public Food getFoodByIndexName(String indexNames) throws SQLException {
        return upstream.getFoodByIndexName(indexNames);
    }

    @Override
    public List<Food> getFoodsById(List<Long> foodIds) throws SQLException {
        return upstream.getFoodsById(foodIds);
    }

    @Override
    public Meal getMealById(Long id) throws SQLException {
        return upstream.getMealById(id);
    }

    @Override
    public List<Meal> getMealsForDay(DateStamp day) throws SQLException {
        List<Long> mealIds = getMealIdsForDay(day);
        return getMealsById(mealIds);
    }

    @Override
    public List<Meal> getMealsById(@NotNull List<Long> mealIds) throws SQLException {
        List<Long> unCachedIds = new ArrayList<>(mealIds.size());
        List<Meal> mealsToReturn = new ArrayList<>(mealIds.size());
        for (Long id : mealIds) {
            if (mealCache.containsKey(id)) {
                mealsToReturn.add(mealCache.get(id));
            } else {
                unCachedIds.add(id);
            }
        }
        List<Meal> freshMeals = getUncachedMealsById(unCachedIds);
        mealsToReturn.addAll(freshMeals);

        return mealsToReturn;
    }

    private List<Meal> getUncachedMealsById(@NotNull List<Long> mealIds) throws SQLException {
        List<Meal> UncachedMeals = upstream.getMealsById(mealIds);
        for (Meal meal : UncachedMeals) {
            mealCache.put(meal.getId(), meal);
        }
        return UncachedMeals;
    }

    @Override
    public List<Long> getMealIdsForDay(DateStamp day) throws SQLException {
        return upstream.getMealIdsForDay(day);
    }

    @Override
    public <M extends MacrosPersistable<M>> int saveObject(M object) throws SQLException {
        onDbWrite(object);
        return upstream.saveObject(object);
    }

    @Override
    public <M extends MacrosPersistable<M>> int saveObjects(List<M> objects) throws SQLException {
        for (M object : objects) {
            onDbWrite(object);
        }
        return upstream.saveObjects(objects);
    }

    private <M extends MacrosPersistable<M>> void onDbWrite(M object) {
        if (object instanceof Food) {
            allFoodsNeedsRefresh = true;
            unCache(object.getId(), Tables.FoodTable.instance());
        } else if (object instanceof Meal) {
            unCache(object.getId(), Tables.MealTable.instance());
        } else if (object instanceof FoodPortion) {
            unCache(((FoodPortion) object).getMealId(), Tables.MealTable.instance());
        } else if (object instanceof Serving) {
            unCache(((Serving) object).getFoodId(), Tables.FoodTable.instance());
        }
    }

    private <M extends MacrosPersistable<M>> void unCache(Long id, Table<M> type) {
        if (type instanceof Tables.FoodTable) {
            foodCache.remove(id);
        } else if (type instanceof Tables.MealTable) {
            mealCache.remove(id);
        }
    }
}

