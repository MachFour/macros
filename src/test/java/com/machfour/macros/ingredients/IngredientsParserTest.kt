package com.machfour.macros.ingredients

import com.machfour.macros.entities.CompositeFood
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.queries.WriteQueries
import com.machfour.macros.util.stringJoin
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.FileReader
import java.io.IOException
import java.sql.SQLException

class IngredientsParserTest {
    companion object {
        private const val TEST_DB_LOCATION = "/home/max/devel/macros/test-ingredients.sqlite"
        private lateinit var db: LinuxDatabase

        @BeforeAll
        fun initDb() {
            db = LinuxDatabase.getInstance(TEST_DB_LOCATION)
            try {
                WriteQueries.deleteAllIngredients(db)
                WriteQueries.deleteAllCompositeFoods(db)
            } catch (e: SQLException) {
                println("Could not delete existing composite foods and/or clear ingredients table!")
                Assertions.fail(e)
            }
        }
    }

    @Test
    fun deserialise() {
        try {
            FileReader("/home/max/devel/macros-test-data/mayo-recipes.json").use { r ->
                val ingredientSpecs = IngredientsParser.deserialiseIngredientsJson(r)
                @Suppress("UNUSED")
                val newFoods: Collection<CompositeFood> = IngredientsParser.createCompositeFoods(ingredientSpecs, db)
                println("Composite Foods Read:")
                println(stringJoin(ingredientSpecs, sep = "\n"))
            }
        } catch (e: IOException) {
            Assertions.fail(e)
        } catch (e: SQLException) {
            Assertions.fail(e)
        }
    }

    @Test
    fun testCreate() {
        try {
            FileReader("/home/max/devel/macros-test-data/mayo-recipes.json").use { r ->
                val ingredientSpecs = IngredientsParser.deserialiseIngredientsJson(r)
                val newFoods: Collection<CompositeFood> = IngredientsParser.createCompositeFoods(ingredientSpecs, db)
                println("Composite Foods created:")
                for (f in newFoods) {
                    println(f)
                }
            }
        } catch (e: IOException) {
            Assertions.fail(e)
        } catch (e: SQLException) {
            Assertions.fail(e)
        }
    }

    @Test
    fun testSave() {
        try {
            FileReader("/home/max/devel/macros-test-data/mayo-recipes.json").use { r ->
                val recipes = IngredientsParser.readRecipes(r, db)
                IngredientsParser.saveRecipes(recipes, db)
            }
        } catch (e: IOException) {
            Assertions.fail(e)
        } catch (e: SQLException) {
            Assertions.fail(e)
        }
    }

}