package com.machfour.macros.core;

import com.machfour.macros.storage.CsvStorage;
import com.machfour.macros.storage.MacrosLinuxDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
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
    void testCsvRead() {
        List<Food> csvFoods;
        try {
            csvFoods = CsvStorage.buildFoodObjectTree(CsvStorage.FOOD_CSV_FILENAME);
            assertNotEquals(0, csvFoods.size(), "CSV read in zero foods!");
            System.out.println(csvFoods.get(0));
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException was thrown");
        }
    }
    @Test
    void testCsvSave() {
        List<Food> csvFoods;
        try {
            csvFoods = CsvStorage.buildFoodObjectTree(CsvStorage.FOOD_CSV_FILENAME);
            for (Food f : csvFoods) {
                db.saveObject(f);
                db.saveObject(f.getNutritionData());
            }
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