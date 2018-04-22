package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Tables;
import com.machfour.macros.storage.MacrosLinuxDatabase;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.machfour.macros.data.Columns.FoodCol.*;

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
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (SQLException e2) {
            e2.printStackTrace();
        }
    }

    void clearFoodTable() {
        try {
            db.removeAll(Tables.FoodTable.instance());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void doFood() {
        foodDc = new ColumnData<>(Tables.FoodTable.instance());
        foodDc.putData(ID, MacrosPersistable.NO_ID);
        foodDc.putData(CREATE_TIME, 0L);
        foodDc.putData(MODIFY_TIME, 0L);
        foodDc.putData(INDEX_NAME, "food1");
        foodDc.putData(BRAND, "Max's");
        foodDc.putData(COMMERCIAL_NAME, "Commercial");
        foodDc.putData(VARIETY_PREFIX_1, "really");
        foodDc.putData(VARIETY_PREFIX_2, "good");
        foodDc.putData(NAME, "food");
        foodDc.putData(VARIETY_SUFFIX, null);
        foodDc.putData(NOTES, "notes");
        foodDc.putData(CATEGORY, 1L);
        foodDc.putData(FOOD_TYPE, FoodType.PRIMARY.name);
        foodDc.putData(DENSITY, 1.0);
        foodDc.putData(USDA_INDEX, null);
        foodDc.putData(NUTTAB_INDEX, null);
        testFood = new Food(foodDc, false);
    }

    void saveALotOfFood() {
        List<Food> lotsOfFoods = new ArrayList<>(1000);
        for (long i = 0; i < 1000; i++) {
            ColumnData<Food> modifiedData = new ColumnData<>(foodDc);
            modifiedData.putData(ID, i);
            modifiedData.putData(INDEX_NAME, "food" + i);
            Food modifiedIndexName = new Food(modifiedData, false);
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