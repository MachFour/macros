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
            if (db.dbExists()) {
                db.removeDb();
            }
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
        foodDc.putData(Schema.FoodTable.ID, MacrosPersistable.NO_ID);
        foodDc.putData(Schema.FoodTable.CREATE_TIME, 0L);
        foodDc.putData(Schema.FoodTable.MODIFY_TIME, 0L);
        foodDc.putData(Schema.FoodTable.INDEX_NAME, "food1");
        foodDc.putData(Schema.FoodTable.BRAND, "Max's");
        foodDc.putData(Schema.FoodTable.VARIETY, "really good");
        foodDc.putData(Schema.FoodTable.NAME, "food");
        foodDc.putData(Schema.FoodTable.VARIETY_AFTER_NAME, false);
        foodDc.putData(Schema.FoodTable.NOTES, "notes");
        foodDc.putData(Schema.FoodTable.CATEGORY, "Dairy");
        foodDc.putData(Schema.FoodTable.FOOD_TYPE, FoodType.PRIMARY.name);
        foodDc.putData(Schema.FoodTable.USDA_INDEX, null);
        foodDc.putData(Schema.FoodTable.NUTTAB_INDEX, null);
        testFood = new Food(foodDc, ObjectSource.IMPORT);
    }

    void saveALotOfFood() {
        List<Food> lotsOfFoods = new ArrayList<>(1000);
        for (long i = 0; i < 1000; i++) {
            ColumnData<Food> modifiedData = new ColumnData<>(foodDc);
            modifiedData.putData(Schema.FoodTable.ID, i);
            modifiedData.putData(Schema.FoodTable.INDEX_NAME, "food" + i);
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