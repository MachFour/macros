package com.machfour.macros.core;

import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.FoodType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sqlite.SQLiteException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FoodTest {
    private static final String DB_LOCATION = "/home/max/devel/macros-java/test.sqlite";
    private static LinuxDatabase db;
    private static ColumnData<Food> foodData;
    private static Food testFood;

    @BeforeAll
    static void initDb() {
        db = LinuxDatabase.getInstance(DB_LOCATION);
        try {
            db.deleteIfExists(DB_LOCATION);
            db.initDb();
        } catch (IOException e1) {
            e1.printStackTrace();
            fail("Database initialisation threw IO exception");
        } catch (SQLException e2) {
            e2.printStackTrace();
            fail("Database initialisation threw SQL exception");
        }
    }

    @BeforeAll
    static void doFood() {
        foodData = new ColumnData<>(Schema.FoodTable.instance());
        foodData.put(Schema.FoodTable.ID, MacrosEntity.NO_ID);
        foodData.put(Schema.FoodTable.CREATE_TIME, 0L);
        foodData.put(Schema.FoodTable.MODIFY_TIME, 0L);
        foodData.put(Schema.FoodTable.INDEX_NAME, "food1");
        foodData.put(Schema.FoodTable.BRAND, "Max's");
        foodData.put(Schema.FoodTable.VARIETY, "really good");
        foodData.put(Schema.FoodTable.NAME, "food");
        foodData.put(Schema.FoodTable.VARIETY_AFTER_NAME, false);
        foodData.put(Schema.FoodTable.NOTES, "notes");
        foodData.put(Schema.FoodTable.CATEGORY, "dairy");
        foodData.put(Schema.FoodTable.FOOD_TYPE, FoodType.PRIMARY.getName());
        foodData.put(Schema.FoodTable.USDA_INDEX, null);
        foodData.put(Schema.FoodTable.NUTTAB_INDEX, null);
        testFood = Food.factory().construct(foodData, ObjectSource.IMPORT);
    }

    @Test
    void getFoodFromDb() {
        ColumnData<Food> modifiedData = foodData.copy();
        modifiedData.put(Schema.FoodTable.ID, 50L);
        Food f = Food.factory().construct(modifiedData, ObjectSource.RESTORE);
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
            ColumnData<Food> modifiedData = foodData.copy();
            modifiedData.put(Schema.FoodTable.ID, i);
            modifiedData.put(Schema.FoodTable.INDEX_NAME, "food" + i);
            Food modifiedIndexName = Food.factory().construct(modifiedData, ObjectSource.RESTORE);
            lotsOfFoods.add(modifiedIndexName);
        }
        try {
            assertEquals(1000, db.insertObjects(lotsOfFoods, true));
        } catch (SQLiteException e) {
            fail("DB save threw SQLite exception with result code: " + e.getResultCode());
            e.printStackTrace();
        } catch (SQLException e2) {
            e2.printStackTrace();
            fail("DB save threw exception");
        }
    }

    @Test
    void saveFoodFromDb() {
        ColumnData<Food> modifiedData = foodData.copy();
        modifiedData.put(Schema.FoodTable.ID, 50L);
        Food f = Food.factory().construct(modifiedData, ObjectSource.RESTORE);
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
        Food f1 = Food.factory().construct(modifiedData2, ObjectSource.DB_EDIT);
        try {
            assertEquals(1, db.saveObject(f1));
        } catch (SQLException e) {
            e.printStackTrace();
            fail("DB save threw exception");
        }
    }

    @Test
    void testSaveWithId() {
        ColumnData<Food> modifiedData = foodData.copy();
        modifiedData.put(Schema.FoodTable.ID, 500L);
        Food f = Food.factory().construct(modifiedData, ObjectSource.RESTORE);
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

    private void clearFoodTable() {
        try {
            db.clearTable(Schema.FoodTable.instance());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Deleting all foods threw SQL exception");
        }
    }


    @AfterEach
    void tearDown() {
    }
}