package com.machfour.macros.storage;

import com.machfour.macros.core.MacrosConfig;
import com.machfour.macros.core.datatype.TypeCastException;
import com.machfour.macros.linux.LinuxConfig;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.Serving;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.util.Pair;
import org.junit.jupiter.api.*;

import java.io.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CsvTest {
    static LinuxDatabase db;
    // TODO make test config
    static final MacrosConfig config = new LinuxConfig();
    static final String TEST_DB_LOCATION = "/home/max/devel/macros-kotlin/test.sqlite";
    static final String REAL_DB_LOCATION = "/home/max/devel/macros-kotlin/macros.sqlite";
    static final String TEST_WRITE_DIR = "/home/max/devel/macros-kotlin/test";

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
        try (Reader foodCsv = new FileReader(config.getFoodCsvPath()) ){
            csvFoods = CsvImport.buildFoodObjectTree(foodCsv);
            Assertions.assertNotEquals(0, csvFoods.size(), "CSV read in zero foods!");
            //System.out.println(csvObjects.first.get(0));
        } catch (IOException | TypeCastException e) {
            e.printStackTrace();
            fail("Exception was thrown");
        }
    }
    @Test
    void testCsvReadServings() {
        List<Serving> csvServings;
        try (Reader servingCsv = new FileReader(config.getServingCsvPath()) ){
            csvServings = CsvImport.buildServings(servingCsv);
            Assertions.assertNotEquals(0, csvServings.size(), "CSV read in zero servings!");
            System.out.println(csvServings.get(0));
        } catch (IOException | TypeCastException e) {
            e.printStackTrace();
            fail("Exception was thrown");
        }
    }

    @Test
    void testCsvSaveFoods() {
        Pair<List<Food>, List<NutritionData>> csvObjects;
        try (Reader foodCsv = new FileReader(config.getFoodCsvPath()) ){
            CsvImport.importFoodData(foodCsv, db, true);
        } catch (SQLException | IOException | TypeCastException e) {
            e.printStackTrace();
            fail("Exception was thrown");
        }
    }
    @Test
    void testCsvSaveServings() {
        List<Serving> csvServings;
        try (Reader servingCsv = new FileReader(config.getServingCsvPath())) {
            CsvImport.importServings(servingCsv, db, true);
        } catch (SQLException | IOException | TypeCastException e) {
            e.printStackTrace();
            fail("Exception was thrown");
        }
    }

    @Test
    void testCsvWriteFoods() {
        MacrosDataSource ds =  LinuxDatabase.getInstance(REAL_DB_LOCATION);
        try (Writer csvOut = new FileWriter(TEST_WRITE_DIR + "/all-food.csv")){
            Map<Long, Food> foods = db.getAllRawObjects(Food.table());
            CsvExport.writeObjectsToCsv(Food.table(), csvOut, foods.values());
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
        MacrosDataSource ds =  LinuxDatabase.getInstance(REAL_DB_LOCATION);
        try (Writer csvOut = new FileWriter(TEST_WRITE_DIR + "/all-serving.csv")) {
            Map<Long, Serving> servings = db.getAllRawObjects(Serving.table());
            CsvExport.writeObjectsToCsv(Serving.table(), csvOut, servings.values());
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            fail("Exception was thrown");
        }
    }
    // put this test after other imports
    @Test
    void testCsvSaveRecipes() {
        try (Reader foodCsv = new FileReader(config.getFoodCsvPath());
             Reader recipeCsv = new FileReader(config.getRecipeCsvPath());
             Reader ingredientCsv = new FileReader(config.getIngredientsCsvPath())) {
            CsvImport.importFoodData(foodCsv, db, true);
            CsvImport.importRecipes(recipeCsv, ingredientCsv, db);
        } catch (SQLException | IOException | TypeCastException e) {
            e.printStackTrace();
            fail("Exception was thrown");
    }
}


    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }
}