package com.machfour.macros.queries

import com.machfour.macros.core.FoodType
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.entities.Meal
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxSqlConfig
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.MealTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.units.GRAMS
import com.machfour.macros.util.DateStamp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
            with (RowData(FoodTable)) {
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
                put(FoodPortionTable.QUANTITY_UNIT, GRAMS.abbr)
                put(FoodPortionTable.NOTES, "food portion notes")
                testFoodPortion = FoodPortion.factory.construct(this, ObjectSource.IMPORT)
            }

            db = LinuxDatabase.getInstance(DB_LOCATION)
            try {
                LinuxDatabase.deleteIfExists(DB_LOCATION)
                db.initDb(LinuxSqlConfig())
            } catch (e1: IOException) {
                e1.printStackTrace()
                Assertions.fail("Database initialisation threw IO exception")
            } catch (e2: SQLException) {
                e2.printStackTrace()
                Assertions.fail("Database initialisation threw SQL exception")
            }
        }

    }

    @BeforeEach
    fun saveObjects() {
        try {
            println("Clearing tables")
            clearTable(db, FoodPortion.table)
            clearTable(db, Meal.table)
            clearTable(db, ServingTable)
            clearTable(db, FoodNutrientValue.table)
            clearTable(db, FoodTable)

            println("Saving objects")
            saveObject(db, testFood)
            saveObject(db, testMeal)
            saveObject(db, testFoodPortion)
        } catch (e: SQLException) {
            e.printStackTrace()
            Assertions.fail("Saving objects threw SQL exception")
        }
    }

    private fun Any?.identityHashCode() = System.identityHashCode(this)

    @Test
    fun testBasicFood() {
        val dataSource = FlowDataSource(db)
        val cachedFood1 = runBlocking { dataSource.getFood(1).first() }
        val cachedFood2 = runBlocking { dataSource.getFood(1).first() }
        assertNotNull(cachedFood1)

        assert(cachedFood1.identityHashCode() == cachedFood2.identityHashCode())
    }

    @Test
    fun testBasicSaveEdits() {
        val dataSource = FlowDataSource(db)
        val uncachedFood = runBlocking { dataSource.getFood(1).first() }
        assertNotNull(uncachedFood)

        val alteredFood = uncachedFood!!.dataFullCopy().run {
            put(FoodTable.NAME, "Edited food")
            put(FoodTable.INDEX_NAME, "food2")
            Food.factory.construct(this, ObjectSource.DB_EDIT)
        }

        val cachedFood1 = runBlocking { dataSource.getFood(1).first() }
        dataSource.saveObject(alteredFood)
        val cachedFood2 = runBlocking { dataSource.getFood(1).first() }
        assert(cachedFood1.identityHashCode() != cachedFood2.identityHashCode())
    }

}