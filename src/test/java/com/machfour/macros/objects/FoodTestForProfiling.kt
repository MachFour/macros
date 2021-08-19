package com.machfour.macros.objects

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodType
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxSqlConfig
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.orm.schema.FoodTable
import com.machfour.macros.queries.WriteQueries
import com.machfour.macros.sql.RowData
import java.io.IOException
import java.sql.SQLException

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
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: SQLException) {
                e.printStackTrace()
            }

        }

        private fun doFood() {
            foodDc = RowData(FoodTable.instance)
            foodDc.put(FoodTable.ID, MacrosEntity.NO_ID)
            foodDc.put(FoodTable.CREATE_TIME, 0L)
            foodDc.put(FoodTable.MODIFY_TIME, 0L)
            foodDc.put(FoodTable.INDEX_NAME, "food1")
            foodDc.put(FoodTable.BRAND, "Max's")
            foodDc.put(FoodTable.VARIETY, "really good")
            foodDc.put(FoodTable.NAME, "food")
            foodDc.put(FoodTable.NOTES, "notes")
            foodDc.put(FoodTable.CATEGORY, "Dairy")
            foodDc.put(FoodTable.FOOD_TYPE, FoodType.PRIMARY.niceName)
            foodDc.put(FoodTable.USDA_INDEX, null)
            foodDc.put(FoodTable.NUTTAB_INDEX, null)
            testFood = Food.factory.construct(foodDc, ObjectSource.IMPORT)
        }

        @JvmStatic
        fun main(args: Array<String>) {
            initDb()
            doFood()
            val f = FoodTestForProfiling()
            f.saveALotOfFood()
        }
    }

    fun clearFoodTable() {
        try {
            db.clearTable(FoodTable.instance)
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }

    private fun saveALotOfFood() {
        val lotsOfFoods = ArrayList<Food>(1000)
        for (i in 0..999) {
            val modifiedData = foodDc.copy()
            modifiedData.put(FoodTable.ID, i.toLong())
            modifiedData.put(FoodTable.INDEX_NAME, "food$i")
            val modifiedIndexName = Food.factory.construct(modifiedData, ObjectSource.IMPORT)
            lotsOfFoods.add(modifiedIndexName)
        }
        try {
            WriteQueries.insertObjects(db, lotsOfFoods, true)
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }


}