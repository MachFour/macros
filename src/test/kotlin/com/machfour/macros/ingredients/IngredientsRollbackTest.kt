package com.machfour.macros.ingredients

import com.machfour.macros.entities.Food
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.queries.deleteAllCompositeFoods
import com.machfour.macros.queries.deleteAllIngredients
import com.machfour.macros.queries.getFoodByIndexName
import com.machfour.macros.sql.SqlException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.FileReader
import java.io.IOException

class IngredientsRollbackTest {
    companion object {
        private const val TEST_DB_LOCATION = "/home/max/devel/macros/test-ingredients.sqlite"
        private lateinit var db: LinuxDatabase

        @BeforeAll
        fun initDb() {
            db = LinuxDatabase.getInstance(TEST_DB_LOCATION)
            try {
                deleteAllIngredients(db)
                deleteAllCompositeFoods(db)
            } catch (e: SqlException) {
                println("Could not delete existing composite foods and/or clear ingredients table!")
                fail<Any>(e)
            }

        }
    }

    // the ingredients list has invalid quantity units, so we expect that the composite food should not be saved either,
    // even though it is inserted first
    @Test
    fun testRollback() {
        val indexName: String = try {
            val f: Food
            FileReader("/home/max/devel/macros-test-data/valid-food-invalid-ingredients.json").use { r ->
                val foods = readRecipes(r, db)

                assertEquals(1, foods.size)
                assertNotNull(foods[0])
                f = foods[0]
                saveRecipes(foods, db)
                fail<Any>("saveRecipes() did not throw an SqlException")
            }
            f.indexName
        } catch (e1: IOException) {
            fail(e1)
        } catch (e2: SqlException) {
            // we expect a foreign key constraint failure, do nothing
            fail(e2)
        }

        try {
            val f = getFoodByIndexName(db, indexName)
            assertNull(f, "Composite food was saved in the database, but should not have been")
        } catch (e2: SqlException) {
            fail(e2)
        }

    }

}

