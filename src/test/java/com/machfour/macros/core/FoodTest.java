package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Schema;
import com.machfour.macros.storage.MacrosLinuxDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
            db.deleteIfExists();
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
            db.removeAll(Schema.FoodTable.instance());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Deleting all foods threw SQL exception");
        }
    }

    @BeforeAll
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

    @Test
    void getFoodFromDb() {
        ColumnData<Food> modifiedData = foodDc.copy();
        modifiedData.put(Schema.FoodTable.ID, 50L);
        Food f = new Food(modifiedData, ObjectSource.RESTORE);
        try {
            // first save with known ID
            assertEquals(1, db.saveObject(f));
        } catch (SQLException e) {
            e.printStackTrace();
            fail("DB save threw exception");
        }
        Food f2 = null;
        try {
            f2 = db.getFoodById(50L);
        } catch (SQLException e) {
            e.printStackTrace();
            fail("DB get threw exception");
        }
        assertTrue(f.equalsWithoutMetadata(f2), "Foods did not match in equals sense (ignoring metadata)");

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
            ColumnData<Food> modifiedData = foodDc.copy();
            modifiedData.put(Schema.FoodTable.ID, i);
            modifiedData.put(Schema.FoodTable.INDEX_NAME, "food" + i);
            Food modifiedIndexName = new Food(modifiedData, ObjectSource.RESTORE);
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
        ColumnData<Food> modifiedData = foodDc.copy();
        modifiedData.put(Schema.FoodTable.ID, 50L);
        Food f = new Food(modifiedData, ObjectSource.RESTORE);
        try {
            // first save with known ID
            assertEquals(1, db.saveObject(f));
        } catch (SQLException e) {
            e.printStackTrace();
            fail("DB save threw exception");
        }
        // now change the data and save with same ID
        ColumnData<Food> modifiedData2 = modifiedData.copy();
        modifiedData2.put(Schema.FoodTable.NAME, "newName");
        Food f1 = new Food(modifiedData2, ObjectSource.DB_EDIT);
        try {
            assertEquals(1, db.saveObject(f1));
        } catch (SQLException e) {
            e.printStackTrace();
            fail("DB save threw exception");
        }
    }

    @Test
    void testSaveWithId() {
        ColumnData<Food> modifiedData = foodDc.copy();
        modifiedData.put(Schema.FoodTable.ID, 500L);
        Food f = new Food(modifiedData, ObjectSource.RESTORE);
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