package com.machfour.macros.storage;

import com.machfour.macros.core.*;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.FoodPortion;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.objects.Serving;
import com.machfour.macros.util.DateStamp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public void beginTransaction() throws SQLException {
        upstream.beginTransaction();
    }

    @Override
    public void endTransaction() throws SQLException {
        upstream.endTransaction();
    }
    @Override
    public void openConnection() throws SQLException {
        upstream.openConnection();
    }

    @Override
    public void closeConnection() throws SQLException {
        upstream.closeConnection();
    }

    @Override
    public <M extends MacrosEntity<M>> int deleteObject(M object) throws SQLException {
        onDbWrite(object);
        return upstream.deleteObject(object);
    }

    @Override
    public <M extends MacrosEntity<M>> int deleteObjects(List<M> objects) throws SQLException {
        for (M object : objects) {
            onDbWrite(object);
        }
        return upstream.deleteObjects(objects);
    }

    @Override
    public <M extends MacrosEntity<M>> List<M> completeForeignKeys(Collection<M> objects, Column.Fk<M, ?, ?> fk) throws SQLException {
        return upstream.completeForeignKeys(objects, fk);
    }

    @Override
    public <M extends MacrosEntity<M>> List<M> completeForeignKeys(Collection<M> objects, List<Column.Fk<M, ?, ?>> which) throws SQLException {
        return upstream.completeForeignKeys(objects, which);
    }

    @Override
    public Set<Long> foodSearch(String keyword) throws SQLException {
        return upstream.foodSearch(keyword);
    }

    @Override
    // TODO should this only return the ID, for caching purposes
    public @Nullable Meal getCurrentMeal() throws SQLException {
        return upstream.getCurrentMeal();
    }

    @Override
    public List<Food> getAllFoods() throws SQLException {
        if (foodCache.isEmpty() || allFoodsNeedsRefresh) {
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
    public Map<Long, Food> getFoodsById(Collection<Long> foodIds) throws SQLException {
        return upstream.getFoodsById(foodIds);
    }

    @Override
    public Map<String, Food> getFoodsByIndexName(Collection<String> indexNames) throws SQLException {
        return upstream.getFoodsByIndexName(indexNames);
    }

    @Override
    public Map<String, Long> getFoodIdsByIndexName(Collection<String> indexNames) throws SQLException {
        return upstream.getFoodIdsByIndexName(indexNames);
    }

    @Override
    public Meal getMealById(Long id) throws SQLException {
        return upstream.getMealById(id);
    }

    @Override
    public Map<String, Meal> getMealsForDay(DateStamp day) throws SQLException {
        return upstream.getMealsForDay(day);
    }

    @Override
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
        Map<Long, Meal> uncachedMeals = upstream.getMealsById(mealIds);
        mealCache.putAll(uncachedMeals);
        return uncachedMeals;
    }

    @Override
    public List<Long> getMealIdsForDay(DateStamp day) throws SQLException {
        return upstream.getMealIdsForDay(day);
    }

    @Override
    public <M extends MacrosEntity<M>> int saveObject(M object) throws SQLException {
        onDbWrite(object);
        return upstream.saveObject(object);
    }


    @Override
    public <M extends MacrosEntity<M>> int updateObjects(Collection<? extends M> objects) throws SQLException {
        for (M object : objects) {
            onDbWrite(object);
        }
        return upstream.updateObjects(objects);
    }
    @Override
    public <M extends MacrosEntity<M>> int insertObjects(Collection<? extends M> objects, boolean withId) throws SQLException {
        for (M object : objects) {
            onDbWrite(object);
        }
        return upstream.updateObjects(objects);
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

