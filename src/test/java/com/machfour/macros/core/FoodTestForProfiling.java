package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Schema;
import com.machfour.macros.storage.MacrosLinuxDatabase;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FoodTestForProfiling {
    static MacrosLinuxDatabase db;
    static ColumnData<Food> foodDc;
    static Food testFood;

    static void initDb() {
        db = MacrosLinuxDatabase.getInstance();
        try {
            db.deleteIfExists();
            db.initDb();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    void clearFoodTable() {
        try {
            db.removeAll(Schema.FoodTable.instance());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void doFood() {
        foodDc = new ColumnData<>(Schema.FoodTable.instance());
        foodDc.put(Schema.FoodTable.ID, MacrosPersistable.NO_ID);
        foodDc.put(Schema.FoodTable.CREATE_TIME, 0L);
        foodDc.put(Schema.FoodTable.MODIFY_TIME, 0L);
        foodDc.put(Schema.FoodTable.INDEX_NAME, "food1");
        foodDc.put(Schema.FoodTable.BRAND, "Max's");
        foodDc.put(Schema.FoodTable.VARIETY, "really good");
        foodDc.put(Schema.FoodTable.NAME, "food");
        foodDc.put(Schema.FoodTable.VARIETY_AFTER_NAME, false);
        foodDc.put(Schema.FoodTable.NOTES, "notes");
        foodDc.put(Schema.FoodTable.CATEGORY, "Dairy");
        foodDc.put(Schema.FoodTable.FOOD_TYPE, FoodType.PRIMARY.name);
        foodDc.put(Schema.FoodTable.USDA_INDEX, null);
        foodDc.put(Schema.FoodTable.NUTTAB_INDEX, null);
        testFood = new Food(foodDc, ObjectSource.IMPORT);
    }

    void saveALotOfFood() {
        List<Food> lotsOfFoods = new ArrayList<>(1000);
        for (long i = 0; i < 1000; i++) {
            ColumnData<Food> modifiedData = foodDc.copy();
            modifiedData.put(Schema.FoodTable.ID, i);
            modifiedData.put(Schema.FoodTable.INDEX_NAME, "food" + i);
            Food modifiedIndexName = new Food(modifiedData, ObjectSource.IMPORT);
            lotsOfFoods.add(modifiedIndexName);
        }
        try {
            db.insertObjects(lotsOfFoods, true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        initDb();
        doFood();
        FoodTestForProfiling f = new FoodTestForProfiling();
        f.saveALotOfFood();
    }

}