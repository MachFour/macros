package com.machfour.macros.queries

import com.machfour.datestamp.makeDateStamp
import com.machfour.macros.core.FoodType
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.entities.Meal
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxSqlConfig
import com.machfour.macros.queries.FlowDataSource
import com.machfour.macros.queries.clearTable
import com.machfour.macros.queries.saveObject
import com.machfour.macros.schema.*
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.SqlException
import com.machfour.macros.units.GRAMS
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
            with (RowData(MealTable)) {
                put(MealTable.ID, MacrosEntity.NO_ID)
                put(MealTable.CREATE_TIME, 0L)
                put(MealTable.MODIFY_TIME, 0L)
                put(MealTable.NAME, "meal")
                put(MealTable.NOTES, "meal notes")
                put(MealTable.DAY, makeDateStamp(2020, 10, 10))
                testMeal = Meal.factory.construct(this, ObjectSource.IMPORT)
            }
            with (RowData(FoodPortionTable)) {
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
            } catch (e: SqlException) {
                e.printStackTrace()
                Assertions.fail("Database initialisation threw SQL exception")
            }
        }

    }

    @BeforeEach
    fun saveObjects() {
        try {
            println("Clearing tables")
            clearTable(db, FoodPortionTable)
            clearTable(db, MealTable)
            clearTable(db, ServingTable)
            clearTable(db, FoodNutrientValueTable)
            clearTable(db, FoodTable)

            println("Saving objects")
            saveObject(db, testFood)
            saveObject(db, testMeal)
            saveObject(db, testFoodPortion)
        } catch (e: SqlException) {
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