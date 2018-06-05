package com.machfour.macros.core;

import com.machfour.macros.storage.CsvStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvTest {

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

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }
}