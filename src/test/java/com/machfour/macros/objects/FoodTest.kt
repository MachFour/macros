package com.machfour.macros.objects

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodType
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxSqlConfig
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.orm.schema.FoodTable
import com.machfour.macros.queries.FoodQueries
import com.machfour.macros.queries.WriteQueries
import com.machfour.macros.sql.RowData
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.sqlite.SQLiteException
import java.io.IOException
import java.sql.SQLException

class FoodTest {
    companion object {
        private const val DB_LOCATION = "/home/max/devel/macros/test.sqlite"
        private lateinit var db: LinuxDatabase
        private lateinit var foodData: RowData<Food>
        private lateinit var testFood: Food

        @BeforeAll
        fun initDb() {
            db = LinuxDatabase.getInstance(DB_LOCATION)
            try {
                LinuxDatabase.deleteIfExists(DB_LOCATION)
                db.initDb(LinuxSqlConfig())
            } catch (e1: IOException) {
                e1.printStackTrace()
                fail<Any>("Database initialisation threw IO exception")
            } catch (e2: SQLException) {
                e2.printStackTrace()
                fail<Any>("Database initialisation threw SQL exception")
            }

        }

        @BeforeAll
        @JvmStatic
        fun doFood() {
            foodData = RowData(FoodTable.instance)
            foodData.put(FoodTable.ID, MacrosEntity.NO_ID)
            foodData.put(FoodTable.CREATE_TIME, 0L)
            foodData.put(FoodTable.MODIFY_TIME, 0L)
            foodData.put(FoodTable.INDEX_NAME, "food1")
            foodData.put(FoodTable.BRAND, "Max's")
            foodData.put(FoodTable.VARIETY, "really good")
            foodData.put(FoodTable.NAME, "food")
            foodData.put(FoodTable.NOTES, "notes")
            foodData.put(FoodTable.CATEGORY, "dairy")
            foodData.put(FoodTable.FOOD_TYPE, FoodType.PRIMARY.niceName)
            foodData.put(FoodTable.USDA_INDEX, null)
            foodData.put(FoodTable.NUTTAB_INDEX, null)
            testFood = Food.factory.construct(foodData, ObjectSource.IMPORT)
        }
    }

    @Test
    fun getFoodFromDb() {
        val modifiedData = foodData.copy()
        modifiedData.put(FoodTable.ID, 50L)
        val f = Food.factory.construct(modifiedData, ObjectSource.RESTORE)
        try {
            // first save with known ID
            assertEquals(1, WriteQueries.saveObject(db, f))
        } catch (e: SQLException) {
            e.printStackTrace()
            fail<Any>("DB save threw exception")
        }

        try {
            val f2 = FoodQueries.getFoodById(db, 50L) ?: fail("Food not found in DB")
            assertTrue(f.equalsWithoutMetadata(f2), "Foods did not match in equals sense (ignoring metadata)")
        } catch (e: SQLException) {
            e.printStackTrace()
            fail<Any>("DB get threw exception")
        }


    }

    @Test
    fun saveFoodNotFromDb() {
        try {
            assertEquals(1, WriteQueries.saveObject(db, testFood))
        } catch (e: SQLException) {
            e.printStackTrace()
            fail<Any>("DB save threw exception")
        }

    }

    @Test
    fun saveALotOfFood() {
        val lotsOfFoods = ArrayList<Food>(10000)
        for (i in 0..9999) {
            val modifiedData = foodData.copy()
            modifiedData.put(FoodTable.ID, i.toLong())
            modifiedData.put(FoodTable.INDEX_NAME, "food$i")
            val modifiedIndexName = Food.factory.construct(modifiedData, ObjectSource.RESTORE)
            lotsOfFoods.add(modifiedIndexName)
        }
        try {
            assertEquals(10000, WriteQueries.insertObjects(db, lotsOfFoods, true))
        } catch (e: SQLiteException) {
            fail<Any>("DB save threw SQLite exception with result code: " + e.resultCode)
            e.printStackTrace()
        } catch (e2: SQLException) {
            e2.printStackTrace()
            fail<Any>("DB save threw exception")
        }

    }

    @Test
    fun saveFoodFromDb() {
        val modifiedData = foodData.copy()
        modifiedData.put(FoodTable.ID, 50L)
        val f = Food.factory.construct(modifiedData, ObjectSource.RESTORE)
        try {
            // first save with known ID
            assertEquals(1, WriteQueries.saveObject(db, f))
        } catch (e: SQLException) {
            e.printStackTrace()
            fail<Any>("DB save threw exception")
        }

        // now change the data and save with same ID
        val modifiedData2 = modifiedData.copy()
        modifiedData2.put(FoodTable.NAME, "newName")
        val f1 = Food.factory.construct(modifiedData2, ObjectSource.DB_EDIT)
        try {
            assertEquals(1, WriteQueries.saveObject(db, f1))
        } catch (e: SQLException) {
            e.printStackTrace()
            fail<Any>("DB save threw exception")
        }

    }

    @Test
    fun testSaveWithId() {
        val modifiedData = foodData.copy()
        modifiedData.put(FoodTable.ID, 500L)
        val f = Food.factory.construct(modifiedData, ObjectSource.RESTORE)
        try {
            assertEquals(1, WriteQueries.saveObject(db, f))
        } catch (e: SQLException) {
            e.printStackTrace()
            fail<Any>("DB save threw exception")
        }

    }

    @BeforeEach
    fun setUp() {
        clearFoodTable()
    }

    private fun clearFoodTable() {
        try {
            db.clearTable(FoodTable.instance)
        } catch (e: SQLException) {
            e.printStackTrace()
            fail<Any>("Deleting all foods threw SQL exception")
        }

    }


    @AfterEach
    fun tearDown() {
    }

}