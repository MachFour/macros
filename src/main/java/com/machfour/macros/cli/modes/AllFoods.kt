package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.printErr
import com.machfour.macros.cli.utils.printFoodList
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.entities.Food
import com.machfour.macros.queries.FoodQueries
import com.machfour.macros.sql.SqlDatabase

import java.sql.SQLException


class AllFoods(config: MacrosConfig) : CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "allfoods"
        private const val USAGE = "Usage: $programName $NAME"

    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }
        val ds = config.database
        listFoods(ds)

        return 0
    }

    private fun listFoods(ds: SqlDatabase) {
        val allFoods: Collection<Food>
        try {
            allFoods = FoodQueries.getAllFoodsMap(ds).values
        } catch (e: SQLException) {
            printErr("SQL exception occurred: ")
            printlnErr(e.errorCode)
            return
        }

        if (allFoods.isEmpty()) {
            println("No foods currently recorded in the database.")
        } else {
            println("============")
            println(" All Foods  ")
            println("============")
            printFoodList(allFoods)
        }
        //DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        //dateFormat.setTimeZone(TimeZone.getDefault());
    }
}
