package objects

import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.Schema
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.objects.Food
import com.machfour.macros.objects.FoodType
import com.machfour.macros.queries.FoodQueries
import com.machfour.macros.queries.Queries
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.sqlite.SQLiteException

import java.io.IOException
import java.sql.SQLException

import org.junit.jupiter.api.Assertions.*

class FoodTest {
    companion object {
        private val DB_LOCATION = "/home/max/devel/macros-kotlin/test.sqlite"
        private lateinit var db: LinuxDatabase
        private lateinit var foodData: ColumnData<Food>
        private lateinit var testFood: Food

        @BeforeAll
        @JvmStatic
        fun initDb() {
            db = LinuxDatabase.getInstance(DB_LOCATION)
            try {
                LinuxDatabase.deleteIfExists(DB_LOCATION)
                db.initDb()
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
            foodData = ColumnData(Schema.FoodTable.instance)
            foodData.put(Schema.FoodTable.ID, MacrosEntity.NO_ID)
            foodData.put(Schema.FoodTable.CREATE_TIME, 0L)
            foodData.put(Schema.FoodTable.MODIFY_TIME, 0L)
            foodData.put(Schema.FoodTable.INDEX_NAME, "food1")
            foodData.put(Schema.FoodTable.BRAND, "Max's")
            foodData.put(Schema.FoodTable.VARIETY, "really good")
            foodData.put(Schema.FoodTable.NAME, "food")
            foodData.put(Schema.FoodTable.VARIETY_AFTER_NAME, false)
            foodData.put(Schema.FoodTable.NOTES, "notes")
            foodData.put(Schema.FoodTable.CATEGORY, "dairy")
            foodData.put(Schema.FoodTable.FOOD_TYPE, FoodType.PRIMARY.niceName)
            foodData.put(Schema.FoodTable.USDA_INDEX, null)
            foodData.put(Schema.FoodTable.NUTTAB_INDEX, null)
            testFood = Food.factory().construct(foodData, ObjectSource.IMPORT)
        }
    }

    @Test
    fun getFoodFromDb() {
        val modifiedData = foodData.copy()
        modifiedData.put(Schema.FoodTable.ID, 50L)
        val f = Food.factory().construct(modifiedData, ObjectSource.RESTORE)
        try {
            // first save with known ID
            assertEquals(1, Queries.saveObject(db, f))
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
            assertEquals(1, Queries.saveObject(db, testFood))
        } catch (e: SQLException) {
            e.printStackTrace()
            fail<Any>("DB save threw exception")
        }

    }

    @Test
    fun saveALotOfFood() {
        val lotsOfFoods = ArrayList<Food>(1000)
        for (i in 0..999) {
            val modifiedData = foodData.copy()
            modifiedData.put(Schema.FoodTable.ID, i.toLong())
            modifiedData.put(Schema.FoodTable.INDEX_NAME, "food$i")
            val modifiedIndexName = Food.factory().construct(modifiedData, ObjectSource.RESTORE)
            lotsOfFoods.add(modifiedIndexName)
        }
        try {
            assertEquals(1000, Queries.insertObjects(db, lotsOfFoods, true))
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
        modifiedData.put(Schema.FoodTable.ID, 50L)
        val f = Food.factory().construct(modifiedData, ObjectSource.RESTORE)
        try {
            // first save with known ID
            assertEquals(1, Queries.saveObject(db, f))
        } catch (e: SQLException) {
            e.printStackTrace()
            fail<Any>("DB save threw exception")
        }

        // now change the data and save with same ID
        val modifiedData2 = modifiedData.copy()
        modifiedData2.put(Schema.FoodTable.NAME, "newName")
        val f1 = Food.factory().construct(modifiedData2, ObjectSource.DB_EDIT)
        try {
            assertEquals(1, Queries.saveObject(db, f1))
        } catch (e: SQLException) {
            e.printStackTrace()
            fail<Any>("DB save threw exception")
        }

    }

    @Test
    fun testSaveWithId() {
        val modifiedData = foodData.copy()
        modifiedData.put(Schema.FoodTable.ID, 500L)
        val f = Food.factory().construct(modifiedData, ObjectSource.RESTORE)
        try {
            assertEquals(1, Queries.saveObject(db, f))
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
            db.clearTable(Schema.FoodTable.instance)
        } catch (e: SQLException) {
            e.printStackTrace()
            fail<Any>("Deleting all foods threw SQL exception")
        }

    }


    @AfterEach
    fun tearDown() {
    }

}