package com.machfour.macros.ingredients;

import com.machfour.macros.cli.utils.CliUtils;
import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.Schema;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.*;

import static com.machfour.macros.util.MiscUtils.toList;
import static org.junit.jupiter.api.Assertions.*;

class IngredientsTest {
    private static final String DB_LOCATION = "/home/max/devel/macros-kotlin/test/test-ingredients.sqlite";
    private static LinuxDatabase db;
    private static ColumnData<Food> foodData;
    // the food that will be made up of the other two foods
    private static Food testCompositeFood;
    // the foods that make up the composite foods
    private static Food testFood1;
    private static Food testFood2;
    // corresponding Ingredient Objects
    private static Food testIngredient1;
    private static Food testIngredient2;

    @BeforeAll
    static void initDb() {
        db = LinuxDatabase.getInstance(DB_LOCATION);
        try {
            db.deleteByColumn(Food.table(), Schema.FoodTable.FOOD_TYPE, toList(FoodType.COMPOSITE.getName()));
            db.clearTable(Ingredient.table());
        } catch (SQLException e) {
            System.out.println("Could not delete existing composite foods and/or clear ingredients table!");
            fail(e);
        }
    }

    @Test
    void testNutritionSum() {
        CompositeFoodSpec spec = new CompositeFoodSpec("chickpea-bread", "Chickpea bread", null, null);
        IngredientSpec waterSpec = new IngredientSpec("water", 875.0, "ml", "3.5 cups");
        IngredientSpec chickpeaSpec = new IngredientSpec("chickpea-flour", 625.0, "ml", "2.5 cups");
        //IngredientSpec chickpeaSpec = new IngredientSpec("chickpea-flour", 306.0, "g", "2.5 cups");
        IngredientSpec cookingSpec = new IngredientSpec("water", -493.0, "g", "cooked weight");
        IngredientSpec oilSpec = new IngredientSpec("olive-oil-cobram", 60.0, "ml", null);
        spec.addIngredients(Arrays.asList(waterSpec, chickpeaSpec, cookingSpec, oilSpec));

        CompositeFood recipe = null;
        try {
            Collection<CompositeFood> recipes = IngredientsParser.createCompositeFoods(Collections.singletonList(spec), db);
            assertEquals(1, recipes.size());
            recipe = recipes.iterator().next();
        } catch (SQLException e) {
            fail("SQL exception processing composite food spec: " + e);
        }
        assertNotNull(recipe);
        System.out.println("Nutrition data total");
        CliUtils.printNutritionData(recipe.getNutritionData(), false, System.out);
        System.out.println();
        System.out.println("Nutrition data per 100g");
        CliUtils.printNutritionData(recipe.getNutritionData().rescale(100), false, System.out);
}


    @BeforeEach
    void setUp() {
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