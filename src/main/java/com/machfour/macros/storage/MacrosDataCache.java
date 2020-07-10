package com.machfour.macros.storage;

import com.machfour.macros.core.Column;
import com.machfour.macros.core.MacrosEntity;
import com.machfour.macros.core.ObjectSource;
import com.machfour.macros.core.Schema;
import com.machfour.macros.core.Table;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.FoodPortion;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.objects.Serving;
import com.machfour.macros.queries.FoodQueries;
import com.machfour.macros.queries.MealQueries;
import com.machfour.macros.queries.Queries;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MacrosDataCache {
    private static MacrosDataCache INSTANCE;
    private final MacrosDataSource upstream;

    /*
     * Object caches
     */
    private final List<Food> allFoodsCache;
    private final Map<Long, Meal> mealCache;
    private final Map<Long, Food> foodCache;
    private boolean allFoodsNeedsRefresh;

    private MacrosDataCache(MacrosDataSource upstream) {
        this.upstream = upstream;
        mealCache = new LinkedHashMap<>(100);
        foodCache = new LinkedHashMap<>(100);
        allFoodsCache = new ArrayList<>(100);
        allFoodsNeedsRefresh = true;

    }

    public static void initialise(MacrosDataSource upstream) {
        INSTANCE = new MacrosDataCache(upstream);
    }

    public static MacrosDataCache getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Not initialised with upstream data source");
        }
        return INSTANCE;
    }


    public <M extends MacrosEntity<M>> int deleteObject(M object) throws SQLException {
        onDbWrite(object);
        return Queries.deleteObject(upstream, object);
    }

    public <M extends MacrosEntity<M>> int deleteObjects(List<M> objects) throws SQLException {
        for (M object : objects) {
            onDbWrite(object);
        }
        return Queries.deleteObjects(upstream, objects);
    }


    @NotNull
    public <M> List<Long> stringSearch(Table<M> t, List<Column<M, String>> cols, String keyword,
                                       boolean globBefore, boolean globAfter) throws SQLException {
        return upstream.stringSearch(t, cols, keyword, globBefore, globAfter);
    }

    public List<Food> getAllFoods() throws SQLException {
        if (foodCache.isEmpty() || allFoodsNeedsRefresh) {
            List<Food> allFoods = FoodQueries.getAllFoods(upstream);
            foodCache.clear();
            for (Food f : allFoods) {
                foodCache.put(f.getId(), f);
            }
            allFoodsNeedsRefresh = false;
        }

        return new ArrayList<>(foodCache.values());
    }


    public Map<Long, Meal> getMealsById(@NotNull List<Long> mealIds) throws SQLException {
        List<Long> unCachedIds = new ArrayList<>(mealIds.size());
        Map<Long, Meal> mealsToReturn = new LinkedHashMap<>(mealIds.size(), 1);
        for (Long id : mealIds) {
            if (mealCache.containsKey(id)) {
                mealsToReturn.put(id, mealCache.get(id));
            } else {
                unCachedIds.add(id);
            }
        }
        Map<Long, Meal> freshMeals = getUncachedMealsById(unCachedIds);
        mealsToReturn.putAll(freshMeals);

        return mealsToReturn;
    }

    private Map<Long, Meal> getUncachedMealsById(@NotNull List<Long> mealIds) throws SQLException {
        Map<Long, Meal> uncachedMeals = MealQueries.getMealsById(upstream, mealIds);
        mealCache.putAll(uncachedMeals);
        return uncachedMeals;
    }

    public <M extends MacrosEntity<M>> int saveObject(M object) throws SQLException {
        onDbWrite(object);
        return Queries.saveObject(upstream, object);
    }

    public <M extends MacrosEntity<M>> int saveObjects(Collection<? extends M> objects, ObjectSource objectSource) throws SQLException {
        for (M object : objects) {
            onDbWrite(object);
        }
        return Queries.saveObjects(upstream, objects, objectSource);
    }

    public <M extends MacrosEntity<M>> int updateObjects(Collection<? extends M> objects) throws SQLException {
        for (M object : objects) {
            onDbWrite(object);
        }
        return Queries.updateObjects(upstream, objects);
    }
    public <M extends MacrosEntity<M>> int insertObjects(Collection<? extends M> objects, boolean withId) throws SQLException {
        for (M object : objects) {
            onDbWrite(object);
        }
        return Queries.updateObjects(upstream, objects);
    }

    public <M, J> int deleteByColumn(Table<M> t, Column<M, J> whereColumn, Collection<J> whereValues) throws SQLException {
        return upstream.deleteByColumn(t, whereColumn, whereValues);
    }

    private <M extends MacrosEntity<M>> void onDbWrite(M object) {
        if (object instanceof Food) {
            allFoodsNeedsRefresh = true;
            unCache(object.getId(), Schema.FoodTable.instance());
        } else if (object instanceof Meal) {
            unCache(object.getId(), Schema.MealTable.instance());
        } else if (object instanceof FoodPortion) {
            unCache(((FoodPortion) object).getMealId(), Schema.MealTable.instance());
        } else if (object instanceof Serving) {
            unCache(((Serving) object).getFoodId(), Schema.FoodTable.instance());
        }
    }

    private <M extends MacrosEntity<M>> void unCache(Long id, Table<M> type) {
        if (type instanceof Schema.FoodTable) {
            foodCache.remove(id);
        } else if (type instanceof Schema.MealTable) {
            mealCache.remove(id);
        }
    }
}

