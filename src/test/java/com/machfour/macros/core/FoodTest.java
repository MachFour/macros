package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Tables;
import com.machfour.macros.storage.MacrosLinuxDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.machfour.macros.data.Columns.FoodCol.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FoodTest {
    static MacrosLinuxDatabase db;
    static ColumnData<Food> foodDc;
    static Food testFood;

    @BeforeAll
    static void initDb() {
        db = MacrosLinuxDatabase.getInstance();
        try {
            if (db.dbExists()) {
                db.removeDb();
            }
            db.initDb();
        } catch (IOException e1) {
            e1.printStackTrace();
            fail("Database initialisation threw IO exception");
        } catch (SQLException e2) {
            e2.printStackTrace();
            fail("Database initialisation threw SQL exception");
        }
    }

    void clearFoodTable() {
        try {
            db.removeAll(Tables.FoodTable.instance());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Deleting all foods threw SQL exception");
        }
    }

    @BeforeAll
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

    @Test
    void saveFoodNotFromDb() {
        try {
            assertEquals(1, db.saveObject(testFood));
        } catch (SQLException e) {
            e.printStackTrace();
            fail("DB save threw exception");
        }
    }

    @Test
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
            assertEquals(1000, db.insertObjects(lotsOfFoods, true));
        } catch (SQLException e) {
            e.printStackTrace();
            fail("DB save threw exception");
        }
    }

    @Test
    void saveFoodFromDb() {
        ColumnData<Food> modifiedData = new ColumnData<>(foodDc);
        modifiedData.putData(ID, 50L);
        Food f = new Food(modifiedData, false);
        try {
            // first save with known ID
            assertEquals(1, db.saveObject(f));
        } catch (SQLException e) {
            e.printStackTrace();
            fail("DB save threw exception");
        }
        // now change the data and save with same ID
        modifiedData.putData(NAME, "newName");
        Food f1 = new Food(modifiedData, true);
        try {
            assertEquals(1, db.saveObject(f1));
        } catch (SQLException e) {
            e.printStackTrace();
            fail("DB save threw exception");
        }
    }

    @Test
    void testSaveWithId() {
        ColumnData<Food> modifiedData = new ColumnData<>(foodDc);
        modifiedData.putData(ID, 500L);
        Food f = new Food(modifiedData, false);
        try {
            assertEquals(1, db.saveObject(f));
        } catch (SQLException e) {
            e.printStackTrace();
            fail("DB save threw exception");
        }
    }

    @BeforeEach
    void setUp() {
        clearFoodTable();
    }

    @AfterEach
    void tearDown() {
    }
}