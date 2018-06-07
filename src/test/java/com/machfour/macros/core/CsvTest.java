package com.machfour.macros.core;

import com.machfour.macros.storage.CsvStorage;
import com.machfour.macros.storage.MacrosLinuxDatabase;
import com.machfour.macros.util.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvTest {
    static MacrosLinuxDatabase db;

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

    @Test
    void testCsvReadFoods() {
        Pair<List<Food>, List<NutritionData>> csvObjects;
        try {
            csvObjects = CsvStorage.buildFoodObjectTree(CsvStorage.FOOD_CSV_FILENAME);
            assertNotEquals(0, csvObjects.first.size(), "CSV read in zero foods!");
            System.out.println(csvObjects.first.get(0));
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException was thrown");
        }
    }
    @Test
    void testCsvReadServings() {
        List<Serving> csvServings;
        try {
            csvServings = CsvStorage.buildServings(CsvStorage.SERVING_CSV_FILENAME);
            assertNotEquals(0, csvServings.size(), "CSV read in zero servings!");
            System.out.println(csvServings.get(0));
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException was thrown");
        }
    }
    @Test
    void testCsvSaveFoods() {
        Pair<List<Food>, List<NutritionData>> csvObjects;
        try {
            csvObjects = CsvStorage.buildFoodObjectTree(CsvStorage.FOOD_CSV_FILENAME);
            db.saveObjects(csvObjects.first, ObjectSource.IMPORT);
            db.saveObjects(csvObjects.second, ObjectSource.IMPORT);
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException was thrown");
        } catch (SQLException e2) {
            e2.printStackTrace();
            fail("Database save threw SQL exception");
        }
    }
    @Test
    void testCsvSaveServings() {
        List<Serving> csvServings;
        try {
            csvServings = CsvStorage.buildServings(CsvStorage.SERVING_CSV_FILENAME);
            db.saveObjects(csvServings, ObjectSource.IMPORT);
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException was thrown");
        } catch (SQLException e2) {
            e2.printStackTrace();
            fail("Database save threw SQL exception");
        }
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }
}