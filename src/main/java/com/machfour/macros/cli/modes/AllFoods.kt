package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.objects.Food
import com.machfour.macros.queries.FoodQueries
import com.machfour.macros.persistence.MacrosDataSource

import java.sql.SQLException


class AllFoods(config: MacrosConfig) : CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "allfoods"
        private val USAGE = "Usage: $programName $NAME"

    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }
        val ds = config.dataSourceInstance
        listFoods(ds)

        return 0
    }

    private fun listFoods(ds: MacrosDataSource) {
        val allFoods: List<Food>
        try {
            allFoods = FoodQueries.getAllFoods(ds)
        } catch (e: SQLException) {
            err.print("SQL exception occurred: ")
            err.println(e.errorCode)
            return
        }

        if (allFoods.isEmpty()) {
            out.println("No foods currently recorded in the database.")
        } else {
            out.println("============")
            out.println(" All Foods  ")
            out.println("============")
            SearchFood.printFoodList(allFoods, out)
        }
        //DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        //dateFormat.setTimeZone(TimeZone.getDefault());
    }
}
