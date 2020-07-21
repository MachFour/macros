package objects

import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.Schema
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.objects.Food
import com.machfour.macros.objects.FoodType
import com.machfour.macros.queries.Queries

import java.io.IOException
import java.sql.SQLException

class FoodTestForProfiling {
    companion object {
        private val DB_LOCATION = "/home/max/devel/macros-kotlin/test.sqlite"
        private lateinit var db: LinuxDatabase
        private lateinit var foodDc: ColumnData<Food>
        private var testFood: Food? = null

        private fun initDb() {
            db = LinuxDatabase.getInstance(DB_LOCATION)
            try {
                LinuxDatabase.deleteIfExists(DB_LOCATION)
                db.initDb()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: SQLException) {
                e.printStackTrace()
            }

        }

        private fun doFood() {
            foodDc = ColumnData(Schema.FoodTable.instance)
            foodDc.put(Schema.FoodTable.ID, MacrosEntity.NO_ID)
            foodDc.put(Schema.FoodTable.CREATE_TIME, 0L)
            foodDc.put(Schema.FoodTable.MODIFY_TIME, 0L)
            foodDc.put(Schema.FoodTable.INDEX_NAME, "food1")
            foodDc.put(Schema.FoodTable.BRAND, "Max's")
            foodDc.put(Schema.FoodTable.VARIETY, "really good")
            foodDc.put(Schema.FoodTable.NAME, "food")
            foodDc.put(Schema.FoodTable.NOTES, "notes")
            foodDc.put(Schema.FoodTable.CATEGORY, "Dairy")
            foodDc.put(Schema.FoodTable.FOOD_TYPE, FoodType.PRIMARY.niceName)
            foodDc.put(Schema.FoodTable.USDA_INDEX, null)
            foodDc.put(Schema.FoodTable.NUTTAB_INDEX, null)
            testFood = Food.factory().construct(foodDc, ObjectSource.IMPORT)
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
            db.clearTable(Schema.FoodTable.instance)
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }

    private fun saveALotOfFood() {
        val lotsOfFoods = ArrayList<Food>(1000)
        for (i in 0..999) {
            val modifiedData = foodDc.copy()
            modifiedData.put(Schema.FoodTable.ID, i.toLong())
            modifiedData.put(Schema.FoodTable.INDEX_NAME, "food$i")
            val modifiedIndexName = Food.factory().construct(modifiedData, ObjectSource.IMPORT)
            lotsOfFoods.add(modifiedIndexName)
        }
        try {
            Queries.insertObjects(db, lotsOfFoods, true)
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }


}