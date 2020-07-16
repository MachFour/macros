package com.machfour.macros.ingredients;

import com.machfour.macros.core.Schema;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.CompositeFood;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.FoodType;
import com.machfour.macros.objects.Ingredient;
import com.machfour.macros.util.StringJoiner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static com.machfour.macros.util.MiscUtils.toList;
import static org.junit.jupiter.api.Assertions.*;

class IngredientsParserTest {
    private static final String TEST_DB_LOCATION = "/home/max/devel/macros-java/test-ingredients.sqlite";
    private static LinuxDatabase db;

    @BeforeAll
    static void initDb() {
        db = LinuxDatabase.getInstance(TEST_DB_LOCATION);
        try {
            db.deleteByColumn(Food.table(), Schema.FoodTable.FOOD_TYPE, toList(FoodType.COMPOSITE.getName()));
            db.clearTable(Ingredient.table());
        } catch (SQLException e) {
            System.out.println("Could not delete existing composite foods and/or clear ingredients table!");
            fail(e);
        }
    }

    @Test
    void deserialise() {
        try (Reader r = new FileReader("/home/max/devel/macros-test-data/mayo-recipes.json")){
            Collection<CompositeFoodSpec> ingredientSpecs = IngredientsParser.deserialiseIngredientsJson(r);
            Collection<CompositeFood> newFoods = IngredientsParser.createCompositeFoods(ingredientSpecs, db);
            System.out.println("Composite Foods Read:");
            System.out.println(StringJoiner.of(ingredientSpecs).sep("\n").join());
        } catch (IOException | SQLException e) {
            fail(e);
        }
}

    @Test
    void testCreate() {
        try (Reader r = new FileReader("/home/max/devel/macros-test-data/mayo-recipes.json")){
            Collection<CompositeFoodSpec> ingredientSpecs = IngredientsParser.deserialiseIngredientsJson(r);
            Collection<CompositeFood> newFoods = IngredientsParser.createCompositeFoods(ingredientSpecs, db);
            System.out.println("Composite Foods created:");
            for (CompositeFood f : newFoods) {
                System.out.println(f);
            }
        } catch (IOException | SQLException e) {
            fail(e);
        }
    }

    @Test
    void testSave() {
        try (Reader r = new FileReader("/home/max/devel/macros-test-data/mayo-recipes.json")){
            List<CompositeFood> recipes = IngredientsParser.readRecipes(r, db);
            IngredientsParser.saveRecipes(recipes,  db);
        } catch (IOException | SQLException e) {
            fail(e);
        }
    }
}