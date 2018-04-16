package com.machfour.macros.storage;

import com.machfour.macros.core.*;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Tables;
import com.machfour.macros.util.DateStamp;
import com.sun.istack.internal.NotNull;

import java.util.*;

public class MacrosDataCache implements MacrosDataSource {
    private static MacrosDataCache INSTANCE;
    private final MacrosDataSource upstream;

    /*
     * Object caches
     */
    private List<Food> allFoodsCache;
    private Map<Long, Meal> mealCache;
    private Map<Long, Food> foodCache;
    private boolean allFoodsNeedsRefresh;

    private MacrosDataCache() {
        upstream = MacrosDatabase.getInstance();
        mealCache = new HashMap<>(100);
        foodCache = new LinkedHashMap<>(100);
        allFoodsNeedsRefresh = true;

    }

    public static MacrosDataCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MacrosDataCache();
        }
        return INSTANCE;
    }

    @Override
    public <M extends MacrosPersistable<M>> boolean deleteObject(M object) {
        onDbWrite(object);
        return upstream.deleteObject(object);
    }

    @Override
    public <M extends MacrosPersistable<M>> void deleteObjects(List<M> objects) {
        for (M object : objects) {
            onDbWrite(object);
        }
        upstream.deleteObjects(objects);
    }

    @Override
    public List<Long> foodSearch(String keyword) {
        return upstream.foodSearch(keyword);
    }

    @Override
    public List<Food> getAllFoods() {
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
    public Food getFoodById(Long id) {
        return upstream.getFoodById(id);
    }

    @Override
    public Food getFoodByIndexName(String indexNames) {
        return upstream.getFoodByIndexName(indexNames);
    }

    @Override
    public List<Food> getFoodsById(List<Long> foodIds) {
        return upstream.getFoodsById(foodIds);
    }

    @Override
    public Meal getMealById(Long id) {
        return upstream.getMealById(id);
    }

    @Override
    public List<Meal> getMealsForDay(DateStamp day) {
        List<Long> mealIds = getMealIdsForDay(day);
        return getMealsById(mealIds);
    }

    @Override
    public List<Meal> getMealsById(@NotNull List<Long> mealIds) {
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

    private List<Meal> getUncachedMealsById(@NotNull List<Long> mealIds) {
        List<Meal> UncachedMeals = upstream.getMealsById(mealIds);
        for (Meal meal : UncachedMeals) {
            mealCache.put(meal.getId(), meal);
        }
        return UncachedMeals;
    }

    @Override
    public List<Long> getMealIdsForDay(DateStamp day) {
        return upstream.getMealIdsForDay(day);
    }

    @Override
    public <M extends MacrosPersistable<M>> boolean saveObject(M object) {
        onDbWrite(object);
        return upstream.saveObject(object);
    }

    @Override
    public <M extends MacrosPersistable<M>> void saveObjects(List<M> objects) {
        for (M object : objects) {
            onDbWrite(object);
        }
        upstream.saveObjects(objects);
    }

    private <M extends MacrosPersistable<M>> void onDbWrite(M object) {
        if (object instanceof Food) {
            allFoodsNeedsRefresh = true;
            unCache(object.getId(), Tables.FoodTable.getInstance());
        } else if (object instanceof Meal) {
            unCache(object.getId(), Tables.MealTable.getInstance());
        } else if (object instanceof FoodPortion) {
            unCache(((FoodPortion) object).getMealId(), Tables.MealTable.getInstance());
        } else if (object instanceof Serving) {
            unCache(((Serving) object).getFoodId(), Tables.FoodTable.getInstance());
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

