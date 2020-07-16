package com.machfour.macros.ingredients;

import com.machfour.macros.core.Schema;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.CompositeFood;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.FoodType;
import com.machfour.macros.objects.Ingredient;
import com.machfour.macros.queries.FoodQueries;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;

import static com.machfour.macros.util.MiscUtils.toList;
import static org.junit.jupiter.api.Assertions.*;

class IngredientsRollbackTest {
    private static final String TEST_DB_LOCATION = "/home/max/devel/macros-kotlin/test-ingredients.sqlite";
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

    // the ingredients list has invalid quantity units, so we expect that the composite food should not be saved either,
    // even though it is inserted first
    @Test
    void testRollback() {
        String indexName = null;
        try (Reader r = new FileReader("/home/max/devel/macros-test-data/valid-food-invalid-ingredients.json")) {
            List<CompositeFood> foods = IngredientsParser.readRecipes(r, db);

            assertEquals(1, foods.size());
            assertNotNull(foods.get(0));
            indexName = foods.get(0).getIndexName();
            IngredientsParser.saveRecipes(foods, db);
            fail("saveRecipes() did not throw an SQLException");
        } catch (IOException e1) {
            fail(e1);
        } catch (SQLException e2) {
            // we expect a foreign key constraint failure, do nothing
        }

        assertNotNull(indexName);
        try {
            Food f = FoodQueries.getFoodByIndexName(db, indexName);
            assertNull(f, "Composite food was saved in the database, but should not have been");
        } catch (SQLException e2) {
            fail(e2);
        }
    }
}

