package com.machfour.macros.json

import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxSqlConfig
import com.machfour.macros.queries.getAllFoodsMap
import kotlin.test.Test

class JsonImportTest {

    @Test
    fun testSimpleImportJson() {
        val db = LinuxDatabase.getInstance("") // in-memory database
        db.openConnection(true)
        db.initDb(LinuxSqlConfig)

        val result = importJsonFoods(db, listOf(apple, carrotCakes))
        assert(result.isEmpty())

        val foods = getAllFoodsMap(db)

        assert(foods.size == 2)
        for (f in foods) {
            println(f)
        }
    }
}