package com.machfour.macros.objects;

import com.machfour.macros.core.*;
import com.machfour.macros.data.ExampleFood;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static com.machfour.macros.core.Schema.FoodPortionTable.*;

class NutritionDataTest {

    private static Food f = ExampleFood.food2;
    private static NutritionData nd1; // grams
    private static NutritionData nd2; // ml
    private static NutritionData nd3; // mg

    static {
        MacrosBuilder<FoodPortion> fpBuilder = new MacrosBuilder<>(FoodPortion.table());
        fpBuilder.setField(FOOD_ID, f.getId());
        fpBuilder.setField(MEAL_ID, MacrosEntity.NO_ID);
        fpBuilder.setField(QUANTITY, 100.0);
        fpBuilder.setField(QUANTITY_UNIT, QtyUnits.GRAMS.getAbbr());
        FoodPortion fp1 = fpBuilder.build();
        fpBuilder.setField(QUANTITY_UNIT, QtyUnits.MILLILITRES.getAbbr());
        FoodPortion fp2 = fpBuilder.build();
        fpBuilder.setField(QUANTITY, 100000.0);
        fpBuilder.setField(QUANTITY_UNIT, QtyUnits.MILLIGRAMS.getAbbr());
        FoodPortion fp3 = fpBuilder.build();


        fp1.setFood(f);
        fp2.setFood(f);
        fp3.setFood(f);

        nd1 = fp1.getNutritionData();
        nd2 = fp2.getNutritionData();
        nd3 = fp3.getNutritionData();
    }

    @Test
    void testScaling() {
        assertNotNull(f.getNutritionData().getDensity());
        double density = f.getNutritionData().getDensity();
        double fat = f.getNutritionData().getData(Schema.NutritionDataTable.FAT);
        double fat1 = nd1.getData(Schema.NutritionDataTable.FAT);
        double fat2 = nd2.getData(Schema.NutritionDataTable.FAT);

        assertEquals(density, 0.92);
        assertEquals(fat, fat2);
        assertEquals(fat / density, fat1);
    }

    @Test
    void testScaling2() {
        assertNotNull(f.getNutritionData().getDensity());
        double fat1 = nd1.getData(Schema.NutritionDataTable.FAT);
        double fat3 = nd3.getData(Schema.NutritionDataTable.FAT);

        assertEquals(fat1, fat3);
    }

    // TODO test complete data propagation through sum and combining of data

    @Test
    void testSum() {
        NutritionData sum = NutritionData.sum(Arrays.asList(nd1, nd2), true);
        assertEquals(192.0, (double) sum.getData(Schema.NutritionDataTable.FAT));
    }

}