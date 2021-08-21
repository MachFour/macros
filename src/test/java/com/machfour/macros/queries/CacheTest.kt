package com.machfour.macros.queries

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.*
import com.machfour.macros.entities.inbuilt.Units
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxSqlConfig
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.orm.schema.FoodPortionTable
import com.machfour.macros.orm.schema.FoodTable
import com.machfour.macros.orm.schema.MealTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.util.DateStamp
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.sql.SQLException

internal class CacheTest {

    companion object {
        private const val DB_LOCATION = "/home/max/devel/macros/test.sqlite"

        private lateinit var db: LinuxDatabase
        private lateinit var testFood: Food
        private lateinit var testFoodPortion: FoodPortion
        private lateinit var testMeal: Meal

        @BeforeAll
        @JvmStatic
        fun initDbAndObjects() {
            with (RowData(Food.table)) {
                put(FoodTable.ID, MacrosEntity.NO_ID)
                put(FoodTable.CREATE_TIME, 0L)
                put(FoodTable.MODIFY_TIME, 0L)
                put(FoodTable.INDEX_NAME, "food1")
                put(FoodTable.BRAND, "Max's")
                put(FoodTable.VARIETY, "really good")
                put(FoodTable.NAME, "food")
                put(FoodTable.NOTES, "notes")
                put(FoodTable.CATEGORY, "dairy")
                put(FoodTable.FOOD_TYPE, FoodType.PRIMARY.niceName)
                put(FoodTable.USDA_INDEX, null)
                put(FoodTable.NUTTAB_INDEX, null)
                testFood = Food.factory.construct(this, ObjectSource.IMPORT)
            }
            with (RowData(Meal.table)) {
                put(MealTable.ID, MacrosEntity.NO_ID)
                put(MealTable.CREATE_TIME, 0L)
                put(MealTable.MODIFY_TIME, 0L)
                put(MealTable.NAME, "meal")
                put(MealTable.NOTES, "meal notes")
                put(MealTable.DAY, DateStamp(2020, 10, 10))
                testMeal = Meal.factory.construct(this, ObjectSource.IMPORT)
            }
            with (RowData(FoodPortion.table)) {
                put(FoodPortionTable.ID, MacrosEntity.NO_ID)
                put(FoodPortionTable.CREATE_TIME, 0L)
                put(FoodPortionTable.MODIFY_TIME, 0L)
                put(FoodPortionTable.FOOD_ID, 1L)
                put(FoodPortionTable.MEAL_ID, 1L)
                put(FoodPortionTable.QUANTITY, 100.0)
                put(FoodPortionTable.QUANTITY_UNIT, Units.GRAMS.abbr)
                put(FoodPortionTable.NOTES, "food portion notes")
                testFoodPortion = FoodPortion.factory.construct(this, ObjectSource.IMPORT)

            }

            db = LinuxDatabase.getInstance(DB_LOCATION)
            try {
                LinuxDatabase.deleteIfExists(DB_LOCATION)
                db.initDb(LinuxSqlConfig())
            } catch (e1: IOException) {
                e1.printStackTrace()
                Assertions.fail<Any>("Database initialisation threw IO exception")
            } catch (e2: SQLException) {
                e2.printStackTrace()
                Assertions.fail<Any>("Database initialisation threw SQL exception")
            }
        }

    }

    @BeforeEach
    fun saveObjects() {
        try {
            println("Clearing tables")
            WriteQueries.clearTable(db, FoodPortion.table)
            WriteQueries.clearTable(db, Meal.table)
            WriteQueries.clearTable(db, Serving.table)
            WriteQueries.clearTable(db, FoodNutrientValue.table)
            WriteQueries.clearTable(db, Food.table)

            println("Saving objects")
            WriteQueries.saveObject(db, testFood)
            WriteQueries.saveObject(db, testMeal)
            WriteQueries.saveObject(db, testFoodPortion)
        } catch (e: SQLException) {
            e.printStackTrace()
            Assertions.fail<Any>("Saving objects threw SQL exception")
        }
    }

    private fun Any?.identityHashCode() = System.identityHashCode(this)

    @Test
    fun testBasicFood() {
        val dataSource = CachedDataSource(db)
        val uncachedFood1 = dataSource.getFoodById(1)
        val uncachedFood2 = dataSource.getFoodById(1)
        assertNotNull(uncachedFood1)
        assert(uncachedFood1.identityHashCode() != uncachedFood2.identityHashCode())

        val cachedFood1 = dataSource.getFood(1)
        val cachedFood2 = dataSource.getFood(1)
        assert(cachedFood1.identityHashCode() == cachedFood2.identityHashCode())
    }

    @Test
    fun testBasicSaveEdits() {
        val dataSource = CachedDataSource(db)
        val uncachedFood = dataSource.getFoodById(1)
        assertNotNull(uncachedFood)

        val alteredFood = uncachedFood!!.dataFullCopy().run {
            put(FoodTable.NAME, "Edited food")
            put(FoodTable.INDEX_NAME, "food2")
            Food.factory.construct(this, ObjectSource.DB_EDIT)
        }

        val cachedFood1 = dataSource.getFood(1)
        dataSource.saveObject(alteredFood)
        val cachedFood2 = dataSource.getFood(1)
        assert(cachedFood1.identityHashCode() != cachedFood2.identityHashCode())
    }

}