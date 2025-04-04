package com.machfour.macros.queries

import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.Food
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxSqlConfig
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.SqlException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.fail

class InsertRowsReturningIdsTest {

    private lateinit var db: LinuxDatabase
    private lateinit var ds: MacrosDataSource

    @BeforeTest
    fun init() {
        db = LinuxDatabase.getInstance("")  // in-memory test database
        ds = StaticDataSource(db)
    }

    @Test
    fun testInsertRowsReturningIds() {
        db.openConnection(getGeneratedKeys = true)
        db.initDb(LinuxSqlConfig())

        val food1 = with (RowData(FoodTable)) {
            put(FoodTable.INDEX_NAME, "food1")
            put(FoodTable.NAME, "food 1")
            put(FoodTable.CATEGORY, "uncategorized")
            Food.factory.construct(this, ObjectSource.TEST)
        }

        val food2 = with (RowData(FoodTable)) {
            put(FoodTable.INDEX_NAME, "food2")
            put(FoodTable.NAME, "food 2")
            put(FoodTable.CATEGORY, "uncategorized")
            Food.factory.construct(this, ObjectSource.TEST)
        }

        try {
            val ids = db.insertRowsReturningIds(listOf(food1, food2).map { it.data }, useDataIds = false)
            println("generated IDs: $ids")
        } catch (e: SqlException) {
            fail(e.message)
        }
    }
}