package com.machfour.macros.queries

import com.machfour.datestamp.makeDateStamp
import com.machfour.macros.core.*
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.entities.Meal
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxSqlConfig
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.MealTable
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.rowdata.RowData
import com.machfour.macros.units.GRAMS
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class CacheTest {
    private lateinit var db: LinuxDatabase
    private lateinit var testFood: Food
    private lateinit var testFoodPortion: FoodPortion
    private lateinit var testMeal: Meal

    private var testFoodId: EntityId = MacrosEntity.NO_ID
    private var testMealId: EntityId = MacrosEntity.NO_ID

    @BeforeTest
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
            put(FoodPortionTable.FOOD_ID, 1.id)
            put(FoodPortionTable.MEAL_ID, EntityId(1L))
            put(FoodPortionTable.QUANTITY, 100.0)
            put(FoodPortionTable.QUANTITY_UNIT, GRAMS.abbr)
            put(FoodPortionTable.NOTES, "food portion notes")
            testFoodPortion = FoodPortion.factory.construct(this, ObjectSource.IMPORT)
        }

        db = LinuxDatabase.getInstance("").apply {
            openConnection(getGeneratedKeys = true)
            initDb(LinuxSqlConfig)
        }

        try {
            println("Saving objects")

            testFoodId = saveObject(db, FoodTable,testFood)
            println("Test food ID: $testFoodId")

            testMealId = saveObject(db, MealTable,testMeal)
            println("Test meal ID: $testMealId")

            saveObject(db, FoodPortionTable, testFoodPortion)
        } catch (e: SqlException) {
            e.printStackTrace()
            fail("Saving objects threw SQL exception")
        }
    }

    @AfterTest
    fun deInit() {
        db.closeConnection()
    }

    private fun Any?.identityHashCode() = System.identityHashCode(this)

    @Test
    fun testBasicFood() {
        val dataSource = FlowDataSource(db)
        val cachedFood1 = runBlocking { dataSource.getFood(1.id).first() }
        val cachedFood2 = runBlocking { dataSource.getFood(1.id).first() }
        assertNotNull(cachedFood1)

        check(cachedFood1.identityHashCode() == cachedFood2.identityHashCode())
    }

    @Test
    fun testBasicSaveEdits() {
        val dataSource = FlowDataSource(db)
        val cachedFood1 = runBlocking { dataSource.getFood(1.id).first() }
        assertNotNull(cachedFood1)

        val alteredFood = cachedFood1.toRowData().run {
            put(FoodTable.NAME, "Edited food")
            put(FoodTable.INDEX_NAME, "food2")
            Food.factory.construct(this, ObjectSource.DB_EDIT)
        }

        val cachedFood2 = runBlocking { dataSource.getFood(1.id).first() }
        dataSource.saveObject(FoodTable, alteredFood)
        val cachedFood3 = runBlocking { dataSource.getFood(1.id).first() }
        assertNotEquals(cachedFood2.identityHashCode(), cachedFood3.identityHashCode())
    }

    @Test
    fun testSaveMealForDay() {
        val dataSource = FlowDataSource(db)
        val mealDay = testMeal.day
        val mealsForDay = dataSource.getMealsForDay(mealDay)

        val mealsBeforeAdding = runBlocking { mealsForDay.first() }
        assertEquals(1, mealsBeforeAdding.size)

        dataSource.saveObject(MealTable, testMeal)

        val mealsAfterAdding = runBlocking { mealsForDay.first() }
        assertEquals(2, mealsAfterAdding.size)
    }
}