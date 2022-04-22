package com.machfour.macros.ingredients

import com.machfour.macros.entities.CompositeFood
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.queries.deleteAllCompositeFoods
import com.machfour.macros.queries.deleteAllIngredients
import com.machfour.macros.sql.SqlException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.FileReader
import java.io.IOException

class IngredientsParserTest {
    companion object {
        private const val TEST_DB_LOCATION = "/home/max/devel/macros/test/test-ingredients.sqlite"
        private lateinit var db: LinuxDatabase
        @BeforeAll
        @JvmStatic
        fun initDb() {
            // TODO initialise database properly before each test
            db = LinuxDatabase.getInstance(TEST_DB_LOCATION)
            try {
                deleteAllIngredients(db)
                deleteAllCompositeFoods(db)
            } catch (e: SqlException) {
                println("Could not delete existing composite foods and/or clear ingredients table!")
                Assertions.fail(e)
            }
        }
    }

    @Test
    fun deserialise() {
        try {
            FileReader("/home/max/devel/macros-test-data/mayo-recipes.json").use { r ->
                val ingredientSpecs = deserialiseIngredientsJson(r)
                @Suppress("UNUSED")
                val newFoods: Collection<CompositeFood> = createCompositeFoods(ingredientSpecs, db)
                println("Composite Foods Read:")
                println(ingredientSpecs.joinToString("\n"))
            }
        } catch (e: IOException) {
            Assertions.fail(e)
        } catch (e: SqlException) {
            Assertions.fail(e)
        }
    }

    @Test
    fun testCreate() {
        try {
            FileReader("/home/max/devel/macros-test-data/mayo-recipes.json").use { r ->
                val ingredientSpecs = deserialiseIngredientsJson(r)
                val newFoods = createCompositeFoods(ingredientSpecs, db)
                println("Composite Foods created:")
                for (f in newFoods) {
                    println(f)
                }
            }
        } catch (e: IOException) {
            Assertions.fail(e)
        } catch (e: SqlException) {
            Assertions.fail(e)
        }
    }

    @Test
    fun testSave() {
        try {
            FileReader("/home/max/devel/macros-test-data/mayo-recipes.json").use { r ->
                val recipes = readRecipes(r, db)
                saveRecipes(recipes, db)
            }
        } catch (e: IOException) {
            Assertions.fail(e)
        } catch (e: SqlException) {
            Assertions.fail(e)
        }
    }

}