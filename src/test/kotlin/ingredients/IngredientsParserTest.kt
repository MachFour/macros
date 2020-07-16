package ingredients

import com.machfour.macros.core.Schema
import com.machfour.macros.ingredients.IngredientsParser
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.objects.CompositeFood
import com.machfour.macros.objects.Food
import com.machfour.macros.objects.FoodType
import com.machfour.macros.objects.Ingredient
import com.machfour.macros.util.MiscUtils.toList
import com.machfour.macros.util.StringJoiner
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.FileReader
import java.io.IOException
import java.sql.SQLException

internal class IngredientsParserTest {
    companion object {
        private const val TEST_DB_LOCATION = "/home/max/devel/macros-java/test-ingredients.sqlite"
        private lateinit var db: LinuxDatabase

        @BeforeAll
        fun initDb() {
            db = LinuxDatabase.getInstance(TEST_DB_LOCATION)
            try {
                db.deleteByColumn(Food.table(), Schema.FoodTable.FOOD_TYPE, toList(FoodType.COMPOSITE.getName()))
                db.clearTable(Ingredient.table())
            } catch (e: SQLException) {
                println("Could not delete existing composite foods and/or clear ingredients table!")
                Assertions.fail<Any>(e)
            }
        }
    }

    @Test
    fun deserialise() {
        try {
            FileReader("/home/max/devel/macros-test-data/mayo-recipes.json").use { r ->
                val ingredientSpecs = IngredientsParser.deserialiseIngredientsJson(r)
                val newFoods: Collection<CompositeFood> = IngredientsParser.createCompositeFoods(ingredientSpecs, db)
                println("Composite Foods Read:")
                println(StringJoiner.of(ingredientSpecs).sep("\n").join())
            }
        } catch (e: IOException) {
            Assertions.fail<Any>(e)
        } catch (e: SQLException) {
            Assertions.fail<Any>(e)
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
            Assertions.fail<Any>(e)
        } catch (e: SQLException) {
            Assertions.fail<Any>(e)
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
            Assertions.fail<Any>(e)
        } catch (e: SQLException) {
            Assertions.fail<Any>(e)
        }
    }

}