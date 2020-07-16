package com.machfour.macros.core;

import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.QtyUnits;
import com.machfour.macros.queries.FoodQueries;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;

import static com.machfour.macros.core.Schema.NutritionDataTable.QUANTITY;
import static org.junit.jupiter.api.Assertions.*;

class DensityTest {
    private static final String DB_LOCATION = "/home/max/devel/macros-java/test.sqlite";
    private static LinuxDatabase db;
    private static Food chickpeaFlour;
    private static Food water;
    private static NutritionData chickpeaNd;
    private static NutritionData waterNd;

    @BeforeAll
    static void initDb() {
        db = LinuxDatabase.getInstance(DB_LOCATION);
        final String failMsg = "Could not find chickpea flour and water in DB. Has it been initialised with data?";
        try {
            chickpeaFlour = FoodQueries.getFoodByIndexName(db, "chickpea-flour");
            water = FoodQueries.getFoodByIndexName(db, "water");
            chickpeaNd = chickpeaFlour.getNutritionData();
            waterNd = water.getNutritionData();
            assertNotNull(chickpeaFlour, failMsg);
            assertNotNull(water, failMsg);
            assertNotNull(chickpeaNd);
            assertNotNull(waterNd);
            assertNotNull(chickpeaNd.getDensity());
            assertNotNull(waterNd.getDensity());
        } catch (SQLException e) {
            System.out.println(failMsg);
            fail(e);
        }
}


    @Test
    void testDensity() {
        Double density = chickpeaNd.getDensity();
        assertNotNull(density);
        NutritionData millilitresNd = chickpeaNd.rescale(100/density, QtyUnits.MILLILITRES);
        NutritionData backConverted = millilitresNd.rescale(100, QtyUnits.GRAMS);
        final Column<NutritionData, Double> carbs = Schema.NutritionDataTable.CARBOHYDRATE;
        assertEquals(chickpeaNd.getData(carbs), millilitresNd.getData(carbs), 0.01);
        assertEquals(chickpeaNd.getData(carbs), backConverted.getData(carbs), 0.01);
        System.out.println("Default quantity: " + chickpeaNd.getQuantity() + chickpeaNd.qtyUnitAbbr());
        System.out.println("mls quantity: " + millilitresNd.getQuantity() + millilitresNd.qtyUnitAbbr());
        System.out.println("backConverted quantity: " + backConverted.getQuantity() + backConverted.qtyUnitAbbr());
    }

    @Test
    void testDensity2() {
        NutritionData chickPea100mL = chickpeaNd.rescale(100, QtyUnits.MILLILITRES);
        NutritionData water100mL = waterNd.rescale(100, QtyUnits.MILLILITRES);
        NutritionData combined = NutritionData.sum(Arrays.asList(chickPea100mL, water100mL));
        assertEquals(QtyUnits.GRAMS, combined.getQtyUnit());
        assertEquals(100+100*chickpeaNd.getDensity(), combined.getQuantity());
        assertTrue(combined.hasCompleteData(QUANTITY));
    }
}

