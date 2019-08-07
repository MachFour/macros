package com.machfour.macros.core;

import com.machfour.macros.linux.Config;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.Serving;
import com.machfour.macros.storage.CsvStorage;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.util.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CsvTest {
    static LinuxDatabase db;
    static final String TEST_DB_LOCATION = "/home/max/devel/macros-java/test.sqlite";
    static final String REAL_DB_LOCATION = "/home/max/devel/macros-java/macros.sqlite";
    static final String TEST_WRITE_DIR = "/home/max/devel/macros-java/test";

    @BeforeAll
    static void initDb() {
        db = LinuxDatabase.getInstance(TEST_DB_LOCATION);
        try {
            db.deleteIfExists(TEST_DB_LOCATION);
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
        Map<String, Food> csvFoods;
        try (Reader foodCsv = new FileReader(Config.FOOD_CSV_FILENAME) ){
            csvFoods = CsvStorage.buildFoodObjectTree(foodCsv);
            assertNotEquals(0, csvFoods.size(), "CSV read in zero foods!");
            //System.out.println(csvObjects.first.get(0));
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException was thrown");
        }
    }
    @Test
    void testCsvReadServings() {
        List<Serving> csvServings;
        try (Reader servingCsv = new FileReader(Config.SERVING_CSV_FILENAME) ){
            csvServings = CsvStorage.buildServings(servingCsv);
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
        try (Reader foodCsv = new FileReader(Config.FOOD_CSV_FILENAME) ){
            CsvStorage.importFoodData(foodCsv, db, true);
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
        try (Reader servingCsv = new FileReader(Config.SERVING_CSV_FILENAME)) {
            CsvStorage.importServings(servingCsv, db, true);
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException was thrown");
        } catch (SQLException e2) {
            e2.printStackTrace();
            fail("Database save threw SQL exception");
        }
    }
    @Test
    void testCsvWriteFoods() {
        MacrosDatabase db = LinuxDatabase.getInstance(REAL_DB_LOCATION);
        try (Writer csvOut = new FileWriter(TEST_WRITE_DIR + "/all-food.csv")){
            Map<Long, Food> foods = db.getAllRawObjects(Food.table());
            CsvStorage.writeObjectsToCsv(Food.table(), csvOut, foods.values());
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException was thrown");
        } catch (SQLException e2) {
            e2.printStackTrace();
            fail("Database save threw SQL exception");
        }
    }
    @Test
    void testCsvWriteServings() {
        MacrosDatabase db = LinuxDatabase.getInstance(REAL_DB_LOCATION);
        try (Writer csvOut = new FileWriter(TEST_WRITE_DIR + "/all-serving.csv")) {
            Map<Long, Serving> servings = db.getAllRawObjects(Serving.table());
            CsvStorage.writeObjectsToCsv(Serving.table(), csvOut, servings.values());
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