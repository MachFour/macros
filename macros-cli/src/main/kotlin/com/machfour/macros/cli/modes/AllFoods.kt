package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.printFoodList
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.core.CliConfig
import com.machfour.macros.entities.Food
import com.machfour.macros.queries.getAllFoodsMap
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException


class AllFoods(config: CliConfig) : CommandImpl(config) {
    override val name: String = "allfoods"
    override val usage: String = noArgsUsage

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
            allFoods = getAllFoodsMap(ds).values
        } catch (e: SqlException) {
            printlnErr("SQL exception occurred: ${e.message}")
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
