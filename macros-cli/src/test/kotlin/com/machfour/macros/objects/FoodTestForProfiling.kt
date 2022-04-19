package com.machfour.macros.objects

import com.machfour.macros.core.FoodType
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.Food
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxSqlConfig
import com.machfour.macros.queries.insertObjects
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.SqlException
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class FoodTestForProfiling {
    companion object {
        private const val DB_LOCATION = "/home/max/devel/macros/test.sqlite"
        private lateinit var db: LinuxDatabase
        private lateinit var foodDc: RowData<Food>
        private var testFood: Food? = null

        private fun initDb() {
            db = LinuxDatabase.getInstance(DB_LOCATION)
            try {
                LinuxDatabase.deleteIfExists(DB_LOCATION)
                db.initDb(LinuxSqlConfig())
            } catch (e: SqlException) {
                e.printStackTrace()
            }

        }

        private fun doFood() {
            foodDc = RowData(FoodTable).apply {
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
            }
            testFood = Food.factory.construct(foodDc, ObjectSource.IMPORT)
        }

        @JvmStatic
        @BeforeAll
        fun init() {
            initDb()
            doFood()
        }
    }

    @Test
    fun saveALotOfFood() {
        val lotsOfFoods = ArrayList<Food>(1000)
        for (i in 0..999) {
            val modifiedData = foodDc.copy().apply {
                put(FoodTable.ID, i.toLong())
                put(FoodTable.INDEX_NAME, "food$i")
            }
            Food.factory.construct(modifiedData, ObjectSource.RESTORE).also {
                lotsOfFoods.add(it)
            }
        }
        try {
            insertObjects(db, lotsOfFoods, true)
        } catch (e: SqlException) {
            fail(e)
        }

    }


}