package ingredients

import com.machfour.macros.core.Schema
import com.machfour.macros.ingredients.IngredientsParser
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.objects.Food
import com.machfour.macros.objects.FoodType
import com.machfour.macros.objects.Ingredient
import com.machfour.macros.queries.FoodQueries
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.io.FileReader
import java.io.IOException
import java.sql.SQLException

import org.junit.jupiter.api.Assertions.*

class IngredientsRollbackTest {
    companion object {
        private val TEST_DB_LOCATION = "/home/max/devel/macros-kotlin/test-ingredients.sqlite"
        private lateinit var db: LinuxDatabase

        @BeforeAll
        fun initDb() {
            db = LinuxDatabase.getInstance(TEST_DB_LOCATION)
            try {
                db.deleteByColumn(Food.table(), Schema.FoodTable.FOOD_TYPE, listOf(FoodType.COMPOSITE.niceName))
                db.clearTable(Ingredient.table)
            } catch (e: SQLException) {
                println("Could not delete existing composite foods and/or clear ingredients table!")
                fail<Any>(e)
            }

        }
    }

    // the ingredients list has invalid quantity units, so we expect that the composite food should not be saved either,
    // even though it is inserted first
    @Test
    fun testRollback() {
        var indexName: String? = null
        try {
            FileReader("/home/max/devel/macros-test-data/valid-food-invalid-ingredients.json").use { r ->
                val foods = IngredientsParser.readRecipes(r, db)

                assertEquals(1, foods.size)
                assertNotNull(foods[0])
                indexName = foods[0].indexName
                IngredientsParser.saveRecipes(foods, db)
                fail<Any>("saveRecipes() did not throw an SQLException")
            }
        } catch (e1: IOException) {
            fail<Any>(e1)
        } catch (e2: SQLException) {
            // we expect a foreign key constraint failure, do nothing
        }

        assertNotNull(indexName)
        try {
            val f = FoodQueries.getFoodByIndexName(db, indexName!!)
            assertNull(f, "Composite food was saved in the database, but should not have been")
        } catch (e2: SQLException) {
            fail<Any>(e2)
        }

    }

}

